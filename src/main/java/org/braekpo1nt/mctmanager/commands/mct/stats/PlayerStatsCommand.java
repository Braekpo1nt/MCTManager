package org.braekpo1nt.mctmanager.commands.mct.stats;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.database.PlayerMetadataArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.database.PlayerMetadataResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlayerStatsCommand implements BrigadierSubCommand {
    
    private final @NotNull GameStateService gameStateService;
    
    public PlayerStatsCommand(@NotNull GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("player")
                .then(Permissioned.argument("playerName", new PlayerMetadataArgumentType(gameStateService))
                        .then(Permissioned.literal("set")
                                .then(Permissioned.literal("discord")
                                        .then(Permissioned.argument("discordUsername", StringArgumentType.string())
                                                .executes(BrigadierAdapters.wraps(this::executeSetDiscord))
                                        )
                                )
                                .then(Permissioned.literal("percentRank")
                                        .then(Permissioned.argument("rank", DoubleArgumentType.doubleArg())
                                                .executes(BrigadierAdapters.wraps(this::executeSetPercentRank))
                                        )
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeSetPercentRank(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerMetadataResolver resolver = ctx.getArgument("playerName", PlayerMetadataResolver.class);
        double percentRank = ctx.getArgument("rank", Double.class);
        try {
            PlayerMetadata playerMetadata = resolver.resolveSingle(ctx.getSource());
            double oldPercentRank = playerMetadata.getPercentRank();
            playerMetadata.setPercentRank(percentRank);
            gameStateService.update(playerMetadata);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Set percent rank of "))
                    .append(Component.text(playerMetadata.getIgn())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(percentRank)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" (was "))
                    .append(Component.text(oldPercentRank)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(")"))
            );
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "SQL error occurred updating PlayerMetadata percentRank", e);
            return CommandResult.failure("A database error occurred. See console for details.");
        }
    }
    
    private @NotNull CommandResult executeSetDiscord(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerMetadataResolver resolver = ctx.getArgument("playerName", PlayerMetadataResolver.class);
        String discordUsername = ctx.getArgument("discordUsername", String.class);
        try {
            PlayerMetadata playerMetadata = resolver.resolveSingle(ctx.getSource());
            String oldDiscordUsername = playerMetadata.getDiscordUsername();
            playerMetadata.setDiscordUsername(discordUsername);
            gameStateService.update(playerMetadata);
            if (oldDiscordUsername != null) {
                return CommandResult.success(Component.empty()
                        .append(Component.text("Set discord username of "))
                        .append(Component.text(playerMetadata.getIgn())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" to "))
                        .append(Component.text(discordUsername)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" (was "))
                        .append(Component.text(oldDiscordUsername)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(")"))
                );
            } else {
                return CommandResult.success(Component.empty()
                        .append(Component.text("Set discord username of "))
                        .append(Component.text(playerMetadata.getIgn())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" to "))
                        .append(Component.text(discordUsername)
                                .decorate(TextDecoration.BOLD))
                );
            }
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "SQL error occurred updating PlayerMetadata discordUsername", e);
            return CommandResult.failure("A database error occurred. See console for details.");
        }
    }
    
}
