package org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link Permissioned} that wraps a {@link LiteralArgumentBuilder}.
 * You could just as easily do the same thing with an instance of {@link Permissioned},
 * this is mainly for readability and potential future extension if there is any literal-specific
 * behavior needed from the wrapper.
 * @param <S> usually a {@link io.papermc.paper.command.brigadier.CommandSourceStack}
 */
public class PermissionedLiteral<S> extends Permissioned<S> {
    private final LiteralArgumentBuilder<S> literalArgument;
    
    public PermissionedLiteral(@NotNull LiteralArgumentBuilder<S> argument) {
        super(argument, argument.getLiteral());
        this.literalArgument = argument;
    }
}
