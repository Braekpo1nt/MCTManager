package org.braekpo1nt.mctmanager.commands.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public EventSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(Component.text("Usage: /mct event <options>"));
            return true;
        }
        String argument = args[0];
        switch (argument) {
            case "start" -> {
                int maxGames = 6;
                if (args.length == 2) {
                    String maxGamesString = args[1];
                    if (!CommandUtils.isInteger(maxGamesString)) {
                        sender.sendMessage(Component.empty()
                                .append(Component.text(maxGamesString)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is not an integer"))
                                .color(NamedTextColor.RED));
                        return true;
                    }
                    maxGames = Integer.parseInt(maxGamesString);
                }
                gameManager.startEvent(sender, maxGames);
            }
            case "stop" -> {
                if (!gameManager.eventIsActive()) {
                    sender.sendMessage(Component.text("There is no event running.")
                            .color(NamedTextColor.RED));
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage(Component.text("Are you sure? Type ")
                            .append(Component.empty()
                                    .append(Component.text("/mct event stop "))
                                    .append(Component.text("confirm")
                                            .decorate(TextDecoration.BOLD))
                                    .decorate(TextDecoration.ITALIC))
                            .append(Component.text(" to confirm."))
                            .color(NamedTextColor.YELLOW));
                    return true;
                }
                String confirmString = args[1];
                if (!confirmString.equals("confirm")) {
                    sender.sendMessage(Component.empty()
                                    .append(Component.text(confirmString))
                                    .append(Component.text(" is not a recognized option."))
                                    .color(NamedTextColor.RED));
                    return true;
                }
                gameManager.stopEvent(sender);
            }
            case "pause" -> {
                gameManager.pauseEvent(sender);
            }
            case "resume" -> {
                gameManager.resumeEvent(sender);
            }
            default -> {
                sender.sendMessage(Component.empty()
                        .append(Component.text("Unrecognized option "))
                        .append(Component.text(argument)
                                .decorate(TextDecoration.BOLD))
                        .color(NamedTextColor.RED)
                );
                return true;
            }
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("pause", "resume", "start", "stop");
        }
        return Collections.emptyList();
    }
}
