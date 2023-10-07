package kor.toxicity.toxicitylibs.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class SpigotPlatformAdapter implements PlatformAdapter {

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().build();

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component name) {
        return Bukkit.createInventory(holder, size, serializer.serialize(name));
    }

    @Override
    public void setDisplay(ItemMeta meta, Component display) {
        meta.setDisplayName(serializer.serialize(display));
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        meta.setLore(lore.stream().map(serializer::serialize).toList());
    }

    @Override
    public Component getDisplay(ItemMeta meta) {
        return serializer.deserialize(meta.getDisplayName());
    }

    @Override
    public List<Component> getLore(ItemMeta meta) {
        var lore = meta.getLore();
        return lore != null ? lore.stream().map(serializer::deserialize).collect(Collectors.toList()) : null;
    }
}
