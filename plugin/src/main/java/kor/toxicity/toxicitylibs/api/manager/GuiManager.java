package kor.toxicity.toxicitylibs.api.manager;

import kor.toxicity.toxicitylibs.api.gui.GuiExecutor;
import kor.toxicity.toxicitylibs.api.gui.GuiType;
import org.bukkit.entity.Player;

public interface GuiManager extends ToxicityPluginManager {
    void openGui(Player player, GuiExecutor executor, GuiType type);
}
