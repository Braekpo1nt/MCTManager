package org.braekpo1nt.mctmanager.commands.database;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

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
                .then(Commands.literal("clear")
                        .then(Commands.literal("score_service")
                                .executes(ctx -> {
                                    if (!plugin.getConfig().getString("database.mode", "prod").equals("test")) {
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("You can't clear the database unless you are in "))
                                                .append(Component.text("\"test\""))
                                                .append(Component.text(" mode. Check your config.yml file's database.mode value"))
                                                .color(NamedTextColor.RED)
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    try {
                                        gameManager.getScoreService().clearDatabase();
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("Clearing the database")));
                                        Main.logger().info("Clearing the database");
                                    } catch (SQLException e) {
                                        Main.logger().log(Level.SEVERE, "Error clearing database", e);
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("Error clearing database. See console for details"))
                                                .color(NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("event_service")
                                .executes(ctx -> {
                                    if (!plugin.getConfig().getString("database.mode", "prod").equals("test")) {
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("You can't clear the database unless you are in "))
                                                .append(Component.text("\"test\""))
                                                .append(Component.text(" mode. Check your config.yml file's database.mode value"))
                                                .color(NamedTextColor.RED)
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    try {
                                        gameManager.getEventService().clearDatabase();
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("Clearing the database")));
                                        Main.logger().info("Clearing the database");
                                    } catch (SQLException e) {
                                        Main.logger().log(Level.SEVERE, "Error clearing database", e);
                                        ctx.getSource().getSender().sendMessage(Component.empty()
                                                .append(Component.text("Error clearing database. See console for details"))
                                                .color(NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .permissionRoot("mctmanager")
                .build(plugin.getServer().getPluginManager());
    }
}
