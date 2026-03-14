package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Generates an {@link EventInfoResolver} when given an eventId
 */
public class EventInfoArgumentType implements CustomArgumentType.Converted<EventInfoResolver, String> {
    
    private final @NotNull EventService eventService;
    
    public EventInfoArgumentType(@NotNull EventService eventService) {
        this.eventService = eventService;
    }
    
    @Override
    public @NotNull EventInfoResolver convert(@NotNull String eventId) throws CommandSyntaxException {
        return new EventInfoResolver(eventService, eventId);
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                eventService.getEventIds().stream()
                        .filter(eventId -> eventId.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);
            } catch (SQLException e) {
                return builder.build();
            }
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
