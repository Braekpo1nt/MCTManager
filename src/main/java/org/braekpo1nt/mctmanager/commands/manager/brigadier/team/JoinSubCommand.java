package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class JoinSubCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public JoinSubCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("join")
                .then(Commands.argument("teamId", new TeamArgumentType(gameManager))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            Team team = ctx.getArgument("teamId", Team.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                return CommandResult.failure("Must be a player to use the no-argument option");
                            }
                            return GameManagerUtils.joinParticipant(plugin, gameManager, player.getName(), team);
                        }))
                        .then(Commands.argument("member", StringArgumentType.word())
                                .suggests((source, builder) -> suggestPlayerNames(builder))
                                .executes(BrigadierAdapters.wraps(this::executeJoin))
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
    
    private @NotNull CommandResult executeJoin(CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        String member = ctx.getArgument("member", String.class);
        return GameManagerUtils.joinParticipant(plugin, gameManager, member, team);
    }
}
