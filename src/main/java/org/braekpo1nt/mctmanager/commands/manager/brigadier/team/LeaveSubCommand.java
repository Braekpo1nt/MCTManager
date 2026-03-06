package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("leave")
                .then(Commands.argument("member", new OfflineParticipantArgumentType(gameManager))
                        .executes(BrigadierAdapters.wraps(this::executeLeave))
                )
                ;
    }
    
    private @NotNull CommandResult executeLeave(CommandContext<CommandSourceStack> ctx) {
        OfflineParticipant offlineParticipant = ctx.getArgument("member", OfflineParticipant.class);
        return gameManager.leaveParticipant(offlineParticipant);
    }
}
