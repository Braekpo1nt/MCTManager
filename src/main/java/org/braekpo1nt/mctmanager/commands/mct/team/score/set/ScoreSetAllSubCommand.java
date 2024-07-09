package org.braekpo1nt.mctmanager.commands.mct.team.score.set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ScoreSetAllSubCommand extends SubCommand {
    
    private final GameManager gameManager;
    
    public ScoreSetAllSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("<score>"));
        }
        
        String scoreString = args[0];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(scoreString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int score = Integer.parseInt(scoreString);
        if (score < 0) {
            return CommandResult.failure(Component.text("Value must be positive"));
        }
        gameManager.setScoreAll(score);
        
        return CommandResult.success(Component.empty()
                .append(Component.text("All team and participant scores have been set to "))
                .append(Component.text(score)));
    }
}
