package org.braekpo1nt.mctmanager.commands.mct.score.all;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ScoreAllSubCommand extends SubCommand {
    
    private final GameManager gameManager;
    
    public ScoreAllSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            return CommandResult.failure(getUsage());
        }
        Component teamDisplay = GameManagerUtils.getTeamDisplay(gameManager);
        return CommandResult.success(teamDisplay);
    }
    
}
