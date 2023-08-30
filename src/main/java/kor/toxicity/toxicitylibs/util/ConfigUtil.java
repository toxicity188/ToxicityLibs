package kor.toxicity.customcrates.util;

import dev.lone.itemsadder.api.CustomStack;
import kor.toxicity.customcrates.CustomCrates;
import kor.toxicity.customcrates.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class ConfigUtil {
    private ConfigUtil() {
        throw new RuntimeException();
    }
    public static Optional<ItemStack> getAsItemStack(ConfigurationSection section, String key) {
        if (section.isItemStack(key)) return Optional.ofNullable(section.getItemStack(key));
        var str = (section.isString(key)) ? section.getString(key) : null;
        if (str != null) return Optional.ofNullable(ItemManager.getItemStack(str));
        return getAsConfig(section,key).map(c -> {
            var itemsadder = getAsString(c,"itemsadder").orElse(null);
            if (itemsadder != null) {
                try {
                    var item = CustomStack.getInstance(itemsadder);
                    if (item != null) return item.getItemStack();
                } catch (Throwable throwable) {
                    CustomCrates.warn("unable to find ItemsAdder.");
                }
            }
            Material material;
            try {
                material = Material.valueOf(getAsString(c,"type").orElse("APPLE").toUpperCase());
            } catch (Exception e) {
                material = Material.APPLE;
            }
            var item = new ItemStack(material);
            var meta = item.getItemMeta();

            meta.displayName(getAsString(c, "display").map(StringUtil::colored).orElse(null));
            meta.lore(getAsStringList(c,"lore").map(l -> l.stream().map(StringUtil::colored).toList()).orElse(null));
            meta.setUnbreakable(c.getBoolean("unbreakable"));
            meta.setCustomModelData(c.getInt("custom-model-data"));
            getAsStringList(c,"flag").ifPresent(l -> l.forEach(s -> {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase()));
                } catch (Exception e) {
                    CustomCrates.warn("unable to read this flag: " + s);
                }
            }));

            item.setItemMeta(meta);
            return item;
        });
    }

    public static Optional<ConfigurationSection> getAsConfig(ConfigurationSection section, String key) {
        return Optional.ofNullable(section.getConfigurationSection(key));
    }
    public static Optional<String> getAsString(ConfigurationSection section, String key) {
        return Optional.ofNullable(section.getString(key));
    }
    public static Optional<List<String>> getAsStringList(ConfigurationSection section, String key) {
        return section.isList(key) ? Optional.of(section.getStringList(key)) : Optional.empty();
    }
}
