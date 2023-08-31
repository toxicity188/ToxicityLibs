package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ComponentReader {
    private static final Pattern DECORATION_PATTERN = Pattern.compile("<((?<name>([a-zA-Z]+)):(?<value>(\\w|,|_|-|#|:)+))>");
    private static final Map<String, BiConsumer<ComponentData, String>> FUNCTION_MAP = new HashMap<>();
    private static final Map<TextDecoration, TextDecoration.State> DECORATION_STATE_MAP = new EnumMap<>(TextDecoration.class);

    static {
        DECORATION_STATE_MAP.put(TextDecoration.BOLD, TextDecoration.State.FALSE);
        DECORATION_STATE_MAP.put(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        DECORATION_STATE_MAP.put(TextDecoration.UNDERLINED, TextDecoration.State.FALSE);
        DECORATION_STATE_MAP.put(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE);
        DECORATION_STATE_MAP.put(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE);

        var colorMap = new HashMap<String, TextColor>();
        colorMap.put("BLACK",NamedTextColor.BLACK);
        colorMap.put("DARK_BLUE", NamedTextColor.DARK_BLUE);
        colorMap.put("DARK_GREEN",NamedTextColor.DARK_GREEN);
        colorMap.put("DARK_AQUA",NamedTextColor.DARK_AQUA);
        colorMap.put("DARK_RED",NamedTextColor.DARK_RED);
        colorMap.put("DARK_PURPLE",NamedTextColor.DARK_PURPLE);
        colorMap.put("GOLD", NamedTextColor.GOLD);
        colorMap.put("GRAY", NamedTextColor.GRAY);
        colorMap.put("DARK_GRAY", NamedTextColor.DARK_GRAY);
        colorMap.put("BLUE",NamedTextColor.BLUE);
        colorMap.put("GREEN",NamedTextColor.GREEN);
        colorMap.put("AQUA",NamedTextColor.AQUA);
        colorMap.put("RED", NamedTextColor.RED);
        colorMap.put("LIGHT_PURPLE",NamedTextColor.LIGHT_PURPLE);
        colorMap.put("YELLOW",NamedTextColor.YELLOW);
        colorMap.put("WHITE",NamedTextColor.WHITE);


        FUNCTION_MAP.put("color", (b,s) -> {
            b.mapper = null;
            b.decoration = DECORATION_STATE_MAP;
            if (s.equals("null")) {
                b.color = null;
                return;
            }
            if (s.startsWith("#") && s.length() == 7) {
                var color = TextColor.fromHexString(s);
                if (color != null) b.color = color;
                else ToxicityLibs.warn("this is not a valid hex format: " + s);
            } else {
                var color = colorMap.get(s.toUpperCase());
                if (color != null) b.color = color;
                else ToxicityLibs.warn("unable to find the color named " + s);
            }
        });
        FUNCTION_MAP.put("font", (b,s) -> {
            if (s.equals("null")) {
                b.font = null;
            } else {
                b.font = Key.key(s.toLowerCase());
            }
        });
        FUNCTION_MAP.put("decoration", (b,s) -> {
            Map<TextDecoration, TextDecoration.State> decorationStateMap = new EnumMap<>(DECORATION_STATE_MAP);
            for (String string : s.split(",")) {
                try {
                    decorationStateMap.put(TextDecoration.valueOf(string.toUpperCase()), TextDecoration.State.TRUE);
                } catch (Exception e) {
                    ToxicityLibs.warn("unable to find the decoration: " + string);
                }
            }
            b.decoration = decorationStateMap;
        });
        FUNCTION_MAP.put("gradient", (b,s) -> {
            b.decoration = DECORATION_STATE_MAP;
            var split = s.split("-");
            if (split.length == 2) {
                var color = Arrays.stream(split).map(c -> (c.startsWith("#") && c.length() == 7) ? TextColor.fromHexString(c) : colorMap.get(c.toUpperCase())).filter(Objects::nonNull).toList();
                if (color.size() == 2) {
                    var color1 = color.get(0);
                    var color2 = color.get(1);

                    var r1 = (double) color1.red();
                    var g1 = (double) color1.green();
                    var b1 = (double) color1.blue();

                    var r2 = (double) color2.red() - r1;
                    var g2 = (double) color2.green() - g1;
                    var b2 = (double) color2.blue() - b1;

                    b.mapper = (str,data) -> {
                        var comp = Component.empty();
                        var d = 1D / (double) str.length();
                        var i = 0D;
                        for (char c : str.toCharArray()) {
                            comp = comp.append(Component.text(c).color(TextColor.color(
                                    (int) (r1 + r2 * d * i),
                                    (int) (g1 + g2 * d * i),
                                    (int) (b1 + b2 * d * i)
                            )).font(data.font).decorations(data.decoration));
                            i++;
                        }
                        return comp;
                    };
                } else ToxicityLibs.warn("unable to read this gradient: " + s);
            } else ToxicityLibs.warn("unable to read this gradient: " + s);
        });
    }
    private final Component result;
    private final String originalText;
    private final List<Function<Player,Component>> builder = new ArrayList<>();

    public @NotNull Component getResult() {
        return result;
    }
    public @NotNull Component buildPlaceholders(Player player) {
        try {
            var comp = Component.empty();
            for (Function<Player, Component> playerComponentFunction : builder) {
                comp = comp.append(playerComponentFunction.apply(player));
            }
            return comp;
        } catch (Throwable throwable) {
            return result;
        }
    }

    public ComponentReader(String original) {
        var comp = Component.empty();
        var data = new ComponentData();
        for (FormattedString formattedString : parse(original)) {
            switch (formattedString.type()) {
                case RAW -> {
                    var c = data.copy();
                    var content = formattedString.content();
                    Function<String,Component> mapper = (c.mapper != null) ? str -> c.mapper.apply(str,c) : str -> Component.text(str).font(c.font).decorations(c.decoration).color(c.color);
                    comp = comp.append(mapper.apply(content));
                    builder.add(p -> mapper.apply(PlaceholderAPI.setPlaceholders(p,content)));
                }
                case DECORATION -> {
                    var matcher = DECORATION_PATTERN.matcher(formattedString.content());
                    if (matcher.find()) {
                        var name = matcher.group("name");
                        var value = matcher.group("value");
                        var consumer = FUNCTION_MAP.get(name.toLowerCase());
                        if (consumer != null) consumer.accept(data,value);
                        else ToxicityLibs.warn("cannot find this block: " + name);
                    }
                }
            }
        }
        originalText = original;
        result = comp;
    }

    public String getOriginalText() {
        return originalText;
    }

    private static class ComponentData {
        private Key font = null;
        private TextColor color = NamedTextColor.WHITE;
        private Map<TextDecoration,TextDecoration.State> decoration = DECORATION_STATE_MAP;
        private BiFunction<String,ComponentData,Component> mapper = null;

        private ComponentData copy() {
            var data = new ComponentData();
            data.font = font;
            data.color = color;
            data.decoration = decoration;
            data.mapper = mapper;
            return data;
        }
    }

    public static List<FormattedString> parse(String target) {
        var array = new ArrayList<FormattedString>();
        var builder = new StringBuilder();
        var cont = false;
        for (char c : target.toCharArray()) {
            if (cont) {
                builder.append(c);
                cont = false;
                continue;
            }
            if (c == '\\') {
                cont = true;
                continue;
            }
            if (c == '<') {
                array.add(new FormattedString(builder.toString(),FormatType.RAW));
                builder.setLength(0);
            }
            builder.append(c);
            if (c == '>') {
                array.add(new FormattedString(builder.toString(),FormatType.DECORATION));
                builder.setLength(0);
            }
        }
        array.add(new FormattedString(builder.toString(),FormatType.RAW));
        return array;
    }
    public record FormattedString(String content, FormatType type) {

    }
    public enum FormatType {
        RAW,
        DECORATION
    }
}
