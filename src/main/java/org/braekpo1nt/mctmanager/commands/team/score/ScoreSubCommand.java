package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
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

public class ScoreSubCommand extends CommandManager {
    
    public ScoreSubCommand(GameManager gameManager) {
        subCommands.put("add", new ScoreAddSubCommand(gameManager));
        subCommands.put("subtract", new ScoreSubtractSubCommand(gameManager));
        subCommands.put("set", new ScoreSetSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("/mct team score <options>");
    }
}
