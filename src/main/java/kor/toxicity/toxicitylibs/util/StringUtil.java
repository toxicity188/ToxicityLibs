package kor.toxicity.customcrates.util;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;

import java.io.File;

public class StringUtil {
    private static final NamespacedKey CRATE_KEY = NamespacedKey.fromString("customcrates.crate.key");
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

    public static NamespacedKey getCrateKey() {
        return CRATE_KEY;
    }
}
