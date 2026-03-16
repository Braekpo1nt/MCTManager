package org.braekpo1nt.mctmanager.commands.database.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DatabaseTeamCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    public final @NotNull GameManager gameManager;
    
    public DatabaseTeamCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("team")
                .then(Permissioned.literal("maintenance")
                        .then(Permissioned.literal("add")
                                .then(Permissioned.argument("teamId", StringArgumentType.word())
                                        .then(Permissioned.argument("displayName", StringArgumentType.string())
                                                .then(Permissioned.argument("color", StringArgumentType.word())
                                                        .suggests(CommandUtils::suggestColor)
                                                        .executes(BrigadierAdapters.wraps(this::executeAddMaintenance))
                                                )
                                        )
                                )
                        )
                        .then(Permissioned.literal("join")
                                .then(Permissioned.argument("teamId", StringArgumentType.word())
                                        .then(Permissioned.argument("member", StringArgumentType.word())
                                                .executes(BrigadierAdapters.wraps(this::executeJoinMaintenance))
                                        )
                                )
                        )
                )
                .then(Permissioned.literal("practice")
                        .then(Permissioned.literal("add")
                                .then(Permissioned.argument("teamId", StringArgumentType.word())
                                        .then(Permissioned.argument("displayName", StringArgumentType.string())
                                                .then(Permissioned.argument("color", StringArgumentType.word())
                                                        .suggests(CommandUtils::suggestColor)
                                                        .executes(BrigadierAdapters.wraps(this::executeAddPractice))
                                                )
                                        )
                                )
                        )
                        .then(Permissioned.literal("join")
                                .then(Permissioned.argument("teamId", StringArgumentType.word())
                                        .then(Permissioned.argument("member", StringArgumentType.word())
                                                .executes(BrigadierAdapters.wraps(this::executeJoinPractice))
                                        )
                                )
                        )
                )
                .then(Permissioned.literal("event")
                        .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                                .then(Permissioned.literal("add")
                                        .then(Permissioned.argument("teamId", StringArgumentType.word())
                                                .then(Permissioned.argument("displayName", StringArgumentType.string())
                                                        .then(Permissioned.argument("color", StringArgumentType.word())
                                                                .suggests(CommandUtils::suggestColor)
                                                                .executes(BrigadierAdapters.wraps(this::executeAddEvent))
                                                        )
                                                )
                                        )
                                )
                                .then(Permissioned.literal("join")
                                        .then(Permissioned.argument("teamId", StringArgumentType.word())
                                                .then(Permissioned.argument("member", StringArgumentType.word())
                                                        .executes(BrigadierAdapters.wraps(this::executeJoinEvent))
                                                )
                                        )
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeAddMaintenance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return null;
    }
    
    private @NotNull CommandResult executeJoinMaintenance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return null;
    }
    
    private @NotNull CommandResult executeAddPractice(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return null;
    }
    
    private @NotNull CommandResult executeJoinPractice(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return null;
    }
    
    private @NotNull CommandResult executeAddEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return null;
    }
    
    private @NotNull CommandResult executeJoinEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver resolver = ctx.getArgument("eventId", EventInfoResolver.class);
        EventInfo eventInfo;
        try {
            eventInfo = resolver.resolve();
        } catch (SQLException e) {
            return CommandResult.sqlException("join participant to event", e);
        }
        String teamId = ctx.getArgument("teamId", String.class);
        String member = ctx.getArgument("member", String.class);
        return GameManagerUtils.joinParticipantEvent(plugin, gameManager, member, teamId, eventInfo);
    }
}
