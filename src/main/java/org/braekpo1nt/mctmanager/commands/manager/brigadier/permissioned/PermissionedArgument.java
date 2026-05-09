package org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link Permissioned} that wraps a {@link RequiredArgumentBuilder}, allowing you to
 * interact with specific aspects of the {@link #requiredArgument}, such as adding suggestions.
 * @param <S> usually a {@link io.papermc.paper.command.brigadier.CommandSourceStack}
 * @param <T> the argument type, I think
 */
public class PermissionedArgument<S, T> extends Permissioned<S> {
    
    private final RequiredArgumentBuilder<S, T> requiredArgument;
    
    public PermissionedArgument(@NotNull RequiredArgumentBuilder<S, T> argument) {
        super(argument, argument.getName());
        this.requiredArgument = argument;
    }
    
    public Permissioned<S> suggests(final SuggestionProvider<S> provider) {
        requiredArgument.suggests(provider);
        return this;
    }
}
