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
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OfflineParticipantArgumentType implements CustomArgumentType.Converted<OfflineParticipant, String> {
    
    private static final DynamicCommandExceptionType ERROR_PARTICIPANT_DOES_NOT_EXIST = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" is not a participant."))
    ));
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    /**
     * @param plugin the plugin used to lookup offline players by name
     * @param gameManager the gameManager used to search through and retrieve participants
     */
    public OfflineParticipantArgumentType(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            gameManager.getAllParticipantNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    @Override
    public @NotNull OfflineParticipant convert(@NotNull String playerName) throws CommandSyntaxException {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(player.getUniqueId());
        if (offlineParticipant == null) {
            throw ERROR_PARTICIPANT_DOES_NOT_EXIST.create(playerName);
        }
        return offlineParticipant;
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
