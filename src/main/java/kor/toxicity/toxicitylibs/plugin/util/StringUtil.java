package kor.toxicity.toxicitylibs.plugin.util;

import kor.toxicity.toxicitylibs.api.ComponentReader;
import kor.toxicity.toxicitylibs.api.ToxicityConfig;
import net.kyori.adventure.text.Component;

import java.io.File;

public class StringUtil {
    private StringUtil() {
        throw new RuntimeException();
    }
    public static Component colored(String target) {
        return new ComponentReader(target).getResult();
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
}
