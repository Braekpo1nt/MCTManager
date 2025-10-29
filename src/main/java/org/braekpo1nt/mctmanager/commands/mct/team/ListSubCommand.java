package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public ListSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[true|false]"));
        }
        Component teamDisplay = GameManagerUtils.getTeamDisplay(gameManager);
        if (args.length == 0) {
            sender.sendMessage(teamDisplay);
            return CommandResult.success();
        }
        String displayToAll = args[0];
        switch (displayToAll) {
            case "true" -> {
                Bukkit.getServer().sendMessage(teamDisplay);
            }
            case "false" -> {
                sender.sendMessage(teamDisplay);
            }
            default -> {
                sender.sendMessage(Component.empty()
                        .append(Component.text(displayToAll)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized option"))
                        .color(NamedTextColor.RED));
            }
        }
        
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("true", "false");
        }
        return Collections.emptyList();
    }
}
