package kor.toxicity.toxicitylibs.plugin.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class PlayerUtil {
    private PlayerUtil() {
        throw new RuntimeException();
    }
    public static int emptySpace(Player player) {
        var inv = player.getInventory();
        int r = 0;
        for (int i = 0; i < 36; i++) {
            var item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) r ++;
        }
        return r;
    }
    public static int storage(@NotNull Player player, @Nullable ItemStack target) {
        if (target == null || switch (target.getType()) {
            case AIR,CAVE_AIR,VOID_AIR -> true;
            default -> false;
        }) return emptySpace(player);
        var inv = player.getInventory();
        int max = target.getMaxStackSize();
        return IntStream.range(0,36).map(i -> {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                if (switch (item.getType()) {
                    case AIR,CAVE_AIR,VOID_AIR -> true;
                    default -> false;
                }) return max;
                else if (item.isSimilar(target)) return Math.max(max - item.getAmount(), 0);
                else return 0;
            } else return max;
        }).sum();
    }
}
