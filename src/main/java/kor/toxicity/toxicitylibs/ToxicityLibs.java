package kor.toxicity.toxicitylibs;

import kor.toxicity.toxicitylibs.api.ComponentReader;
import kor.toxicity.toxicitylibs.api.LibsConfig;
import kor.toxicity.toxicitylibs.api.ToxicityPlugin;
import kor.toxicity.toxicitylibs.api.command.CommandAPI;
import kor.toxicity.toxicitylibs.api.command.SenderType;
import kor.toxicity.toxicitylibs.util.ConfigUtil;
import kor.toxicity.toxicitylibs.util.StringUtil;
import kor.toxicity.toxicitylibs.util.data.PlayerData;
import kor.toxicity.toxicitylibs.util.database.DatabaseSupplier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ToxicityLibs extends ToxicityPlugin {
    private static ToxicityPlugin libs;
    private static final Map<UUID,PlayerThread> PLAYER_THREAD_MAP = new ConcurrentHashMap<>();

    private final CommandAPI commandAPI = new CommandAPI("<gradient:blue-aqua>[ToxicityLibs]")
            .setCommandPrefix("tc")
            //parse
            .create("parse")
            .setAliases(new String[] {"pa"})
            .setDescription("parse your argument.")
            .setUsage("parse <text>")
            .setPermission(new String[] {"toxicitylibs.parse"})
            .setLength(1)
            .setExecutor((c,a) -> c.sendMessage(StringUtil.colored(String.join(" ",a))))
            .build()
            //reload
            .create("reload")
            .setAliases(new String[] {"re","rl"})
            .setDescription("reload this plugin.")
            .setUsage("reload")
            .setPermission(new String[] {"toxicitylibs.reload"})
            .setExecutor((c,a) -> reload(l -> getCommandAPI().message(c,"plugin reloaded (" + l + " ms)")))
            .build()
            //placeholder
            .create("placeholder")
            .setAliases(new String[] {"ph"})
            .setDescription("parse your argument with PlaceholderAPI.")
            .setUsage("placeholder <text>")
            .setPermission(new String[] {"toxicitylibs.placeholder"})
            .setLength(1)
            .setAllowedSender(new SenderType[] {SenderType.PLAYER})
            .setExecutor((c,a) -> {
                try {
                    c.sendMessage(new ComponentReader(String.join(" ",a)).buildPlaceholders((Player) c));
                } catch (Throwable throwable) {
                    getCommandAPI().message(c,"PlaceholderAPI not found!");
                }
            })
            .build()
            ;

    @Override
    public void onEnable() {
        var command = getCommand("toxicity");
        if (command != null) command.setExecutor(commandAPI.createTabExecutor());

        libs = load(this);
        send("Plugin enabled.");
    }

    @Override
    public void load() {
        var config = loadYamlFile("config");
        if (config != null) {
            ConfigUtil.getAsConfig(config,"database").ifPresent(db -> {
                var using = ConfigUtil.getAsString(db,"using").orElse("yml");
                try {
                    var find = DatabaseSupplier.valueOf(using.toUpperCase());
                    var cfg = ConfigUtil.getAsConfig(db,using.toLowerCase()).orElse(new MemoryConfiguration());
                    var supply = find.supply(cfg);
                    if (supply != null) LibsConfig.INSTANCE.setCurrentDatabase(supply);
                } catch (Exception e) {
                    warn("unable to find the database: " + using);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        send("Plugin disabled.");
    }


    public static void send(@NotNull String message) {
        libs.getLogger().info(message);
    }
    public static void warn(@NotNull String message) {
        libs.getLogger().warning(message);
    }

    public CommandAPI getCommandAPI() {
        return commandAPI;
    }

    public static @NotNull ToxicityPlugin getInstance() {
        return Objects.requireNonNull(libs);
    }
    public static void setInstance(@NotNull ToxicityPlugin plugin) {
        libs = load(Objects.requireNonNull(plugin));
    }

    private static ToxicityPlugin load(ToxicityPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void join(PlayerJoinEvent event) {
                var player = event.getPlayer();
                Bukkit.getScheduler().runTaskAsynchronously(plugin,() -> PLAYER_THREAD_MAP.put(player.getUniqueId(),new PlayerThread(plugin,player)));
            }
            @EventHandler
            public void quit(PlayerQuitEvent event) {
                var thread = PLAYER_THREAD_MAP.remove(event.getPlayer().getUniqueId());
                if (thread != null) {
                    thread.cancel();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, thread::save);
                }
            }
            @EventHandler
            public void disable(PluginDisableEvent event) {
                if (event.getPlugin().getName().equals(plugin.getName())) {
                    PLAYER_THREAD_MAP.values().forEach(PlayerThread::save);
                }
            }
        },plugin);
        plugin.load();
        return plugin;
    }

    private static class PlayerThread {
        private final BukkitTask task, check;
        private final JavaPlugin plugin;
        private final Player player;
        private final PlayerData data;
        private PlayerThread(JavaPlugin plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
            this.data = LibsConfig.INSTANCE.getCurrentDatabase().load(plugin,player);

            var time = LibsConfig.INSTANCE.getAutoSaveTime();
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this::save,time,time);
            check = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this::removeIf,20,20);
        }
        private synchronized void removeIf() {
            var now = LocalDateTime.now();
            data.getStorageItem().removeIf(i -> ChronoUnit.SECONDS.between(i.time(),now) > i.leftTime());
        }

        private void save() {
            LibsConfig.INSTANCE.getCurrentDatabase().save(plugin, player, data);
        }
        private void cancel() {
            task.cancel();
            check.cancel();
        }
    }
}
