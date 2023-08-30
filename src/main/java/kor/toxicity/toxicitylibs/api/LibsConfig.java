package kor.toxicity.toxicitylibs.api;

import kor.toxicity.toxicitylibs.util.database.Database;
import kor.toxicity.toxicitylibs.util.database.DatabaseSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum LibsConfig {
    INSTANCE
    ;
    private long autoSaveTime = 300L;
    private Database currentDatabase = DatabaseSupplier.YML.supply(null);

    public void setCurrentDatabase(@NotNull Database currentDatabase) {
        this.currentDatabase = Objects.requireNonNull(currentDatabase);
    }

    public void setAutoSaveTime(long autoSaveTime) {
        this.autoSaveTime = Math.max(autoSaveTime,1);
    }

    public @NotNull Database getCurrentDatabase() {
        return currentDatabase;
    }

    public long getAutoSaveTime() {
        return autoSaveTime;
    }
}
