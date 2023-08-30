package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.api.manager.GuiManager;
import kor.toxicity.toxicitylibs.api.manager.ItemManager;
import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import kor.toxicity.toxicitylibs.plugin.util.StringUtil;
import kor.toxicity.toxicitylibs.plugin.util.manager.GuiManagerImpl;
import kor.toxicity.toxicitylibs.plugin.util.manager.ItemManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class ToxicityPlugin extends JavaPlugin {
    protected abstract void load();
    public void reload() {
        load();
        itemManager.reload(this);
        guiManager.reload(this);
    }

    private final ItemManager itemManager;
    private final GuiManager guiManager;

    public ToxicityPlugin() {
        this(ItemManagerImpl.MANAGER, GuiManagerImpl.MANAGER);
    }
    public ToxicityPlugin(@NotNull ItemManager itemManager, @NotNull GuiManager guiManager) {
        this.itemManager = itemManager;
        this.guiManager = guiManager;
    }

    public final @NotNull GuiManager getGuiManager() {
        return guiManager;
    }

    public final @NotNull ItemManager getItemManager() {
        return itemManager;
    }

    public final void reload(@NotNull Consumer<Long> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this, t -> {
            var time = System.currentTimeMillis();
            reload();
            var time2 = System.currentTimeMillis() - time;
            Bukkit.getScheduler().runTask(this, t2 -> consumer.accept(time2));
        });
    }

    public final ConfigurationSection loadYamlFile(@NotNull String name) {
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
    public final void loadYamlFolder(@NotNull String dir, @NotNull BiConsumer<File,YamlConfiguration> consumer) {
        var dataFolder = getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdir();
        var folder = new File(dataFolder,dir);
        if (!folder.exists()) folder.mkdir();
        var listFile = folder.listFiles();
        if (listFile != null) for (File file : listFile) {
            var name = StringUtil.getFileName(file);
            if (name.extension().equals("yml")) {
                var yaml = new YamlConfiguration();
                try {
                    yaml.load(file);
                    consumer.accept(file,yaml);
                } catch (Exception e) {
                    ToxicityLibs.warn("unable to read this file: " + file.getName());
                }
            }
        }
    }
}
