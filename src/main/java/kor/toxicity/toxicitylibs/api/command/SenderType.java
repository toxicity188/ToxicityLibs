package kor.toxicity.toxicitylibs.util.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public enum SenderType {
    PLAYER(Player.class),
    CONSOLE(ConsoleCommandSender.class)
    ;
    private final Class<? extends CommandSender> sender;

    public boolean match(Class<? extends CommandSender> sender) {
        return this.sender.isAssignableFrom(sender);
    }
    SenderType(Class<? extends CommandSender> sender) {
        this.sender = sender;
    }
}
