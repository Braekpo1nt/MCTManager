package org.braekpo1nt.mctmanager.commands.mct.hub;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class HubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public HubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("hub")
                .executes(BrigadierAdapters.wrapsFuture(this::executeTpToHub))
                .then(Permissioned.literal("menu")
                        .executes(BrigadierAdapters.wraps(this::executeMenu))
                )
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeTpToHub(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("This command can only be run by a player").asFuture();
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant != null) {
            return gameManager.returnParticipantToHub(participant.getUniqueId());
        }
        if (gameManager.isAdmin(player.getUniqueId())) {
            return gameManager.returnAdminToHub(player).asFuture();
        }
        return CommandResult.failure("Only participants and admins can use this command").asFuture();
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
