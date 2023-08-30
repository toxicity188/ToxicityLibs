package kor.toxicity.toxicitylibs.plugin.util.database;

import kor.toxicity.toxicitylibs.plugin.util.data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface Database {
    void close();
    @NotNull PlayerData load(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player);
    boolean save(@NotNull JavaPlugin plugin, @NotNull OfflinePlayer player, @NotNull PlayerData data);
}
