package org.braekpo1nt.mctmanager.commands.mct.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineAdminArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineAdminListResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AdminCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public AdminCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    private static final DynamicCommandExceptionType ERROR_NOT_AN_ADMIN = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" is not an admin"))
    ));
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("admin")
                .then(Permissioned.literal("add")
                        .then(Permissioned.argument("player", gameManager.getPlayerArgumentType())
                                .executes(BrigadierAdapters.wrapsFuture(this::executeAdd))
                        )
                )
                .then(Permissioned.literal("remove")
                        .then(Permissioned.argument("admin", new OfflineAdminArgumentType(gameManager))
                                .executes(BrigadierAdapters.wrapsFuture(this::executeRemove))
                        )
                )
                .then(Permissioned.literal("test")
                        .executes(ctx -> {
                            throw ERROR_NOT_AN_ADMIN.create("TeSt");
                        })
                )
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OfflineAdminListResolver adminResolver = ctx.getArgument("admin", OfflineAdminListResolver.class);
        Collection<OfflinePlayer> offlineAdmins = adminResolver.resolve(ctx.getSource());
        CompletableFuture<CommandResult> chain = CompletableFuture.completedFuture(CommandResult.success());
        for (OfflinePlayer offlineAdmin : offlineAdmins) {
            chain = CommandResult.appendAsync(
                    chain,
                    () -> gameManager.removeAdmin(offlineAdmin, offlineAdmin.getName() != null ? offlineAdmin.getName() : offlineAdmin.getUniqueId().toString()),
                    gameManager.getMainThreadExecutor()
            );
        }
        return chain;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeAdd(CommandContext<CommandSourceStack> ctx) {
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
        try {
            final Player newAdmin = targetResolver.resolve(ctx.getSource()).getFirst();
            if (!newAdmin.isOnline()) {
                return CommandResult.failure(Component.empty()
                        .append(newAdmin.displayName())
                        .append(Component.text(" is not online"))
                ).asFuture();
            }
            if (gameManager.isAdmin(newAdmin.getUniqueId())) {
                return CommandResult.success(Component.empty()
                        .append(newAdmin.displayName())
                        .append(Component.text(" is already an admin"))
                ).asFuture();
            }
            return gameManager.addAdmin(newAdmin);
        } catch (CommandSyntaxException e) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not find player"))
            ).asFuture();
        }
        
    }
}
