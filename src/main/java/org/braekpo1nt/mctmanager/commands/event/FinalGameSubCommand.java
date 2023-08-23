package org.braekpo1nt.mctmanager.commands.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FinalGameSubCommand extends CommandManager {
    
    public FinalGameSubCommand(GameManager gameManager) {
        subCommands.put("start", new TabExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /mct event finalgame start <first> <second>")
                            .color(NamedTextColor.RED));
                    return true;
                }
                String firstTeam = args[0];
                String secondTeam = args[1];
                if (firstTeam.equals(secondTeam)) {
                    sender.sendMessage(Component.text("must be two different teams")
                            .color(NamedTextColor.RED));
                    return true;
                }
                if (!gameManager.hasTeam(firstTeam)) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text(firstTeam)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid team name"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                if (!gameManager.hasTeam(secondTeam)) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text(secondTeam)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid team name"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                gameManager.getEventManager().startColossalColosseum(sender, firstTeam, secondTeam);
                return true;
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length >= 1 && args.length <= 2) {
                    return gameManager.getTeamNames().stream().sorted().toList();
                }
                return Collections.emptyList();
            }
        });
        subCommands.put("stop", (sender, command, label, args) -> {
            if (args.length != 0) {
                sender.sendMessage(Component.text("Usage: /mct event finalgame stop")
                        .color(NamedTextColor.RED));
                return true;
            }
            gameManager.getEventManager().stopColossalColosseum(sender);
            return true;
        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct event finalgame <options>");
    }
    
}
