package kor.toxicity.toxicitylibs;

import org.bukkit.plugin.java.JavaPlugin;

public final class ToxicityLibs extends JavaPlugin {
    private static ToxicityLibs libs;
    @Override
    public void onEnable() {
        libs = this;
        send("Plugin enabled.");
    }

    @Override
    public void onDisable() {
        send("Plugin disabled.");
    }

    public static void send(String message) {
        libs.getLogger().info(message);
    }
    public static void warn(String message) {
        libs.getLogger().warning(message);
    }
}
