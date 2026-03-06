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
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class GameIdArgumentType implements CustomArgumentType.Converted<GameType, String> {
    
    private final static DynamicCommandExceptionType ERROR_INVALID_GAME_TYPE = new DynamicCommandExceptionType(gameTypeStr -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid gameId: "))
            .append(Component.text(gameTypeStr.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final static DynamicCommandExceptionType ERROR_NOT_AN_ACTIVE_GAME = new DynamicCommandExceptionType(gameTypeId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("There is no active game of type "))
            .append(Component.text(gameTypeId.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    
    private final @NotNull GameManager gameManager;
    private final boolean activeOnly;
    
    public GameIdArgumentType(@NotNull GameManager gameManager, boolean activeOnly) {
        this.gameManager = gameManager;
        this.activeOnly = activeOnly;
    }
    
    @Override
    public @NotNull GameType convert(@NotNull String inputString) throws CommandSyntaxException {
        GameType gameType = GameType.fromID(inputString);
        if (gameType == null) {
            throw ERROR_INVALID_GAME_TYPE.create(inputString);
        }
        if (activeOnly) {
            if (!gameManager.gameIsActive(gameType)) {
                throw ERROR_NOT_AN_ACTIVE_GAME.create(gameType.getId());
            }
        }
        return gameType;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            if (activeOnly) {
                gameManager.getActiveGameIds().stream()
                        .map(gameInstanceId -> gameInstanceId.getGameType().getId())
                        .filter(id -> id.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);
            } else {
                Arrays.stream(GameType.values())
                        .map(GameType::getId)
                        .filter(id -> id.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);
            }
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
