package kor.toxicity.toxicitylibs.api.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface CommandAPITabCompleter {
    @Nullable List<String> complete(@NotNull CommandAPI api, @NotNull CommandSender sender, @NotNull String[] args);
}
