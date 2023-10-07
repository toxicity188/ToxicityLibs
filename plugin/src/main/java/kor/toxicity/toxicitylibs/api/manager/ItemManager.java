package kor.toxicity.toxicitylibs.api.manager;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemManager extends ToxicityPluginManager {
    @Nullable ItemStack getItemStack(String name);
    @NotNull Set<String> getAllKeys();
}
