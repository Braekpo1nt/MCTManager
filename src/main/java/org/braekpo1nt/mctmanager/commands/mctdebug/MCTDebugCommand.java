package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
