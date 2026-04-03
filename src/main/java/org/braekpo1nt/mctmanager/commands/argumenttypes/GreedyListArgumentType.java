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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GreedyListArgumentType implements CustomArgumentType.Converted<String[], String> {
    
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(value -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Invalid value: "))
            .append(Component.text(value.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final @NotNull Set<String> validValues;
    
    protected @NotNull Set<String> getValidValues() {
        return validValues;
    }
    
    public GreedyListArgumentType(@NotNull Set<String> validValues) {
        this.validValues = validValues;
    }
    
    @Override
    public String @NotNull [] convert(@NotNull String nativeValue) throws CommandSyntaxException {
        if (nativeValue.isBlank()) {
            return new String[0]; // zero teamIds case
        }
        List<String> input = Arrays.stream(nativeValue.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
        
        for (String value : input) {
            if (!getValidValues().contains(value)) {
                throw ERROR_INVALID_VALUE.create(value);
            }
        }
        
        return input.toArray(new String[0]);
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(
            @NotNull CommandContext<S> context,
            @NotNull SuggestionsBuilder builder
    ) {
        return CompletableFuture.supplyAsync(() -> {
            String remainingFullStrings = builder.getRemaining();
            CommandUtils.suggestGreedyList(remainingFullStrings, getValidValues())
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
}
