package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.team.preset.PresetCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.team.score.ScoreCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class TeamCommand implements BrigadierSubCommand {
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public TeamCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("team")
                .then(new AddSubCommand(gameManager).create())
                .then(new JoinSubCommand(plugin, gameManager).create())
                .then(new LeaveSubCommand(plugin, gameManager).create())
                .then(new ListSubCommand(plugin, gameManager).create())
                .then(new RemoveSubCommand(gameManager).create())
                .then(new ScoreCommand(gameManager).create())
                .then(new PresetCommand(plugin, gameManager).create())
                ;
    }
}
