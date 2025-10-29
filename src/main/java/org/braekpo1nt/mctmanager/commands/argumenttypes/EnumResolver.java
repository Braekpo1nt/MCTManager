package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A convenience argument type to easily parse arguments into any enum type.
 * Also provides suggestions for the command sender.
 * Enum arguments are suggested and parsed in lowercase.
 * @param <E> The type of enum to be parsed
 */
public class EnumResolver<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {
    
    private static final InvalidEnumInputException ERROR_INVALID_ENUM_VALUE = new InvalidEnumInputException();
    
    public static class InvalidEnumInputException implements CommandExceptionType {
        public CommandSyntaxException create(Component message) {
            return new CommandSyntaxException(this, MessageComponentSerializer.message()
                    .serialize(message));
        }
        
        public CommandSyntaxException create(Class<?> enumType, String nativeType) {
            return create(Component.empty()
                    .append(Component.text(nativeType)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid value for enum "))
                    .append(Component.text(enumType.getName())
                            .decorate(TextDecoration.ITALIC)));
        }
    }
    
    private final Class<E> type;
    private final Set<String> suggestions;
    
    /**
     * @param type The type of the enum to parse
     * @param suggestions The values to be suggested the user when typing this argument
     */
    public EnumResolver(Class<E> type, E[] suggestions) {
        this.type = type;
        this.suggestions = Arrays.stream(suggestions).map(e -> e.toString().toLowerCase()).collect(Collectors.toSet());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull E convert(@NotNull String nativeType) throws CommandSyntaxException {
        try {
            return E.valueOf(type, nativeType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            throw ERROR_INVALID_ENUM_VALUE.create(type, nativeType);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        for (String suggestion : suggestions) {
            if (suggestion.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
        //        return Converted.super.listSuggestions(context, builder);
    }
}
