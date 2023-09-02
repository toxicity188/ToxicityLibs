package kor.toxicity.toxicitylibs.api.data;

import kor.toxicity.toxicitylibs.api.ToxicityConfig;
import kor.toxicity.toxicitylibs.api.gui.GuiExecutor;
import kor.toxicity.toxicitylibs.api.gui.GuiType;
import kor.toxicity.toxicitylibs.api.gui.MouseButton;
import kor.toxicity.toxicitylibs.plugin.ToxicityLibs;
import kor.toxicity.toxicitylibs.plugin.util.PlayerUtil;
import kor.toxicity.toxicitylibs.plugin.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayerData {
    private final List<ItemData> storageItem;
    private final OfflinePlayer player;
    public PlayerData(OfflinePlayer player, List<String> item) {
        this.player = player;
        storageItem = item.stream().map(ItemData::deserialize).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ItemData> getStorageItem() {
        return storageItem;
    }

    public String[] getSerializedStorageItem() {
        return storageItem.stream().map(ItemData::serialize).toArray(String[]::new);
    }

    public void openStorage(Player viewer) {
        var storageName = ToxicityConfig.INSTANCE.getStorageName();
        ToxicityLibs.getInstance().getGuiManager().openGui(viewer, new GuiExecutor(54,player instanceof Player online ? storageName.getResult(online) : storageName.getResult()) {
            @Override
            public void initialize() {
                var inv = getInventory();
                var i = 0;
                var now = LocalDateTime.now();
                for (int t = 0; t < 54; t++) inv.setItem(t,null);
                for (ItemData itemData : storageItem) {

                    var item = itemData.itemStack().clone();
                    var meta = item.getItemMeta();

                    if (itemData.leftTime() > 0) {
                        var display = meta.displayName();
                        if (display == null) display = Component.text(item.getType().toString());
                        meta.displayName(display.append(Component.space()).append(Component.text(StringUtil.parseTimeFormat(itemData.leftTime() - ChronoUnit.SECONDS.between(itemData.time(),now))).color(NamedTextColor.GRAY)));
                    }
                    var list = meta.lore();
                    if (list == null) list = new ArrayList<>();
                    list.addAll(ToxicityConfig.INSTANCE.getStorageItemSuffix().stream().map(s -> player instanceof Player online ? s.getResult(online) : s.getResult()).toList());

                    item.setItemMeta(meta);

                    inv.setItem(i, item);

                    if (++i == 54) break;
                }
            }

            @Override
            public boolean onClick(boolean isPlayerInventory, @NotNull ItemStack clickedItem, int clickedSlot, @NotNull MouseButton button) {
                if (isPlayerInventory) return true;
                if (clickedItem.getType() == Material.AIR) return true;
                var item = storageItem.get(clickedSlot).itemStack();

                if (PlayerUtil.storage(viewer,item) >= item.getAmount()) {
                    viewer.getInventory().addItem(item);
                    storageItem.remove(clickedSlot);
                    initialize();
                    viewer.updateInventory();
                } else {
                    viewer.sendMessage(ToxicityConfig.INSTANCE.getInventorySmallMessage().getResult(viewer));
                }
                return true;
            }
        }, GuiType.DEFAULT);
    }
}
