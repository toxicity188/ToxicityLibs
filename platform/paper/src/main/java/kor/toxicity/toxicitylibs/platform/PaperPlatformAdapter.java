package kor.toxicity.toxicitylibs.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PaperPlatformAdapter implements PlatformAdapter {
    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component name) {
        return Bukkit.createInventory(holder, size, name);
    }

    @Override
    public void setDisplay(ItemMeta meta, Component display) {
        meta.displayName(display);
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        meta.lore(lore);
    }

    @Override
    public Component getDisplay(ItemMeta meta) {
        return meta.displayName();
    }

    @Override
    public List<Component> getLore(ItemMeta meta) {
        return meta.lore();
    }
}
