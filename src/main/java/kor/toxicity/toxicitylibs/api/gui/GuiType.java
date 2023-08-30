package kor.toxicity.toxicitylibs.api.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum GuiType {
    DEFAULT {
        @Override
        public @NotNull GuiExecutor build(@NotNull GuiExecutor parent, @NotNull GuiExecutor now) {
            return new GuiExecutor(now.getInventory(),parent) {
                @Override
                public boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button) {
                    return now.onClick(isPlayerInventory, clickedItem, clickedSlot, button);
                }

                @Override
                public void initialize() {
                    now.initialize();
                }

                @Override
                public void onEnd() {
                    now.onEnd();
                }
            };
        }
    },
    SUB {
        @Override
        public @NotNull GuiExecutor build(@NotNull GuiExecutor parent, @NotNull GuiExecutor now) {
            return new SubExecutor(now.getInventory(),parent) {
                @Override
                public boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button) {
                    return now.onClick(isPlayerInventory, clickedItem, clickedSlot, button);
                }

                @Override
                public void initialize() {
                    now.initialize();
                }

                @Override
                public void onEnd() {
                    now.onEnd();
                }
            };
        }
    },
    ;
    public abstract @NotNull GuiExecutor build(@NotNull GuiExecutor parent, @NotNull GuiExecutor now);
}
