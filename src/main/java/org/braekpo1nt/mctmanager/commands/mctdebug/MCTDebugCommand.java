package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
                .then(Permissioned.literal("futureTest")
                        .executes(BrigadierAdapters.wraps(this::executeFutureTest))
                )
                .then(Commands.literal("rebuild")
                        .then(Commands.argument("mode", new EnumArgumentType<>(Mode.class, Mode.values()))
                                .executes(BrigadierAdapters.wraps(this::executeRebuild))
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
    
    private @NotNull CommandResult executeFutureTest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return CommandResult.async(plugin, asyncOperation(), this::syncOperation);
    }
    
    private CompletableFuture<CommandResult> asyncOperation() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                callDatabaseTask();
            } catch (SQLException e) {
                return CommandResult.sqlException("call db task", e);
            }
            return CommandResult.success(Component.text("async op success"));
        });
    }
    
    private CommandResult syncOperation() {
        callTaskThatUsesBukkitAPIButMustHappenAfterDatabaseTaskCompletes();
        return CommandResult.success(Component.text("sync op success"));
    }
    
    private void callDatabaseTask() throws SQLException {
        gameManager.getGameStateService().getAllPracticeTeams().forEach(practiceTeam -> {
            Main.logger().info("TeamId: " + practiceTeam.getTeamId());
        });
    }
    
    private void callTaskThatUsesBukkitAPIButMustHappenAfterDatabaseTaskCompletes() {
        gameManager.getOnlineParticipants().forEach(participant -> participant.teleport(new Location(
                participant.getLocation().getWorld(),
                participant.getLocation().getX(),
                participant.getLocation().getY() + 5,
                participant.getLocation().getZ()
        )));
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
    
    private @NotNull CommandResult executeRebuild(CommandContext<CommandSourceStack> ctx) {
        Mode mode = ctx.getArgument("mode", Mode.class);
        try {
            switch (mode) {
                case PRACTICE -> {
                    gameManager.getGameStateService().rebuildPracticeMode();
                    return CommandResult.success(Component.text("Loaded practice mode"));
                }
                case MAINTENANCE -> {
                    gameManager.getGameStateService().rebuildMaintenanceMode();
                    return CommandResult.success(Component.text("Loaded maintenance mode"));
                }
                case EVENT -> {
                    gameManager.getGameStateService().rebuildEventMode("MCT_1B");
                    return CommandResult.success(Component.text("Loaded event mode"));
                }
                case null, default -> {
                    return CommandResult.failure("not recognized practice|event|maintenance");
                }
            }
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "An error occurred trying to rebuild practice mode", e);
            return CommandResult.failure("An error occurred, see the console for more");
        }
    }
    
    private @NotNull CommandResult executeDebug(CommandContext<CommandSourceStack> ctx) {
        return CommandResult.success(Component.text("No implementation at this time"));
    }
}
