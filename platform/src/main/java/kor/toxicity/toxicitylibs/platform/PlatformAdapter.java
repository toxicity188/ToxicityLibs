package kor.toxicity.toxicitylibs.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public interface PlatformAdapter {
    Inventory createInventory(InventoryHolder holder, int size, Component name);
    void setDisplay(ItemMeta meta, Component display);
    void setLore(ItemMeta meta, List<Component> lore);
    Component getDisplay(ItemMeta meta);
    List<Component> getLore(ItemMeta meta);
}
