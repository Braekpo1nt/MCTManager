package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.UsageCommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles stopping the current game
 */
public class StopSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public StopSubCommand(GameManager gameManager, String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameManager.gameIsRunning()) {
            return CommandResult.failed("No game is running.");
        }
        if (args.length == 0) {
            gameManager.manuallyStopGame(true);
            return CommandResult.succeeded();
        }
        if (args.length == 1) {
            String shouldTeleport = args[0];
            switch (shouldTeleport) {
                case "true" -> {
                    gameManager.manuallyStopGame(true);
                    return CommandResult.succeeded();
                }
                case "false" -> {
                    if (gameManager.getEventManager().eventIsActive()) {
                        return CommandResult.failed(Component.empty()
                                .append(Component.text("Can't skip teleport to hub while an event is running. Use "))
                                .append(Component.text("/mct game stop [true]")
                                        .clickEvent(ClickEvent.suggestCommand("/mct game stop"))
                                        .decorate(TextDecoration.BOLD)));
                    }
                    sender.sendMessage("Skipping teleport to hub.");
                    gameManager.manuallyStopGame(false);
                    return CommandResult.succeeded();
                }
                default -> {
                    return CommandResult.failed(Component.text(shouldTeleport)
                            .append(Component.text(" is not a recognized option")));
                }
            }
        } 
        return getUsage().with("[true|false]");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("true", "false");
        }
        
        return Collections.emptyList();
    }
}
