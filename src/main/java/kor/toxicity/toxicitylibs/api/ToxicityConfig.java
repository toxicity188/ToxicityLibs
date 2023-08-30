package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.api.util.TimeFormat;
import kor.toxicity.toxicitylibs.plugin.util.database.Database;
import kor.toxicity.toxicitylibs.plugin.util.database.DatabaseSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum ToxicityConfig {
    INSTANCE
    ;
    private long autoSaveTime = 6000L;
    private Database currentDatabase = DatabaseSupplier.YML.supply(null);
    private ComponentReader storageName;
    private ComponentReader inventorySmallMessage;
    private List<ComponentReader> storageItemSuffix = Collections.emptyList();
    private TimeFormat timeFormat = new TimeFormat("%dd","%dh","%dm","%ds");

    public void setCurrentDatabase(@NotNull Database currentDatabase) {
        this.currentDatabase.close();
        this.currentDatabase = Objects.requireNonNull(currentDatabase);
    }

    public @NotNull ComponentReader getStorageName() {
        return storageName;
    }

    public void setStorageName(@NotNull ComponentReader storageName) {
        this.storageName = Objects.requireNonNull(storageName);
    }

    public void setAutoSaveTime(long autoSaveTime) {
        this.autoSaveTime = Math.max(autoSaveTime,1) * 20;
    }

    public @NotNull List<ComponentReader> getStorageItemSuffix() {
        return storageItemSuffix;
    }

    public void setStorageItemSuffix(@NotNull List<ComponentReader> storageItemSuffix) {
        this.storageItemSuffix = Objects.requireNonNull(storageItemSuffix);
    }

    public @NotNull Database getCurrentDatabase() {
        return currentDatabase;
    }

    public long getAutoSaveTime() {
        return autoSaveTime;
    }

    public @NotNull TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(@NotNull TimeFormat format) {
        this.timeFormat = Objects.requireNonNull(format);
    }

    public @NotNull ComponentReader getInventorySmallMessage() {
        return inventorySmallMessage;
    }

    public void setInventorySmallMessage(@NotNull ComponentReader inventorySmallMessage) {
        this.inventorySmallMessage = Objects.requireNonNull(inventorySmallMessage);
    }
}
