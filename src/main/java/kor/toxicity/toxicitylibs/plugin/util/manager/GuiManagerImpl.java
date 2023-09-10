package kor.toxicity.toxicitylibs.plugin.util.manager;

import kor.toxicity.toxicitylibs.api.ToxicityPlugin;
import kor.toxicity.toxicitylibs.api.gui.*;
import kor.toxicity.toxicitylibs.api.manager.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GuiManagerImpl implements GuiManager {
    public static final GuiManager MANAGER = new GuiManagerImpl();
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private boolean started = false;
    private GuiManagerImpl() {
    }
    @Override
    public void openGui(Player player, GuiExecutor executor, GuiType type) {
        var holder = getHolder(player);
        if (holder != null && holder.getExecutor() instanceof SubExecutor subExecutor) subExecutor.setSafeEnd(true);
        var select = holder != null ? type.build(holder.getExecutor(),executor) : executor;
        select.initialize();
        player.openInventory(select.getHolder().getInventory());
    }

    @Override
    public void start(ToxicityPlugin plugin) {
        if (started) return;
        started = true;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void click(InventoryClickEvent event) {
                var clicked = event.getClickedInventory();
                if (clicked == null) return;
                var item = event.getCurrentItem();
                if (item == null) item = AIR;
                if (event.getView().getTopInventory().getHolder() instanceof GuiHolder holder) {
                    MouseButton button;
                    if (event.isLeftClick()) {
                        button = event.isShiftClick() ? MouseButton.SHIFT_LEFT : MouseButton.LEFT;
                    } else if (event.isRightClick()) {
                        button = event.isShiftClick() ? MouseButton.SHIFT_RIGHT : MouseButton.RIGHT;
                    } else {
                        button = MouseButton.OTHER;
                    }
                    event.setCancelled(holder.getExecutor().onClick(
                            clicked.equals(event.getWhoClicked().getInventory()),
                            item,
                            event.getSlot(),
                            button
                    ));
                }
            }
            @EventHandler
            public void close(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof Player player && event.getView().getTopInventory().getHolder() instanceof GuiHolder holder) {
                    var executor = holder.getExecutor();
                    executor.onEnd();
                    if (executor instanceof SubExecutor subExecutor && !subExecutor.isSafeEnd()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(Objects.requireNonNull(subExecutor.getParent()).getHolder().getInventory()), 1);
                    }
                    if (executor.getParent() instanceof SubExecutor subExecutor) subExecutor.setSafeEnd(false);
                }
            }
        },plugin);
    }

    @Override
    public void reload(ToxicityPlugin plugin) {
        Bukkit.getScheduler().runTask(plugin,GuiManagerImpl::closeInventory);
    }

    @Override
    public void end(ToxicityPlugin plugin) {
        closeInventory();
    }
    private static void closeInventory() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            var holder = getHolder(onlinePlayer);
            if (holder != null) onlinePlayer.closeInventory();
        }
    }
    private static GuiHolder getHolder(Player player) {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder holder ? holder : null;
    }
}
