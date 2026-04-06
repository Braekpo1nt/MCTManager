package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A utility command for testing various things, so I don't have to create a new command.
 */
public class MCTDebugCommand implements BrigadierCommand, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public MCTDebugCommand(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Permissioned.literal("mctdebug")
                .executes(BrigadierAdapters.wraps(this::executeDebug))
                .then(Permissioned.literal("all_players")
                        .then(Permissioned.literal("add")
                                .then(Permissioned.argument("uuid", ArgumentTypes.uuid())
                                        .then(Permissioned.argument("ign", StringArgumentType.word())
                                                .executes(BrigadierAdapters.wraps(this::executeAddUUIDAndIGN))
                                        )
                                )
                        )
                        .then(Permissioned.literal("migrate")
                                .then(Permissioned.literal("uuid")
                                        .then(Permissioned.argument("fromUUID", ArgumentTypes.uuid())
                                                .then(Permissioned.argument("toUUID", ArgumentTypes.uuid())
                                                        .then(Permissioned.argument("ign", StringArgumentType.word())
                                                                .executes(BrigadierAdapters.wraps(this::executeMigrateUUID))
                                                        )
                                                )
                                        )
                                )
                                .then(Permissioned.literal("ign")
                                        .then(Permissioned.argument("uuid", ArgumentTypes.uuid())
                                                .then(Permissioned.argument("newIGN", StringArgumentType.word())
                                                        .executes(BrigadierAdapters.wraps(this::executeMigrateIGN))
                                                )
                                        )
                                )
                        )
                )
                .then(Permissioned.literal("printGameState")
                        .executes(BrigadierAdapters.wraps(this::executePrintGameState))
                )
                .then(Commands.literal("rebuild")
                        .then(Commands.literal("event")
                                .then(Commands.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                                        .executes(BrigadierAdapters.wraps(this::executeRebuildEvent))
                                )
                        )
                        .then(Commands.literal("maintenance")
                                .executes(BrigadierAdapters.wraps(this::executeRebuildMaintenance))
                        )
                        .then(Commands.literal("practice")
                                .executes(BrigadierAdapters.wraps(this::executeRebuildPractice))
                        )
                )
                .then(Commands.literal("totals")
                        .then(Commands.argument("sessionId", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wraps(this::executeTotals))
                        )
                )
                .permissionRoot("mctmanager")
                .build(plugin.getServer().getPluginManager());
    }
    
    private @NotNull CommandResult executeMigrateUUID(CommandContext<CommandSourceStack> ctx) {
        String fromUuid = ctx.getArgument("fromUUID", UUID.class).toString();
        String toUuid = ctx.getArgument("toUUID", UUID.class).toString();
        String ign = ctx.getArgument("ign", String.class);
        try {
            gameManager.getGameStateService().migrateUUID(fromUuid, toUuid, ign);
        } catch (SQLException e) {
            return CommandResult.sqlException("migrate player uuid", e);
        }
        return CommandResult.success(Component.text("Migration successful"));
    }
    
    private @NotNull CommandResult executeMigrateIGN(CommandContext<CommandSourceStack> ctx) {
        String uuid = ctx.getArgument("uuid", UUID.class).toString();
        String toIGN = ctx.getArgument("newIGN", String.class);
        try {
            gameManager.getGameStateService().migrateIgn(uuid, toIGN);
        } catch (SQLException e) {
            return CommandResult.sqlException("migrate player uuid", e);
        }
        return CommandResult.success(Component.text("Migration successful"));
    }
    
    private @NotNull CommandResult executePrintGameState(CommandContext<CommandSourceStack> ctx) {
        Component gameState = gameManager.printGameState();
        plugin.getServer().getConsoleSender().sendMessage(gameState);
        return CommandResult.success(gameState);
    }
    
    private @NotNull CommandResult executeAddUUIDAndIGN(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String uuid = ctx.getArgument("uuid", UUID.class).toString();
        String ign = ctx.getArgument("ign", String.class);
        try {
            gameManager.getGameStateService().registerPlayer(uuid, ign);
        } catch (SQLException e) {
            return CommandResult.sqlException("registering uuid and ign", e);
        }
        return CommandResult.success(Component.text("Done"));
    }
    
    
    private @NotNull CommandResult executeTotals(CommandContext<CommandSourceStack> ctx) {
        int sessionId = ctx.getArgument("sessionId", Integer.class);
        CommandSender sender = ctx.getSource().getSender();
        try {
            Map<UUID, ScoreService.PointTotal> participantSessionTotals = gameManager.getScoreService().getParticipantSessionTotals(sessionId);
            sender.sendMessage(Component.empty()
                    .append(Component.text("Score totals for "))
                    .append(Component.text(sessionId))
                    .append(Component.text(":")));
            for (ScoreService.PointTotal pointTotal : participantSessionTotals.values()) {
                sender.sendMessage(Component.empty()
                        .append(Component.text(pointTotal.participantUUID().toString()))
                        .append(Component.text(", "))
                        .append(Component.text(pointTotal.teamId()))
                        .append(Component.text(" -> "))
                        .append(Component.text(pointTotal.totalPoints())));
            }
            
            Map<String, Integer> teamSessionTotals = gameManager.getScoreService().getTeamSessionTotals(sessionId);
            for (Map.Entry<String, Integer> entry : teamSessionTotals.entrySet()) {
                sender.sendMessage(Component.empty()
                        .append(Component.text(entry.getKey()))
                        .append(Component.text(" -> "))
                        .append(Component.text(entry.getValue()))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CommandResult.success();
    }
    
    private @NotNull CommandResult executeRebuildEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver resolver = ctx.getArgument("eventId", EventInfoResolver.class);
        try {
            EventInfo eventInfo = resolver.resolve();
            gameManager.getGameStateService().rebuildEventMode(eventInfo.getEventId());
            return CommandResult.success(Component.text("Loaded event mode"));
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "An error occurred trying to rebuild practice mode", e);
            return CommandResult.failure("An error occurred, see the console for more");
        }
    }
    
    private @NotNull CommandResult executeRebuildMaintenance(CommandContext<CommandSourceStack> ctx) {
        try {
            gameManager.getGameStateService().rebuildMaintenanceMode();
            return CommandResult.success(Component.text("Loaded maintenance mode"));
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "An error occurred trying to rebuild practice mode", e);
            return CommandResult.failure("An error occurred, see the console for more");
        }
    }
    
    private @NotNull CommandResult executeRebuildPractice(CommandContext<CommandSourceStack> ctx) {
        try {
            gameManager.getGameStateService().rebuildPracticeMode();
            return CommandResult.success(Component.text("Loaded practice mode"));
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "An error occurred trying to rebuild practice mode", e);
            return CommandResult.failure("An error occurred, see the console for more");
        }
    }
    
    private @NotNull CommandResult executeDebug(CommandContext<CommandSourceStack> ctx) {
        return CommandResult.success(Component.text("No implementation at this time"));
    }
}
