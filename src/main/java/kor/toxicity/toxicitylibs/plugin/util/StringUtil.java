package kor.toxicity.toxicitylibs.plugin.util;

import kor.toxicity.toxicitylibs.api.FormatType;
import kor.toxicity.toxicitylibs.api.ReaderBuilder;
import kor.toxicity.toxicitylibs.api.ToxicityConfig;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    private StringUtil() {
        throw new RuntimeException();
    }
    public static Component colored(String target) {
        return ReaderBuilder.simple(target).build().getResult();
    }
    public static FileName getFileName(File file) {
        var name = file.getName().split("\\.");
        return name.length == 2 ? new FileName(name[0],name[1]) : new FileName(file.getName(),"");
    }

    public record FileName(String name, String extension) {

    }

    public static String parseTimeFormat(long time) {
        var d = time / 86400;
        var d1 = time % 86400;
        var h = d1 / 3600;
        var d2 = d1 % 3600;
        var m = d2 / 60;
        var s = d2 % 60;
        var sb = new StringBuilder();
        var format = ToxicityConfig.INSTANCE.getTimeFormat();
        if (d > 0) {
            sb.append(format.day().formatted(d));
        }
        if (h > 0) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(format.hour().formatted(h));
        }
        if (m > 0) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(format.minute().formatted(m));
        }
        if (!sb.isEmpty()) sb.append(' ');
        sb.append(format.second().formatted(s));
        return sb.toString();
    }

    public static List<FormattedString> readerParse(String target) {
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
                array.add(new FormattedString(builder.toString(), FormatType.RAW));
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
}
