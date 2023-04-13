package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.commands.team.score.ScoreSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamSubCommand extends CommandManager {
    
    public TeamSubCommand(GameManager gameManager) {
        subCommands.put("add", new AddSubCommand(gameManager));
        subCommands.put("join", new JoinSubCommand(gameManager));
        subCommands.put("leave", new LeaveSubCommand(gameManager));
        subCommands.put("list", new ListSubCommand(gameManager));
        subCommands.put("remove", new RemoveSubCommand(gameManager));
        subCommands.put("score", new ScoreSubCommand(gameManager));
    }
    
    @Override
    protected Component getUsageMessage() {
        return Component.text("Usage: /mct team <options>");
    }
}
