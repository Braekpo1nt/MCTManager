package org.braekpo1nt.mctmanager.commands.mct;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.mct.debug.DebugCommand;
import org.braekpo1nt.mctmanager.commands.mct.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.mct.event.EventCommand;
import org.braekpo1nt.mctmanager.commands.mct.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.mct.hub.HubCommand;
import org.braekpo1nt.mctmanager.commands.mct.mode.ModeCommand;
import org.braekpo1nt.mctmanager.commands.mct.option.OptionSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.score.ScoreCommand;
import org.braekpo1nt.mctmanager.commands.mct.tablist.TabListCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.TeamCommand;
import org.braekpo1nt.mctmanager.commands.mct.timer.TimerCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * The super command for all MCT related commands.
 * Handles all sub-commands which start with /mct ...
 */
public class MCTCommand extends MasterCommandManager {
    
    public MCTCommand(@NotNull Main plugin, @NotNull GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        super(plugin, "mct");
        CommandUtils.refreshGameConfigs(plugin);
        addSubCommand(new GameCommand(plugin, gameManager));
        addSubCommand(new EditCommand(plugin, gameManager, "edit"));
        addSubCommand(new HubCommand(gameManager, "hub"));
        addSubCommand(new OptionSubCommand(blockEffectsListener, "option"));
        addSubCommand(new ModeCommand(gameManager, "mode"));
        addSubCommand(new TeamCommand(plugin, gameManager, "team"));
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
                return gameManager.loadGameState();
            }
        });
        addSubCommand(new ScoreCommand(plugin, gameManager, "score"));
        addSubCommand(new TimerCommand(gameManager, "timer"));
        addSubCommand(new TabListCommand(gameManager, "tablist"));
        addSubCommand(new DebugCommand("debug"));
        onInit(plugin.getServer().getPluginManager());
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg(Permissible permissible) {
        return new Usage("<options>");
    }
}
