package org.braekpo1nt.mctmanager.commands.mct.tablist;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TabListCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public TabListCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("tablist")
                .then(Permissioned.literal("show")
                        .executes(BrigadierAdapters.wraps(ctx -> executeTabListToggle(ctx, true)))
                )
                .then(Permissioned.literal("hide")
                        .executes(BrigadierAdapters.wraps(ctx -> executeTabListToggle(ctx, false)))
                )
                ;
    }
    
    private @NotNull CommandResult executeTabListToggle(@NotNull CommandContext<CommandSourceStack> ctx, boolean show) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("This command can only be run by a player");
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant == null
                && !gameManager.isAdmin(player.getUniqueId())) {
            return CommandResult.failure("This command can only be run by a participant or an admin");
        }
        gameManager.setTabListVisibility(player.getUniqueId(), show);
        return CommandResult.success(Component.text("The custom Tab List is hidden"));
    }
}
