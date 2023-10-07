package kor.toxicity.toxicitylibs.api.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kor.toxicity.toxicitylibs.plugin.util.GsonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public record ItemData(LocalDateTime time, ItemStack itemStack, long leftTime) {

    public String serialize() {
        var obj = new JsonObject();
        obj.addProperty("time", time.toString());
        obj.addProperty("item" , serialize0(itemStack));
        obj.addProperty("left", leftTime);
        return Base64.getEncoder().encodeToString(GsonUtil.getGson().toJson(obj).getBytes(StandardCharsets.UTF_8));
    }

    public static @Nullable ItemData deserialize(String string) {
        try {
            var json = JsonParser.parseString(new String(Base64.getDecoder().decode(string))).getAsJsonObject();
            return new ItemData(
                    LocalDateTime.parse(json.getAsJsonPrimitive("time").getAsString()),
                    deserialize0(new String(Base64.getDecoder().decode(json.getAsJsonPrimitive("item").getAsString()))),
                    json.getAsJsonPrimitive("item").getAsLong()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static String serialize0(ItemStack stack) {
        var config = new YamlConfiguration();
        config.set("i",stack);
        return config.saveToString();
    }
    private static ItemStack deserialize0(String string) throws InvalidConfigurationException {
        var config = new YamlConfiguration();
        config.loadFromString(string);
        return config.getItemStack("i");
    }
}