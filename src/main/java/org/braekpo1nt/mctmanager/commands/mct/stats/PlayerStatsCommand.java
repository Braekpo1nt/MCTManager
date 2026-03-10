package org.braekpo1nt.mctmanager.commands.mct.stats;

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
import java.util.Collection;
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
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeSetDiscord(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerMetadataResolver resolver = ctx.getArgument("playerName", PlayerMetadataResolver.class);
        String discordUsername = ctx.getArgument("discordUsername", String.class);
        try {
            Collection<PlayerMetadata> playerMetadatas = resolver.resolve(ctx.getSource());
            if (playerMetadatas.isEmpty()) {
                return CommandResult.failure("Could not find player");
            }
            if (playerMetadatas.size() != 1) {
                return CommandResult.failure("Must reference only one player");
            }
            PlayerMetadata playerMetadata = playerMetadatas.stream().findFirst().get();
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
                        .append(Component.text(oldDiscordUsername))
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
            Main.logger().log(Level.SEVERE, "SQL error occurred updating PlayerMetadata", e);
            return CommandResult.failure("A database error occurred. See console for details.");
        }
    }
    
}
