package org.braekpo1nt.mctmanager.commands.mct.team;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.PresetCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.ScoreCommand;
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
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("team")
                .then(new AddSubCommand(gameManager).create())
                .then(new JoinSubCommand(plugin, gameManager).create())
                .then(new LeaveSubCommand(gameManager).create())
                .then(new ListSubCommand(plugin, gameManager).create())
                .then(new RemoveSubCommand(gameManager).create())
                .then(new ScoreCommand(gameManager).create())
                .then(new PresetCommand(plugin, gameManager).create())
                ;
    }
}
