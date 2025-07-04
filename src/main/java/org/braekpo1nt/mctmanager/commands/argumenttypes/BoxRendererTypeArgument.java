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
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BoxRendererTypeArgument implements CustomArgumentType.Converted<BoundingBoxRendererImpl.Type, String> {
    
    private static final DynamicCommandExceptionType ERROR_INVALID_BOX_TYPE = new DynamicCommandExceptionType(type -> {
        return MessageComponentSerializer.message()
                .serialize(Component.text(type + " is not a valid box type!"));
    });
    
    @Override
    public BoundingBoxRendererImpl.Type convert(String nativeType) throws CommandSyntaxException {
        try {
            return BoundingBoxRendererImpl.Type.valueOf(nativeType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            throw ERROR_INVALID_BOX_TYPE.create(nativeType);
        }
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (BoundingBoxRendererImpl.Type type : BoundingBoxRendererImpl.Type.values()) {
            String name = type.toString();
            
            // Only suggest if the flavor name matches the user input
            if (name.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(name);
            }
        }
        
        return builder.buildFuture();
    }
    
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
