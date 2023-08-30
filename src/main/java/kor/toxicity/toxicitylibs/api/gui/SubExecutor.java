package kor.toxicity.toxicitylibs.api.gui;

import org.bukkit.inventory.Inventory;

import java.util.Objects;

public abstract class SubExecutor extends GuiExecutor {
    private boolean safeEnd;

    public boolean isSafeEnd() {
        return safeEnd;
    }

    public void setSafeEnd(boolean safeEnd) {
        this.safeEnd = safeEnd;
    }

    public SubExecutor(Inventory inventory, GuiExecutor parent) {
        super(inventory, Objects.requireNonNull(parent));
    }
}
