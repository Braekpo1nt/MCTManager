package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles stopping the current game
 */
public class StopSubCommand implements TabSubCommand {
    
    private final GameManager gameManager;
    
    public StopSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameManager.gameIsRunning()) {
            return CommandResult.failed(Component.text("No game is running.")
                    .color(NamedTextColor.RED));
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
        return CommandResult.failed("Usage: /mct game stop [true|false]");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("true", "false");
        }
        
        return Collections.emptyList();
    }
}
