package org.braekpo1nt.mctmanager.commands.mct.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class JoinSubCommand implements BrigadierSubCommand {
    
    private static final SimpleCommandExceptionType ERROR_NAME_DOES_NOT_MATCH = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("The given ign does not match that of the given UUID for the online player"))
    ));
    private static final String IGN_ARGUMENT = "ign";
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public JoinSubCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("join")
                .then(Permissioned.argument("teamId", new TeamArgumentType(gameManager))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            Team team = ctx.getArgument("teamId", Team.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                return CommandResult.failure("Must be a player to use the no-argument option");
                            }
                            return gameManager.joinOnlineParticipant(player, team.getTeamId());
                        }))
                        .then(Permissioned.argument(IGN_ARGUMENT, StringArgumentType.word())
                                .suggests((source, builder) -> suggestPlayerNames(builder))
                                .executes(BrigadierAdapters.wraps(this::executeJoin))
                                .then(Permissioned.argument("uuid", plugin.getUUIDArgumentType())
                                        .suggests((ctx, builder) -> JoinSubCommand.suggestPlayerUUID(ctx, builder, plugin))
                                        .executes(BrigadierAdapters.wraps(this::executeJoinUUID))
                                )
                        )
                )
                ;
    }
    
    public static @NotNull CompletableFuture<Suggestions> suggestPlayerUUID(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, Main plugin) {
        return CompletableFuture.supplyAsync(() -> {
            String ign = ctx.getArgument(JoinSubCommand.IGN_ARGUMENT, String.class);
            UUID uuid = plugin.getServer().getPlayerUniqueId(ign);
            if (uuid != null) {
                builder.suggest(uuid.toString());
            }
            return builder.build();
        });
    }
    
    /**
     * @param builder the suggestion builder
     * @return the player names of all online/offline players and members of teams
     */
    private @NotNull CompletableFuture<Suggestions> suggestPlayerNames(SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            Stream.concat(
                            plugin.getServer().getOnlinePlayers().stream()
                                    .map(Player::getName),
                            gameManager.getAllParticipantNames().stream()
                    )
                    .distinct()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    private @NotNull CommandResult executeJoin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Team team = ctx.getArgument("teamId", Team.class);
        String ign = ctx.getArgument(JoinSubCommand.IGN_ARGUMENT, String.class);
        // prioritize online players
        Player player = plugin.getServer().getPlayer(ign);
        if (player != null) {
            return gameManager.joinOnlineParticipant(player, team.getTeamId());
        }
        UUID uuid = plugin.getServer().getPlayerUniqueId(ign);
        if (uuid == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not find uuid for player with ign "))
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
            );
        }
        return gameManager.joinOfflineParticipant(uuid, ign, team.getTeamId());
    }
    
    private @NotNull CommandResult executeJoinUUID(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Team team = ctx.getArgument("teamId", Team.class);
        String ign = ctx.getArgument(JoinSubCommand.IGN_ARGUMENT, String.class);
        UUID uuid = ctx.getArgument("uuid", UUID.class);
        // prioritize online players first
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) {
            return gameManager.joinOfflineParticipant(uuid, ign, team.getTeamId());
        }
        if (!player.getName().equals(ign)) {
            throw ERROR_NAME_DOES_NOT_MATCH.create();
        }
        return gameManager.joinOnlineParticipant(player, team.getTeamId());
    }
}
