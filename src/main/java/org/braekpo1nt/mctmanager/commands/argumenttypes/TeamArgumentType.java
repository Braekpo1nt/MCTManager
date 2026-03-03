package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TeamArgumentType implements CustomArgumentType.Converted<Team, String> {
    
    private static final DynamicCommandExceptionType ERROR_TEAM_DOES_NOT_EXISTS = new DynamicCommandExceptionType(teamId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid team id: "))
            .append(Component.text(teamId.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final @NotNull GameManager gameManager;
    
    public TeamArgumentType(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Team convert(@NotNull String nativeType) throws CommandSyntaxException {
        Team team = gameManager.getTeam(nativeType);
        if (team == null) {
            throw ERROR_TEAM_DOES_NOT_EXISTS.create(nativeType);
        }
        return team;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            gameManager.getTeamIds().stream()
                    .filter(teamId -> teamId.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
    
}
