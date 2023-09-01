package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.api.data.PlayerData;
import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ToxicityAPI {
    private ToxicityAPI() {
        throw new RuntimeException();
    }
    public static ToxicityPlugin getInstance() {
        return ToxicityLibs.getInstance();
    }
    public static void setInstance(@NotNull ToxicityPlugin plugin) {
        ToxicityLibs.setInstance(plugin);
    }
    public static @Nullable PlayerData getPlayerData(@NotNull Player player) {
        return ToxicityLibs.getPlayerData(player);
    }
    public static void getPlayerData(@NotNull OfflinePlayer player, @NotNull Consumer<PlayerData> consumer) {
        ToxicityLibs.getPlayerData(player,consumer);
    }
}
