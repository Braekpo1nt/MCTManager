package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AllPlayersDebugCommand implements BrigadierSubCommand {
    
    private final GameStateService gameStateService;
    private final @NotNull Main plugin;
    
    public AllPlayersDebugCommand(@NotNull GameManager gameManager, @NotNull Main plugin) {
        this.gameStateService = gameManager.getGameStateService();
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("all_players")
                .then(Permissioned.literal("testEquals")
                        .then(Permissioned.argument("ign", StringArgumentType.word())
                                .executes(BrigadierAdapters.wraps(this::executeTestEquals))
                        )
                )
                .then(Permissioned.literal("add")
                        .then(Permissioned.argument("uuid", plugin.getUUIDArgumentType())
                                .then(Permissioned.argument("ign", StringArgumentType.word())
                                        .executes(BrigadierAdapters.wraps(this::executeAddUUIDAndIGN))
                                )
                        )
                )
                .then(Permissioned.literal("findUUID")
                        .then(Permissioned.argument("ign", StringArgumentType.word())
                                .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                                    try {
                                        gameStateService.getPlayerIGNs()
                                                .forEach(builder::suggest);
                                    } catch (SQLException e) {
                                        // do nothing
                                    }
                                    return builder.build();
                                }))
                                .executes(BrigadierAdapters.wraps(ctx -> {
                                    String ign = ctx.getArgument("ign", String.class);
                                    AllPlayersEntity player;
                                    try {
                                        player = gameStateService.getPlayerByIgn(ign);
                                    } catch (SQLException e) {
                                        return CommandResult.sqlException("find player", e);
                                    }
                                    if (player == null) {
                                        return CommandResult.failure("Could not find a player in database with that ign");
                                    }
                                    String uuid = player.getUuid();
                                    return CommandResult.success(Component.empty()
                                            .append(Component.text("The database says "))
                                            .append(Component.text(ign))
                                            .append(Component.text("'s UUID is "))
                                            .append(Component.text(uuid)
                                                    .decorate(TextDecoration.UNDERLINED)
                                                    .clickEvent(ClickEvent.copyToClipboard(uuid))
                                                    .hoverEvent(HoverEvent.showText(Component.text("Copy"))))
                                    );
                                }))
                        )
                )
                .then(Permissioned.literal("findIGN")
                        .then(Permissioned.argument("uuid", plugin.getUUIDArgumentType())
                                .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                                    try {
                                        gameStateService.getPlayerUUIDs()
                                                .forEach(builder::suggest);
                                    } catch (SQLException e) {
                                        // do nothing
                                    }
                                    return builder.build();
                                }))
                                .executes(BrigadierAdapters.wraps(ctx -> {
                                    UUID uuid = ctx.getArgument("uuid", UUID.class);
                                    AllPlayersEntity player;
                                    try {
                                        player = gameStateService.getPlayer(uuid.toString());
                                    } catch (SQLException e) {
                                        return CommandResult.sqlException("find player", e);
                                    }
                                    if (player == null) {
                                        return CommandResult.failure("Could not find a player in database with that uuid");
                                    }
                                    String ign = player.getIgn();
                                    return CommandResult.success(Component.empty()
                                            .append(Component.text("The database says the player with the uuid "))
                                            .append(Component.text(uuid.toString()))
                                            .append(Component.text(" has an ign of "))
                                            .append(Component.text(ign)
                                                    .decorate(TextDecoration.UNDERLINED)
                                                    .clickEvent(ClickEvent.copyToClipboard(ign))
                                                    .hoverEvent(HoverEvent.showText(Component.text("Copy"))))
                                    );
                                }))
                        )
                );
        
    }
    
    private @NotNull CommandResult executeTestEquals(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String ign = ctx.getArgument("ign", String.class);
        try {
            int n = gameStateService.getMatchesInAllPlayers(ign);
            return CommandResult.success(Component.empty()
                    .append(Component.text("There are "))
                    .append(Component.text(n)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" entries in all_players with the ign "))
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
            );
        } catch (SQLException e) {
            return CommandResult.sqlException("getting matches for the ign in all_players", e);
        }
    }
    
    private @NotNull CommandResult executeAddUUIDAndIGN(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String uuid = ctx.getArgument("uuid", UUID.class).toString();
        String ign = ctx.getArgument("ign", String.class);
        try {
            gameStateService.registerPlayer(uuid, ign);
        } catch (SQLException e) {
            return CommandResult.sqlException("registering uuid and ign", e);
        }
        return CommandResult.success(Component.text("Done"));
    }
}
