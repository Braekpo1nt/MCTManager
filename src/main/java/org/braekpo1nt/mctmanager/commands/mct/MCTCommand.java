package org.braekpo1nt.mctmanager.commands.mct;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.mct.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.mct.event.EventCommand;
import org.braekpo1nt.mctmanager.commands.mct.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.TeamCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

/**
 * The super command for all MCT related commands. 
 * Handles all sub-commands which start with /mct ...
 */
public class MCTCommand extends MasterCommandManager {
    
    public MCTCommand(Main plugin, GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        super(plugin, "mct");
        addSubCommand(new GameCommand(gameManager));
        addSubCommand(new EditCommand(gameManager, "edit"));
        addSubCommand(new OptionSubCommand(gameManager, blockEffectsListener, "option"));
        addSubCommand(new TeamCommand(gameManager, "team"));
        addSubCommand(new AdminCommand(gameManager));
        addSubCommand(new EventCommand(gameManager, "event"));
        addSubCommand(new SubCommand("save") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.saveGameState();
                return CommandResult.success(Component.text("Saved game state"));
            }
        });
        addSubCommand(new SubCommand("load") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.loadGameState();
                return CommandResult.success(Component.text("Loaded gameState.json"));
            }
        });
        registerPermissions(plugin.getServer().getPluginManager());
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg() {
        return new Usage("<options>");
    }
}
