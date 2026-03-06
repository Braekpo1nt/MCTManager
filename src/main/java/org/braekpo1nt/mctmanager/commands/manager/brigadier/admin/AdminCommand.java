package org.braekpo1nt.mctmanager.commands.manager.brigadier.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineAdminArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public AdminCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("admin")
                .then(Commands.literal("add")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(BrigadierAdapters.wraps(this::executeAdd))
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("admin", new OfflineAdminArgumentType(gameManager))
                                .executes(BrigadierAdapters.wraps(this::executeRemove))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeRemove(CommandContext<CommandSourceStack> ctx) {
        OfflinePlayer offlineAdmin = ctx.getArgument("admin", OfflinePlayer.class);
        return gameManager.removeAdmin(offlineAdmin, offlineAdmin.getName() != null ? offlineAdmin.getName() : offlineAdmin.getUniqueId().toString());
    }
    
    private @NotNull CommandResult executeAdd(CommandContext<CommandSourceStack> ctx) {
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
        try {
            final Player newAdmin = targetResolver.resolve(ctx.getSource()).getFirst();
            if (!newAdmin.isOnline()) {
                return CommandResult.failure(Component.empty()
                        .append(newAdmin.displayName())
                        .append(Component.text(" is not online")));
            }
            if (gameManager.isAdmin(newAdmin.getUniqueId())) {
                return CommandResult.success(Component.empty()
                        .append(newAdmin.displayName())
                        .append(Component.text(" is already an admin")));
            }
            return gameManager.addAdmin(newAdmin);
        } catch (CommandSyntaxException e) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not find player"))
            );
        }
        
    }
}
