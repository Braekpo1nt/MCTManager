package org.braekpo1nt.mctmanager.commands.database;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.database.team.DatabaseTeamCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DatabaseCommand implements BrigadierCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public DatabaseCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Permissioned.literal("database")
                .then(new DatabaseTeamCommand(plugin, gameManager).create())
                .then(Commands.literal("clear")
                        .then(Commands.literal("score_service")
                                .executes(BrigadierAdapters.wraps(this::executeClearScoreService))
                        )
                        .then(Commands.literal("event_service")
                                .executes(BrigadierAdapters.wraps(this::executeClearEventService))
                        )
                )
                .permissionRoot("mctmanager")
                .build(plugin.getServer().getPluginManager());
    }
    
    private @NotNull CommandResult executeClearEventService(CommandContext<CommandSourceStack> ctx) {
        if (!plugin.getConfig().getString("database.mode", "prod").equals("test")) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("You can't clear the database unless you are in "))
                    .append(Component.text("\"test\""))
                    .append(Component.text(" mode. Check your config.yml file's database.mode value"))
            );
        }
        try {
            gameManager.getEventService().clearDatabase();
            Main.logger().info("Clearing the database");
            return CommandResult.success(Component.empty()
                    .append(Component.text("Clearing the database")));
        } catch (SQLException e) {
            return CommandResult.sqlException("clearing the event database", e);
        }
    }
    
    private @NotNull CommandResult executeClearScoreService(CommandContext<CommandSourceStack> ctx) {
        if (!plugin.getConfig().getString("database.mode", "prod").equals("test")) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("You can't clear the database unless you are in "))
                    .append(Component.text("\"test\""))
                    .append(Component.text(" mode. Check your config.yml file's database.mode value")));
        }
        try {
            gameManager.getScoreService().clearDatabase();
            Main.logger().info("Clearing the database");
            return CommandResult.success(Component.empty()
                    .append(Component.text("Clearing the database")));
        } catch (SQLException e) {
            return CommandResult.sqlException("clearing the score database", e);
        }
    }
}
