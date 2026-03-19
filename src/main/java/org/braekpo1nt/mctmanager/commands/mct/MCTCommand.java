package org.braekpo1nt.mctmanager.commands.mct;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.mct.debug.DebugCommand;
import org.braekpo1nt.mctmanager.commands.mct.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.mct.event.EventSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.mct.hub.HubCommand;
import org.braekpo1nt.mctmanager.commands.mct.mode.ModeCommand;
import org.braekpo1nt.mctmanager.commands.mct.option.OptionCommand;
import org.braekpo1nt.mctmanager.commands.mct.score.ScoreCommand;
import org.braekpo1nt.mctmanager.commands.mct.stats.StatsCommand;
import org.braekpo1nt.mctmanager.commands.mct.tablist.TabListCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.TeamCommand;
import org.braekpo1nt.mctmanager.commands.mct.timer.TimerCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.jetbrains.annotations.NotNull;

public class MCTCommand implements BrigadierCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    private final @NotNull BlockEffectsListener blockEffectsListener;
    
    public MCTCommand(@NotNull Main plugin, @NotNull GameManager gameManager, @NotNull BlockEffectsListener blockEffectsListener) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.blockEffectsListener = blockEffectsListener;
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Permissioned.literal("mct")
                .then(new EventSubCommand(gameManager, plugin).create())
                .then(new TeamCommand(plugin, gameManager).create())
                .then(new AdminCommand(gameManager).create())
                .then(new DebugCommand().create())
                .then(new GameCommand(gameManager).create())
                .then(new HubCommand(gameManager).create())
                .then(new ModeCommand(gameManager).create())
                .then(new OptionCommand(blockEffectsListener).create())
                .then(new EditCommand(gameManager).create())
                .then(new ScoreCommand(gameManager).create())
                .then(new TabListCommand(gameManager).create())
                .then(new TimerCommand(gameManager).create())
                .then(Permissioned.literal("load")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            gameManager.loadGameState(ctx.getSource().getSender());
                            return CommandResult.success(Component.text("Loading the game state..."));
                        }))
                )
                .then(new StatsCommand(gameManager.getGameStateService()).create())
                .permissionRoot("mctmanager")
                .build(plugin.getServer().getPluginManager());
    }
}
