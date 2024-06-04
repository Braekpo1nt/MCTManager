package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ModifyCommand extends CommandManager {
    
    public ModifyCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("maxgames") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!gameManager.getEventManager().eventIsActive()) {
                    return CommandResult.failure(Component.text("There is no event running."));
                }
                
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<newCount>"));
                }
                
                String newCountString = args[0];
                if (!CommandUtils.isInteger(newCountString)) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(newCountString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid integer")));
                }
                
                int newCount = Integer.parseInt(newCountString);
                gameManager.getEventManager().setMaxGames(sender, newCount);
                return CommandResult.success();
            }
            
            @Override
            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
    }
}
