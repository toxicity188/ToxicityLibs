package kor.toxicity.toxicitylibs.plugin.util.manager;

import kor.toxicity.toxicitylibs.api.ToxicityPlugin;
import kor.toxicity.toxicitylibs.api.manager.ItemManager;
import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import kor.toxicity.toxicitylibs.plugin.util.ConfigUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemManagerImpl implements ItemManager {
    public static final ItemManager MANAGER = new ItemManagerImpl();
    private ItemManagerImpl() {

    }
    private final Map<String,ItemStack> itemStackMap = new HashMap<>();
    @Override
    public @Nullable ItemStack getItemStack(String name) {
        return itemStackMap.get(name);
    }

    @Override
    public @NotNull Set<String> getAllKeys() {
        return itemStackMap.keySet();
    }

    @Override
    public void start(ToxicityPlugin plugin) {
    }

    @Override
    public void reload(ToxicityPlugin plugin) {
        itemStackMap.clear();
        plugin.loadYamlFolder("items", (f,c) -> c.getKeys(false).forEach(s -> ConfigUtil.getAsItemStack(c,s).ifPresentOrElse(i -> itemStackMap.put(s,i),() -> ToxicityLibs.warn("syntax error has occurred: (" + s + " in " + f.getName() + ")"))));
    }
    @Override
    public void end(ToxicityPlugin plugin) {

    }
}
