package kor.toxicity.toxicitylibs.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GuiHolder implements InventoryHolder {
    private final Inventory inventory;
    public GuiHolder(int size, Component name) {
        this.inventory = Bukkit.createInventory(this, size, name);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setExecutor(GuiExecutor executor) {
        this.executor = executor;
    }

    public GuiExecutor getExecutor() {
        return executor;
    }

    private GuiExecutor executor;
}
