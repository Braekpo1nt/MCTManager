package org.braekpo1nt.mctmanager.commands.mct.tablist;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TabListCommand extends CommandManager {
    
    
    public TabListCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new SubCommand("show") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    return CommandResult.failure("This command can only be run by a player");
                }
                if (!gameManager.isParticipant(player.getUniqueId()) 
                        && !gameManager.isAdmin(player.getUniqueId())) {
                    return CommandResult.failure("This command can only be run by a participant or an admin");
                }
                gameManager.setTabListVisibility(player.getUniqueId(), true);
                return CommandResult.success(Component.text("The custom Tab List is shown"));
            }
        });
        addSubCommand(new SubCommand("hide") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    return CommandResult.failure("This command can only be run by a player");
                }
                if (!gameManager.isParticipant(player.getUniqueId())
                        && !gameManager.isAdmin(player.getUniqueId())) {
                    return CommandResult.failure("This command can only be run by a participant or an admin");
                }
                gameManager.setTabListVisibility(player.getUniqueId(), false);
                return CommandResult.success(Component.text("The custom Tab List is hidden"));
            }
        });
    }
}
