package org.braekpo1nt.mctmanager.commands.mct.team;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

public class RemoveSubCommand implements BrigadierSubCommand {
    
    
    private final @NotNull GameManager gameManager;
    
    public RemoveSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("remove")
                .then(Permissioned.argument("teamId", new TeamArgumentType(gameManager))
                        .executes(BrigadierAdapters.wraps(this::executeRemove))
                )
                ;
    }
    
    private @NotNull CommandResult executeRemove(CommandContext<CommandSourceStack> ctx) {
        Team teamToRemove = ctx.getArgument("teamId", Team.class);
        return gameManager.removeTeam(teamToRemove.getTeamId());
    }
}
