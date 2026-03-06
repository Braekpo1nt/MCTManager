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
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameInstanceIdArgumentType implements CustomArgumentType.Converted<GameInstanceId, String> {
    
    private final static DynamicCommandExceptionType ERROR_INVALID_GAME_TYPE = new DynamicCommandExceptionType(gameTypeStr -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid gameId: "))
            .append(Component.text(gameTypeStr.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final static DynamicCommandExceptionType ERROR_NOT_AN_ACTIVE_GAME = new DynamicCommandExceptionType(gameInstanceId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("There is no active game with the id "))
            .append(Component.text(gameInstanceId.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final static DynamicCommandExceptionType ERROR_INVALID_FORMAT = new DynamicCommandExceptionType(attempted -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid format: "))
            .append(Component.text(attempted.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final @NotNull GameManager gameManager;
    private final boolean activeOnly;
    
    public GameInstanceIdArgumentType(@NotNull GameManager gameManager, boolean activeOnly) {
        this.gameManager = gameManager;
        this.activeOnly = activeOnly;
    }
    
    @Override
    public @NotNull GameInstanceId convert(@NotNull String inputString) throws CommandSyntaxException {
        Main.logf("inputString=\"%s\"", inputString);
        if (inputString.contains("+")) {
            String[] split = inputString.split("\\+");
            Main.logf("split=\"%s\"", Arrays.toString(split));
            if (split.length != 2) {
                throw ERROR_INVALID_FORMAT.create(inputString);
            }
            String gameTypeStr = split[0];
            String configFileRemaining = split[1];
            GameType gameType = GameType.fromID(gameTypeStr);
            if (gameType == null) {
                throw ERROR_INVALID_GAME_TYPE.create(gameTypeStr);
            }
            // TODO: validate config file?
            return new GameInstanceId(gameType, configFileRemaining);
        }
        
        GameType gameType = GameType.fromID(inputString);
        if (gameType == null) {
            throw ERROR_INVALID_GAME_TYPE.create(inputString);
        }
        GameInstanceId gameInstanceId = new GameInstanceId(gameType, "default.json");
        MCTGame activeGame = gameManager.getActiveGame(gameInstanceId);
        if (activeGame == null) {
            throw ERROR_NOT_AN_ACTIVE_GAME.create(gameInstanceId);
        }
        return gameInstanceId;
    }
    
    /**
     * Suggests the game ids on their own, in which case "default.json" will be selected as the config file,
     * and gameIds in combination with existing config files in the appropriate directory
     * @param context command context
     * @param builder suggestion builder
     * @param <S> usually a {@link io.papermc.paper.command.brigadier.CommandSourceStack}
     * @return the suggestions
     */
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            if (builder.getRemaining().contains("+")) {
                String[] split = builder.getRemaining().split("\\+");
                if (split.length == 0) {
                    return builder.build();
                }
                String gameTypeStr = split[0];
                GameType gameType = GameType.fromID(gameTypeStr);
                if (gameType == null) {
                    // "invalid-type:" results in no suggestions
                    return builder.build();
                }
                List<String> allConfigFiles;
                if (activeOnly) {
                    allConfigFiles = gameManager.getActiveConfigFiles(gameType);
                } else {
                    allConfigFiles = gameManager.getConfigFiles(gameType);
                }
                String configFileRemaining;
                if (split.length == 2) {
                    configFileRemaining = split[1];
                } else {
                    configFileRemaining = "";
                }
                allConfigFiles.stream()
                        .filter(configFile -> configFile.startsWith(configFileRemaining))
                        .forEach(configFile -> builder.suggest(String.format("%s+%s", gameType.getId(), configFile)));
            }
            return suggestOnlyGameId(builder);
        });
    }
    
    private Suggestions suggestOnlyGameId(@NotNull SuggestionsBuilder builder) {
        GameType gameType = GameType.fromID(builder.getRemaining());
        if (gameType == null) {
            Arrays.stream(GameType.values())
                    .map(GameType::getId)
                    .filter(id -> id.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.build();
        }
        builder.suggest(gameType.getId());
        List<String> configFiles;
        if (activeOnly) {
            configFiles = gameManager.getActiveConfigFiles(gameType);
        } else {
            configFiles = gameManager.getConfigFiles(gameType);
        }
        configFiles
                .forEach(configFile -> builder.suggest(String.format("%s+%s", gameType.getId(), configFile)));
        return builder.build();
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
