package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.debug.DebugCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.event.EventSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.hub.HubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.team.TeamCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class MCTCommand2 implements BrigadierCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public MCTCommand2(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("mct2")
                .then(new EventSubCommand(gameManager).create())
                .then(new TeamCommand(plugin, gameManager).create())
                .then(new AdminCommand(gameManager).create())
                .then(new DebugCommand().create())
                .then(new GameCommand(gameManager).create())
                .then(new HubCommand(gameManager).create())
                .build();
    }
}
