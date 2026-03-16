package org.braekpo1nt.mctmanager.commands.database.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;

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
                        .then(Permissioned.literal("remove")
                                .then(Permissioned.argument("teamId", StringArgumentType.word())
                                        .executes(BrigadierAdapters.wraps(this::executeRemoveMaintenance))
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
        String teamId = ctx.getArgument("teamId", String.class);
        String displayName = ctx.getArgument("displayName", String.class);
        String color = ctx.getArgument("color", String.class);
        return CommandResult.async(plugin, "Adding team", () -> {
            try {
                gameManager.getGameStateService().addTeam(MaintenanceTeam.builder()
                        .teamId(teamId)
                        .displayName(displayName)
                        .color(color)
                        .modifiedAt(new Date())
                        .build());
                return CommandResult.success(Component.empty()
                        .append(Component.text("Added "))
                        .append(Component.text(teamId))
                        .append(Component.text(" to maintenance mode"))
                );
            } catch (SQLException e) {
                return CommandResult.sqlException("add team to maintenance database", e);
            }
        });
    }
    
    private @NotNull CommandResult executeRemoveMaintenance(CommandContext<CommandSourceStack> ctx) {
        String teamId = ctx.getArgument("teamId", String.class);
        return CommandResult.async(plugin, "Removing team", () -> {
            try {
                boolean existed = gameManager.getGameStateService().deleteMaintenanceTeam(teamId);
                if (!existed) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text("Could not find team with id "))
                            .append(Component.text(teamId))
                    );
                }
                return CommandResult.success(Component.empty()
                        .append(Component.text("Removed team "))
                        .append(Component.text(teamId))
                );
            } catch (SQLException e) {
                return CommandResult.sqlException("remove a team from maintenance database", e);
            }
        });
    }
    
    private @NotNull CommandResult executeJoinMaintenance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String teamId = ctx.getArgument("teamId", String.class);
        String member = ctx.getArgument("member", String.class);
        if (gameManager.getMode().equals(Mode.MAINTENANCE)) {
            return GameManagerUtils.joinParticipant(plugin, gameManager, member, teamId);
        }
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(member);
        return CommandResult.async(plugin, "Adding participant", () -> {
            try {
                gameManager.getGameStateService().addParticipant(
                        MaintenanceParticipantEntity.builder()
                                .teamId(teamId)
                                .participantUUID(offlinePlayer.getUniqueId().toString())
                                .build(),
                        member
                );
                return CommandResult.success(Component.empty()
                        .append(Component.text("Added "))
                        .append(Component.text(member))
                        .append(Component.text(" to "))
                        .append(Component.text(teamId))
                );
            } catch (SQLException e) {
                return CommandResult.sqlException("add a participant to maintenance database", e);
            }
        });
    }
    
    private @NotNull CommandResult executeAddPractice(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return CommandResult.success();
    }
    
    private @NotNull CommandResult executeJoinPractice(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return CommandResult.success();
    }
    
    private @NotNull CommandResult executeAddEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return CommandResult.success();
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
