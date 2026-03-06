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
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Allows the last argument of a command to be an arbitrary length string of space-separated teams
 */
public class TeamIdsListArgumentType implements CustomArgumentType.Converted<String[], String> {
    
    private static final DynamicCommandExceptionType ERROR_TEAM_DOES_NOT_EXISTS = new DynamicCommandExceptionType(teamId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid team id: "))
            .append(Component.text(teamId.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final @NotNull GameManager gameManager;
    
    public static TeamIdsListArgumentType teamIds(@NotNull GameManager gameManager) {
        return new TeamIdsListArgumentType(gameManager);
    }
    
    private TeamIdsListArgumentType(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }
    
    @Override
    public String @NotNull [] convert(@NotNull String nativeValue) throws CommandSyntaxException {
        
        if (nativeValue.isBlank()) {
            return new String[0]; // zero teamIds case
        }
        
        List<String> input = Arrays.stream(nativeValue.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
        
        Set<String> validTeams = gameManager.getTeamIds();
        
        for (String id : input) {
            if (!validTeams.contains(id)) {
                throw ERROR_TEAM_DOES_NOT_EXISTS.create(id);
            }
        }
        
        return input.toArray(new String[0]);
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(
            @NotNull CommandContext<S> context,
            @NotNull SuggestionsBuilder builder
    ) {
        return CompletableFuture.supplyAsync(() -> {
            String remainingFullStrings = builder.getRemaining();
            CommandUtils.suggestTeamIds(remainingFullStrings, gameManager.getTeamIds())
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
}
