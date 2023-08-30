package kor.toxicity.toxicitylibs.plugin;

import kor.toxicity.toxicitylibs.api.ComponentReader;
import kor.toxicity.toxicitylibs.api.ToxicityConfig;
import kor.toxicity.toxicitylibs.api.ToxicityPlugin;
import kor.toxicity.toxicitylibs.api.command.CommandAPI;
import kor.toxicity.toxicitylibs.api.command.SenderType;
import kor.toxicity.toxicitylibs.api.util.TimeFormat;
import kor.toxicity.toxicitylibs.plugin.util.ConfigUtil;
import kor.toxicity.toxicitylibs.plugin.util.StringUtil;
import kor.toxicity.toxicitylibs.plugin.util.data.ItemData;
import kor.toxicity.toxicitylibs.plugin.util.data.PlayerData;
import kor.toxicity.toxicitylibs.plugin.util.database.DatabaseSupplier;
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
            .setExecutor((c,a) -> c.sendMessage(new ComponentReader(String.join(" ",a)).buildPlaceholders((Player) c)))
            .build()
            //give
            .create("give")
            .setAliases(new String[] {"g"})
            .setDescription("give handheld item to some player.")
            .setUsage("give <player> [second]")
            .setLength(1)
            .setPermission(new String[] {"toxicitylibs.give"})
            .setAllowedSender(new SenderType[] {SenderType.PLAYER})
            .setExecutor((c,a) -> {
                var item = ((Player) c).getInventory().getItemInMainHand();
                Long left = null;
                var target = Bukkit.getOfflinePlayer(a[0]);
                if (a.length > 1) {
                    try {
                        left = Long.parseLong(a[1]);
                    } catch (Exception e) {
                        getCommandAPI().message(c,"this is not an integer: " + a[1]);
                    }
                }
                long actualLeft = left != null ? left : -1;
                var thread = PLAYER_THREAD_MAP.get(target.getUniqueId());
                var now = LocalDateTime.now();
                if (thread != null) {
                    var data = thread.data;
                    data.getStorageItem().add(new ItemData(now,item,actualLeft));
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(ToxicityLibs.this,() -> {
                        var db = ToxicityConfig.INSTANCE.getCurrentDatabase();
                        var data = db.load(ToxicityLibs.this, target);
                        data.getStorageItem().add(new ItemData(now,item,actualLeft));
                        db.save(ToxicityLibs.this, target, data);
                    });
                }
                getCommandAPI().message(c,"your item successfully be given.");
            })
            .build()
            ;

    @Override
    public void onEnable() {
        setInstance(this);
        var command = getCommand("toxicity");
        if (command != null) command.setExecutor(commandAPI.createTabExecutor());
        var storage = getCommand("storage");
        if (storage != null) storage.setExecutor((sender, command1, label, args) -> {
            if (sender instanceof Player player) {
                if (args.length == 0) {
                    var thread = PLAYER_THREAD_MAP.get(player.getUniqueId());
                    if (thread != null) thread.data.openStorage(player);
                } else if (sender.isOp()) {
                    var data = Bukkit.getPlayer(args[0]);
                    if (data == null) return true;
                    var thread = PLAYER_THREAD_MAP.get(data.getUniqueId());
                    if (thread != null) thread.data.openStorage(player);
                }
            }
            return true;
        });
        send("Plugin enabled.");
    }
    @Override
    protected void load() {
        var config = loadYamlFile("config");
        if (config != null) {
            ConfigUtil.getAsConfig(config,"database").ifPresent(db -> {
                var using = ConfigUtil.getAsString(db,"using").orElse("yml");
                try {
                    var find = DatabaseSupplier.valueOf(using.toUpperCase());
                    var cfg = ConfigUtil.getAsConfig(db,using.toLowerCase()).orElse(new MemoryConfiguration());
                    var supply = find.supply(cfg);
                    if (supply != null) ToxicityConfig.INSTANCE.setCurrentDatabase(supply);
                } catch (Exception e) {
                    warn("unable to find the database: " + using);
                }
            });
            ToxicityConfig.INSTANCE.setAutoSaveTime(config.getLong("auto-save-time"));
            ConfigUtil.getAsString(config,"storage-name").ifPresent(s -> ToxicityConfig.INSTANCE.setStorageName(new ComponentReader(s)));
            ConfigUtil.getAsStringList(config,"storage-item-suffix").ifPresent(s -> ToxicityConfig.INSTANCE.setStorageItemSuffix(s.stream().map(ComponentReader::new).toList()));
            ConfigUtil.getAsConfig(config,"time-format").ifPresent(c -> ToxicityConfig.INSTANCE.setTimeFormat(new TimeFormat(
                    ConfigUtil.getAsString(c,"day").orElse("%dd"),
                    ConfigUtil.getAsString(c,"hour").orElse("%h"),
                    ConfigUtil.getAsString(c,"minute").orElse("%m"),
                    ConfigUtil.getAsString(c,"second").orElse("%ds")
            )));
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
        load(Objects.requireNonNull(plugin));
    }

    private static void load(ToxicityPlugin plugin) {
        libs = plugin;
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
                    plugin.getItemManager().end(plugin);
                    plugin.getGuiManager().end(plugin);
                }
            }
        },plugin);
        ToxicityConfig.INSTANCE.setStorageName(new ComponentReader("Storage"));
        ToxicityConfig.INSTANCE.setInventorySmallMessage(new ComponentReader("<color:red>your inventory space is too small to get this item!"));
        plugin.getItemManager().start(plugin);
        plugin.getGuiManager().start(plugin);
        plugin.reload();
    }

    private static class PlayerThread {
        private final BukkitTask task, check;
        private final JavaPlugin plugin;
        private final Player player;
        private final PlayerData data;
        private PlayerThread(JavaPlugin plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
            this.data = ToxicityConfig.INSTANCE.getCurrentDatabase().load(plugin,player);

            var time = ToxicityConfig.INSTANCE.getAutoSaveTime();
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this::save,time,time);
            check = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,this::removeIf,20,20);
        }
        private synchronized void removeIf() {
            var now = LocalDateTime.now();
            data.getStorageItem().removeIf(i -> i.leftTime() > 0 && ChronoUnit.SECONDS.between(i.time(),now) > i.leftTime());
        }

        private void save() {
            ToxicityConfig.INSTANCE.getCurrentDatabase().save(plugin, player, data);
        }
        private void cancel() {
            task.cancel();
            check.cancel();
        }
    }
}
