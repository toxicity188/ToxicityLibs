package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.ToxicityLibs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class ToxicityPlugin extends JavaPlugin {
    public abstract void load();
    public void reload(@NotNull Consumer<Long> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this, t -> {
            var time = System.currentTimeMillis();
            load();
            var time2 = System.currentTimeMillis() - time;
            Bukkit.getScheduler().runTask(this, t2 -> consumer.accept(time2));
        });
    }

    public ConfigurationSection loadYamlFile(String name) {
        var dataFolder = getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdir();
        var n = name + ".yml";
        var file = new File(dataFolder, n);
        if (!file.exists()) saveResource(n, false);
        var yaml = new YamlConfiguration();
        try {
            yaml.load(file);
            return yaml;
        } catch (Exception e) {
            ToxicityLibs.warn("error has occurred: " + e.getMessage());
            return null;
        }
    }
}
