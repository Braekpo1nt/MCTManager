package org.braekpo1nt.mctmanager.commands.mct.team;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class JoinSubCommand implements BrigadierSubCommand {
    
    private static final SimpleCommandExceptionType ERROR_PLAYER_NOT_FOUND = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Could not find player"))
    ));
    
    private static final SimpleCommandExceptionType ERROR_TOO_MANY_PLAYERS = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Too many players, please only select 1"))
    ));
    
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
                            return GameManagerUtils.joinParticipant(gameManager, player, team);
                        }))
                        .then(Permissioned.argument("member", StringArgumentType.word())
                                .suggests((source, builder) -> suggestPlayerNames(builder))
                                .executes(BrigadierAdapters.wraps(this::executeJoin))
                                .then(Permissioned.argument("uuid", ArgumentTypes.uuid())
                                        .executes(BrigadierAdapters.wraps(this::executeJoinUUID))
                                )
                        )
                )
                ;
    }
    
    /**
     * @param builder the suggestion builder
     * @return the player names of all online/offline players and members of teams
     */
    private @NotNull CompletableFuture<Suggestions> suggestPlayerNames(SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            Stream.concat(
                            Arrays.stream(plugin.getServer().getOfflinePlayers())
                                    .map(OfflinePlayer::getName),
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
        final PlayerProfileListResolver profilesResolver = ctx.getArgument("member", PlayerProfileListResolver.class);
        final List<PlayerProfile> foundProfiles = new ArrayList<>(profilesResolver.resolve(ctx.getSource()));
        if (foundProfiles.isEmpty()) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        if (foundProfiles.size() != 1) {
            throw ERROR_TOO_MANY_PLAYERS.create();
        }
        PlayerProfile profile = foundProfiles.getFirst();
        if (profile.getName() == null) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        if (profile.getId() == null) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        return GameManagerUtils.joinParticipant(gameManager, profile.getName(), profile.getId(), team);
    }
    
    private @NotNull CommandResult executeJoinUUID(CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        String member = ctx.getArgument("member", String.class);
        UUID uuid = ctx.getArgument("uuid", UUID.class);
        return GameManagerUtils.joinParticipant(gameManager, member, uuid, team);
    }
}
