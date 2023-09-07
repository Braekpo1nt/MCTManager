package org.braekpo1nt.mctmanager.commands.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoteSubCommand extends CommandManager {
    
    public VoteSubCommand(GameManager gameManager) {
        subCommands.put("add", new TabExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    sender.sendMessage(Component.text("Usage: /mct event vote add <game>")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                String gameString = args[0];
                GameType gameToAdd = GameType.fromID(gameString);
                if (gameToAdd == null) {
                    sender.sendMessage(Component.text("")
                            .append(Component.text(gameString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized game"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                gameManager.getEventManager().addGameToVotingPool(sender, gameToAdd);
                return true;
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return GameType.GAME_IDS.keySet().stream().sorted().toList();
                }
                return null;
            }
        });
        subCommands.put("remove", new TabExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    sender.sendMessage(Component.text("Usage: /mct event vote remove <game>")
                            .color(NamedTextColor.RED));
                    return true;
                }
    
                String gameString = args[0];
                GameType gameToRemove = GameType.fromID(gameString);
                if (gameToRemove == null) {
                    sender.sendMessage(Component.text("")
                            .append(Component.text(gameString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized game"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                gameManager.getEventManager().removeGameFromVotingPool(sender, gameToRemove);
                return true;
            }
    
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return GameType.GAME_IDS.keySet().stream().sorted().toList();
                }
                return null;
            }
        });
    }
    
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct event vote <arguments>")
                .color(NamedTextColor.RED);
    }
}
