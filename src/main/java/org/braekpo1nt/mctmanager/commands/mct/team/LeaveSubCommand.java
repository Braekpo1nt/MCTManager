package org.braekpo1nt.mctmanager.commands.mct.team;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.jetbrains.annotations.NotNull;

public class LeaveSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public LeaveSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("leave")
                .then(Permissioned.argument("participant", new OfflineParticipantArgumentType(gameManager))
                        .executes(BrigadierAdapters.wraps(this::executeLeave))
                )
                ;
    }
    
    private @NotNull CommandResult executeLeave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OfflineParticipant offlineParticipant = ctx.getArgument("participant", OfflineParticipantResolver.class)
                .resolve();
        return gameManager.leaveParticipant(offlineParticipant);
    }
}
