package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.debug.DebugCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.event.EventSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.hub.HubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.mode.ModeCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.option.OptionCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.score.ScoreCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.tablist.TabListCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.team.TeamCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.timer.TimerCommand;
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
                .then(new EventSubCommand(gameManager).create())
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
                .then(Commands.literal("load")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.loadGameState()))
                )
                .pluginManager(plugin.getServer().getPluginManager())
                .build();
    }
}
