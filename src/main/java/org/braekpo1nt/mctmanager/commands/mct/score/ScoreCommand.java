package org.braekpo1nt.mctmanager.commands.mct.score;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.score.all.ScoreAllSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.score.player.ScorePlayerSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.score.team.ScoreTeamSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ScoreCommand extends CommandManager {
    
    private final ScorePlayerSubCommand playerSubCommand;
    
    public ScoreCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        playerSubCommand = new ScorePlayerSubCommand(gameManager, "player");
        addSubCommand(playerSubCommand);
        addSubCommand(new ScoreTeamSubCommand(gameManager, "team"));
        addSubCommand(new ScoreAllSubCommand(gameManager, "all"));
    }
    
    /**
     * Acts as an alias for the "player" subcommand with no arguments
     * @param sender the sender
     * @param command the command
     * @param label the label
     * @return a {@link CommandResult} detailing what happened
     */
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        return playerSubCommand.onSubCommand(sender, command, label, new String[0]);
    }
}
