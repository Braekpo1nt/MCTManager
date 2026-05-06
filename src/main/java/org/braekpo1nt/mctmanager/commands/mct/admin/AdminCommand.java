package org.braekpo1nt.mctmanager.commands.mct.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineAdminArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineAdminListResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

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
                                .executes(BrigadierAdapters.wraps(this::executeAdd))
                        )
                )
                .then(Permissioned.literal("remove")
                        .then(Permissioned.argument("admin", new OfflineAdminArgumentType(gameManager))
                                .executes(BrigadierAdapters.wraps(this::executeRemove))
                        )
                )
                .then(Permissioned.literal("test")
                        .executes(ctx -> {
                            throw ERROR_NOT_AN_ADMIN.create("TeSt");
                        })
                )
                ;
    }
    
    private @NotNull CommandResult executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OfflineAdminListResolver adminResolver = ctx.getArgument("admin", OfflineAdminListResolver.class);
        Collection<OfflinePlayer> offlineAdmins = adminResolver.resolve(ctx.getSource());
        List<CommandResult> results = new ArrayList<>(offlineAdmins.size());
        for (OfflinePlayer offlineAdmin : offlineAdmins) {
            CommandResult result = gameManager.removeAdmin(offlineAdmin, offlineAdmin.getName() != null ? offlineAdmin.getName() : offlineAdmin.getUniqueId().toString());
            results.add(result);
        }
        return new CompositeCommandResult(results);
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
