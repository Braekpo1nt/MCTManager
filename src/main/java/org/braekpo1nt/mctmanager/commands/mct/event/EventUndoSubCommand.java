package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventUndoSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public EventUndoSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<game>").of("<iteration>"));
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(gameID)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid game")));
        }
        String iterationNumberString = args[1];
        if (!CommandUtils.isInteger(iterationNumberString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(iterationNumberString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int iterationNumber = Integer.parseInt(iterationNumberString);
        gameManager.getEventManager().undoGame(sender, gameType, iterationNumber - 1);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return Collections.emptyList();
        }
        int iterations = gameManager.getEventManager().getGameIterations(gameType);
        if (iterations <= 0) {
            return Collections.emptyList();
        }
        return generateNumberList(iterations);
    }
    
    /**
     * @param n the number to generate numbers up to (must be at least 1 to get any entries)
     * @return a list containing the numbers 1 through n inclusive as Strings, in increasing order. If n is less than 1, an empty list is produced. 
     */
    public static List<String> generateNumberList(int n) {
        List<String> numbers = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            numbers.add(Integer.toString(i));
        }
        return numbers;
    }
}
