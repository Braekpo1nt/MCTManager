package org.braekpo1nt.mctmanager.commands.manager.brigadier.hub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public HubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("hub")
                .executes(BrigadierAdapters.wraps(this::executeTpToHub))
                .then(Commands.literal("menu")
                        .executes(BrigadierAdapters.wraps(this::executeMenu))
                )
                ;
    }
    
    private @NotNull CommandResult executeTpToHub(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("This command can only be run by a player");
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant != null) {
            return gameManager.returnParticipantToHub(participant.getUniqueId());
        }
        if (gameManager.isAdmin(player.getUniqueId())) {
            return gameManager.returnAdminToHub(player);
        }
        return CommandResult.failure("Only participants and admins can use this command");
    }
    
    private @NotNull CommandResult executeMenu(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("This command can only be run by a player");
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant == null) {
            return CommandResult.failure("This command can only be run by a participant");
        }
        return gameManager.openHubMenu(participant.getUniqueId());
    }
}
