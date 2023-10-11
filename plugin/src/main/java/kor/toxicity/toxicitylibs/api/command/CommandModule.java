package kor.toxicity.toxicitylibs.api.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandModule {
    String[] aliases();
    String description();
    String usage();
    SenderType[] allowedSender();
    boolean opOnly();
    String[] permission();
    int length();
    void execute(CommandAPI api, CommandSender sender, String[] args);
    @Nullable
    List<String> tabComplete(CommandAPI api, CommandSender sender, String[] args);
}
