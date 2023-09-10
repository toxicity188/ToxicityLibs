package kor.toxicity.toxicitylibs.api.gui;

import java.util.Objects;

public abstract class SubExecutor extends GuiExecutor {
    private boolean safeEnd = false;

    public boolean isSafeEnd() {
        return safeEnd;
    }

    public void setSafeEnd(boolean safeEnd) {
        this.safeEnd = safeEnd;
    }

    public SubExecutor(GuiHolder inventory, GuiExecutor parent) {
        super(inventory, Objects.requireNonNull(parent));
    }
}
