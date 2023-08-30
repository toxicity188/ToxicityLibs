package kor.toxicity.toxicitylibs.plugin.util;

import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
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
        return getAsConfig(section,key).map(c -> {
            var reference = ConfigUtil.getAsString(c,"reference").orElse(null);
            if (reference != null) {
                var k = ConfigUtil.getAsString(c,"key").orElse(null);
                if (k != null) {
                    try {
                        return switch (reference.toLowerCase()) {
                            case "itemsadder" -> {
                                var stack = CustomStack.getInstance(k);
                                yield stack != null ? stack.getItemStack() : null;
                            }
                            case "oraxen" -> {
                                var stack = OraxenItems.getItemById(k);
                                yield stack != null ? stack.build() : null;
                            }
                            default -> null;
                        };
                    } catch (Throwable throwable) {
                        ToxicityLibs.warn("plugin not found: " + reference);
                        return null;
                    }
                } else ToxicityLibs.warn("unable to find key: " + reference);
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
                    ToxicityLibs.warn("unable to read this flag: " + s);
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
