package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class ModifySubCommand extends CommandManager {
    
    public ModifySubCommand(GameManager gameManager) {
        subCommands.put("maxgames", new TabExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!gameManager.getEventManager().eventIsActive()) {
                    sender.sendMessage(Component.text("There is no event running.")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                if (args.length != 1) {
                    sender.sendMessage(Component.text("Usage: /mct event maxgames <new count>")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                String newCountString = args[0];
                if (!CommandUtils.isInteger(newCountString)) {
                    sender.sendMessage(Component.text(newCountString)
                            .append(Component.text(" is not a valid integer"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                int newCount = Integer.parseInt(newCountString);
                gameManager.getEventManager().setMaxGames(sender, newCount);
                
                return true;
            }
            
            @Override
            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct event modify <options>");
    }
       
}
