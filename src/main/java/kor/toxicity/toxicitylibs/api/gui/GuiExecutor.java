package kor.toxicity.toxicitylibs.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiExecutor implements InventoryHolder {

    private final Inventory inventory;
    private final GuiExecutor parent;
    public GuiExecutor(int size, Component name) {
        this(size,name,null);
    }
    public GuiExecutor(int size, Component name, GuiExecutor parent) {
        this.inventory = Bukkit.createInventory(this,size,name);
        this.parent = parent;
    }

    public GuiExecutor(Inventory inventory, GuiExecutor parent) {
        this.inventory = inventory;
        this.parent = parent;
    }

    public final @Nullable GuiExecutor getParent() {
        return parent;
    }

    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    public void initialize() {

    }
    public abstract boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button);
    public void onEnd() {

    }
}
