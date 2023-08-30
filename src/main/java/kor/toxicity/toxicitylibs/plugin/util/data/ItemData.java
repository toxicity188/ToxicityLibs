package kor.toxicity.toxicitylibs.plugin.util.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kor.toxicity.toxicitylibs.plugin.util.GsonUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public record ItemData(LocalDateTime time, ItemStack itemStack, long leftTime) {

    public String serialize() {
        var obj = new JsonObject();
        obj.addProperty("time", time.toString());
        obj.addProperty("item" , Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()));
        obj.addProperty("left", leftTime);
        return Base64.getEncoder().encodeToString(GsonUtil.getGson().toJson(obj).getBytes(StandardCharsets.UTF_8));
    }

    public static @Nullable ItemData deserialize(String string) {
        try {
            var json = JsonParser.parseString(new String(Base64.getDecoder().decode(string))).getAsJsonObject();
            return new ItemData(
                    LocalDateTime.parse(json.getAsJsonPrimitive("time").getAsString()),
                    ItemStack.deserializeBytes(Base64.getDecoder().decode(json.getAsJsonPrimitive("item").getAsString())),
                    json.getAsJsonPrimitive("item").getAsLong()
            );
        } catch (Exception e) {
            return null;
        }
    }
}