package kor.toxicity.toxicitylibs.api.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandAPIExecutor {
    void execute(@NotNull CommandAPI api, @NotNull CommandSender sender, @NotNull String[] args);
}
