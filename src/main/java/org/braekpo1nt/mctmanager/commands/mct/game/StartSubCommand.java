package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles starting games
 */
public class StartSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public StartSubCommand(GameManager gameManager, String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("<game>"));
        }
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.text(gameID)
                    .append(Component.text(" is not a valid game"))
                    .color(NamedTextColor.RED));
        }
        if (gameManager.getEventManager().eventIsActive()) {
            return CommandResult.failure(Component.text("Can't manually start a game while an event is active.")
                    .color(NamedTextColor.RED));
        }
        gameManager.startGame(gameType, sender);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return GameType.GAME_IDS.keySet().stream().sorted().toList();
        }
        return Collections.emptyList();
    }
}
