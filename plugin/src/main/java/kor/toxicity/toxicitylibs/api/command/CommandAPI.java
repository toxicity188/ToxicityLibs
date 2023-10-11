package kor.toxicity.toxicitylibs.api.command;

import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import kor.toxicity.toxicitylibs.plugin.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CommandAPI {
    private static final String[] EMPTY_ARRAY = new String[0];
    private static String[] removeFirst(String[] original) {
        var array = new String[original.length - 1];
        System.arraycopy(original, 1, array, 0, array.length);
        return array;
    }


    private final Component prefix;
    private final Map<String,CommandModule> moduleMap = new LinkedHashMap<>();
    private Component notCommandMessage = StringUtil.colored("<color:red>try /tc help to find command.");
    private Component unknownCommandMessage = StringUtil.colored("<color:red>unknown command. try /tc help to find command.");
    private Component permissionRequiredMessage = StringUtil.colored("<color:red>sorry, you have not permission to do that.");
    private Component opOnlyCommandMessage = StringUtil.colored("<color:red>sorry, this is a op only command.");
    private Component notAllowedSenderMessage = StringUtil.colored("<color:red>you are not included in allowed sender type.");

    private final CommandAPI superAPI;

    private String commandPrefix = "";

    public CommandAPI setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
        return this;
    }

    public CommandAPI(String prefix) {
        this(StringUtil.colored(prefix));
    }
    public CommandAPI(Component prefix) {
        this(prefix,null);
    }

    private final CommandAPIBuilder helpBuilder;
    public CommandAPI(@NotNull Component prefix, @NotNull CommandAPI superAPI) {
        this.prefix = prefix;
        this.superAPI = superAPI;
        helpBuilder = create("help")
                .setAliases(new String[] {"h"})
                .setDescription("show all sub-command in this command.")
                .setUsage("help")
                .setExecutor((c,a) -> moduleMap.forEach((k, v) -> message(c, "/" + commandPrefix + " " + v.usage() + " - " + v.description())))
                ;
    }

    public @NotNull CommandAPIBuilder getHelpBuilder() {
        return helpBuilder;
    }

    public @NotNull CommandAPI getSuperAPI() {
        return superAPI;
    }

    public @NotNull CommandAPI setUnknownCommandMessage(@NotNull String unknownCommandMessage) {
        this.unknownCommandMessage = StringUtil.colored(unknownCommandMessage);
        return this;
    }

    public @NotNull CommandAPI setNotAllowedSenderMessage(@NotNull Component notAllowedSenderMessage) {
        this.notAllowedSenderMessage = notAllowedSenderMessage;
        return this;
    }

    public @NotNull CommandAPI setNotCommandMessage(@NotNull Component notCommandMessage) {
        this.notCommandMessage = notCommandMessage;
        return this;
    }

    public @NotNull CommandAPI setOpOnlyCommandMessage(@NotNull Component opOnlyCommandMessage) {
        this.opOnlyCommandMessage = opOnlyCommandMessage;
        return this;
    }

    public @NotNull CommandAPI setPermissionRequiredMessage(@NotNull Component permissionRequiredMessage) {
        this.permissionRequiredMessage = permissionRequiredMessage;
        return this;
    }

    public @NotNull CommandAPI setUnknownCommandMessage(@NotNull Component unknownMessage) {
        this.unknownCommandMessage = unknownMessage;
        return this;
    }

    public @NotNull CommandAPI setNotCommandMessage(@NotNull String notCommandMessage) {
        this.notCommandMessage = StringUtil.colored(notCommandMessage);
        return this;
    }

    public @NotNull CommandAPI setPermissionRequiredMessage(@NotNull String permissionRequiredMessage) {
        this.permissionRequiredMessage = StringUtil.colored(permissionRequiredMessage);
        return this;
    }

    public @NotNull CommandAPI setOpOnlyCommandMessage(@NotNull String opOnlyCommandMessage) {
        this.opOnlyCommandMessage = StringUtil.colored(opOnlyCommandMessage);
        return this;
    }

    public @NotNull CommandAPI setNotAllowedSenderMessage(@NotNull String notAllowedSenderMessage) {
        this.notAllowedSenderMessage = StringUtil.colored(notAllowedSenderMessage);
        return this;
    }

    public @NotNull CommandAPIBuilder create(@NotNull String name) {
        return new CommandAPIBuilder(name);
    }
    public @NotNull CommandAPI createSubBranches(@NotNull String name, @NotNull String[] aliases, @NotNull String description, @NotNull String[] permission, @NotNull SenderType[] allowedSender, boolean opOnly) {
        var api = new CommandAPI(prefix,this);
        api.commandPrefix = commandPrefix;

        api.notCommandMessage = notCommandMessage;
        api.unknownCommandMessage = unknownCommandMessage;
        api.notAllowedSenderMessage = notAllowedSenderMessage;
        api.permissionRequiredMessage = permissionRequiredMessage;
        api.opOnlyCommandMessage = opOnlyCommandMessage;
        moduleMap.put(name, new CommandModule() {
            @Override
            public String[] aliases() {
                return aliases;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String usage() {
                return name;
            }

            @Override
            public SenderType[] allowedSender() {
                return allowedSender;
            }

            @Override
            public boolean opOnly() {
                return opOnly;
            }

            @Override
            public String[] permission() {
                return permission;
            }

            @Override
            public int length() {
                return 0;
            }

            @Override
            public void execute(CommandAPI api1, CommandSender sender, String[] args) {
                if (args.length == 0) {
                    api.message(sender,api.notCommandMessage);
                } else {
                    var module = api.getModule(sender, args[0], true);
                    if (module != null) {
                        var r = removeFirst(args);
                        if (r.length < module.length()) {
                            api.message(sender, Component.text("usage: " + module.usage()));
                        } else module.execute(api1, sender, r);
                    }
                }
            }

            @Override
            public @Nullable List<String> tabComplete(CommandAPI api1, CommandSender sender, String[] args) {
                return switch (args.length) {
                    case 0 -> null;
                    case 1 -> api.moduleMap.keySet().stream().filter(s -> s.startsWith(args[0])).toList();
                    default -> Optional.ofNullable(api.getModule(sender, args[0], false)).map(m -> m.tabComplete(api1, sender,removeFirst(args))).orElse(null);
                };
            }
        });
        return api;
    }

    public @NotNull TabExecutor createTabExecutor() {
        return new TabExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 0) {
                    message(sender,notCommandMessage);
                } else {
                    var module = getModule(sender, args[0], true);
                    if (module != null) {
                        var r = removeFirst(args);
                        if (r.length < module.length()) {
                            message(sender, Component.text("usage: " + module.usage()));
                        } else module.execute(CommandAPI.this, sender, r);
                    }
                }
                return true;
            }

            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return switch (args.length) {
                    case 0 -> null;
                    case 1 -> moduleMap.keySet().stream().filter(s -> s.startsWith(args[0])).toList();
                    default -> Optional.ofNullable(getModule(sender, args[0], false)).map(m -> m.tabComplete(CommandAPI.this, sender,removeFirst(args))).orElse(null);
                };
            }
        };
    }
    public void message(@NotNull CommandSender sender, @NotNull String message) {
        message(sender,Component.text(message).color(NamedTextColor.WHITE));
    }
    public void message(@NotNull CommandSender sender, @NotNull Component message) {
        ToxicityLibs.getAudiences().sender(sender).sendMessage(prefix.append(Component.space().append(message)));
    }

    public void execute(@NotNull CommandSender sender, @NotNull String name, @NotNull String[] args) {
        var module = getModule(sender, name, true);
        if (module != null) module.execute(this, sender, args);
    }
    private @Nullable CommandModule getModule(@NotNull CommandSender sender, @NotNull String name, boolean message) {
        var module = moduleMap.get(name);
        if (module == null) module = moduleMap.values().stream().filter(m -> {
            var b = false;
            for (String alias : m.aliases()) {
                if (alias.equals(name)) {
                    b = true;
                    break;
                }
            }
            return b;
        }).findFirst().orElse(null);
        if (module == null) {
            if (message) message(sender, unknownCommandMessage);
            return null;
        }
        if (module.opOnly() && !sender.isOp()) {
            if (message) message(sender,opOnlyCommandMessage);
            return null;
        }
        var permission = module.permission().length == 0;
        for (String s : module.permission()) {
            if (sender.hasPermission(s)) {
                permission = true;
                break;
            }
        }
        if (!permission) {
            if (message) message(sender,permissionRequiredMessage);
            return null;
        }
        var allowed = module.allowedSender().length == 0;
        for (SenderType senderType : module.allowedSender()) {
            if (senderType.match(sender.getClass())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            if (message) message(sender,notAllowedSenderMessage);
            return null;
        }
        return module;
    }

    public class CommandAPIBuilder {
        private String[] aliases = EMPTY_ARRAY;
        private String description = "unknown";
        private String usage = "usage";
        private SenderType[] allowedSender = SenderType.values();
        private String[] permission = EMPTY_ARRAY;
        private boolean opOnly = false;
        private int length = 0;
        private CommandAPIExecutor executor = (api,s,a) -> {};
        private CommandAPITabCompleter tabCompleter = (api,s,a) -> null;

        private final String name;
        private CommandAPIBuilder(@NotNull String name) {
            this.name = name;
        }

        public @NotNull CommandAPIBuilder setAliases(@NotNull String[] aliases) {
            this.aliases = aliases;
            return this;
        }

        public @NotNull CommandAPIBuilder setAllowedSender(@NotNull SenderType[] allowedSender) {
            this.allowedSender = allowedSender;
            return this;
        }

        public @NotNull CommandAPIBuilder setDescription(@NotNull String description) {
            this.description = description;
            return this;
        }

        public @NotNull CommandAPIBuilder setExecutor(@NotNull BiConsumer<@NotNull CommandSender, @NotNull String[]> executor) {
            this.executor = (api, sender, args) -> executor.accept(sender, args);
            return this;
        }
        public @NotNull CommandAPIBuilder setExecutor(@NotNull CommandAPIExecutor executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull CommandAPIBuilder setOpOnly(boolean opOnly) {
            this.opOnly = opOnly;
            return this;
        }

        public @NotNull CommandAPIBuilder setPermission(@NotNull String[] permission) {
            this.permission = permission;
            return this;
        }

        public @NotNull CommandAPIBuilder setTabCompleter(@NotNull BiFunction<@NotNull CommandSender, @NotNull String[], @Nullable List<String>> tabCompleter) {
            this.tabCompleter = ((api, sender, args) -> tabCompleter.apply(sender, args));
            return this;
        }

        public @NotNull CommandAPIBuilder setUsage(@NotNull String usage) {
            this.usage = usage;
            return this;
        }

        public @NotNull CommandAPIBuilder setLength(int length) {
            this.length = length;
            return this;
        }

        public @NotNull CommandAPI build() {
            moduleMap.put(name, new CommandModule() {
                @Override
                public String[] aliases() {
                    return aliases;
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public String usage() {
                    return usage;
                }

                @Override
                public SenderType[] allowedSender() {
                    return allowedSender;
                }

                @Override
                public boolean opOnly() {
                    return opOnly;
                }

                @Override
                public String[] permission() {
                    return permission;
                }

                @Override
                public void execute(CommandAPI api, CommandSender sender, String[] args) {
                    executor.execute(api, sender,args);
                }

                @Override
                public @Nullable List<String> tabComplete(CommandAPI api, CommandSender sender, String[] args) {
                    return tabCompleter.complete(api, sender,args);
                }

                @Override
                public int length() {
                    return length;
                }
            });
            return CommandAPI.this;
        }
    }
}
