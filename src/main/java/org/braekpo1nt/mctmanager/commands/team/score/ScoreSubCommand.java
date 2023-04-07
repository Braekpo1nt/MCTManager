package org.braekpo1nt.mctmanager.commands.team.score;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreSubCommand implements TabExecutor {
    private final GameManager gameManager;
    private final Map<String, CommandExecutor> subCommands = new HashMap<>();

    public ScoreSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
        subCommands.put("add", ScoreAddSubCommand());
        subCommands.put("add", ScoreSubtractSubCommand());
        subCommands.put("add", ScoreSetSubCommand());
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
