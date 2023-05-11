package org.braekpo1nt.mctmanager.commands.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EventSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public EventSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /mct event <options>"));
            return true;
        }
        String argument = args[0];
        switch (argument) {
            case "start" -> {
                gameManager.startEvent(sender);
            }
            case "stop" -> {
                gameManager.stopEvent();
            }
            case "pause" -> {
                gameManager.pauseEvent();
            }
            case "resume" -> {
                gameManager.resumeEvent();
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
        return Arrays.asList("pause", "resume", "start", "stop");
    }
}
