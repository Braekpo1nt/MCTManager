package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EventUndoSubCommand extends SubCommand {
    
    private final GameManager gameManager;
    
    public EventUndoSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 3) {
            return CommandResult.failure(getUsage().of("<game>").of("<configFile.json>").of("<iteration>"));
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(gameID)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid game")));
        }
        
        String configFile = args[1];
        
        String iterationNumberString = args[2];
        if (!CommandUtils.isInteger(iterationNumberString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(iterationNumberString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int iterationNumber = Integer.parseInt(iterationNumberString);
        return gameManager.undoGame(new GameInstanceId(gameType, configFile), iterationNumber - 1);
    }
}
