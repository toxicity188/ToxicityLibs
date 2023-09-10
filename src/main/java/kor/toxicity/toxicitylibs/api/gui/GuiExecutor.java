package kor.toxicity.toxicitylibs.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiExecutor {

    private final GuiExecutor parent;
    private GuiHolder holder;
    public GuiExecutor(int size, Component name) {
        this(size,name,null);
    }
    public GuiExecutor(int size, Component name, GuiExecutor parent) {
        this.holder = new GuiHolder(size, name);
        holder.setExecutor(this);
        this.parent = parent;
    }

    public GuiExecutor(GuiHolder holder, GuiExecutor parent) {
        this.holder = holder;
        holder.setExecutor(this);
        this.parent = parent;
    }

    public GuiHolder getHolder() {
        return holder;
    }
    public Inventory getInventory() {
        return holder.getInventory();
    }

    public void setHolder(GuiHolder holder) {
        this.holder = holder;
    }

    public final @Nullable GuiExecutor getParent() {
        return parent;
    }


    public void initialize() {

    }
    public abstract boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button);
    public void onEnd() {

    }
}
