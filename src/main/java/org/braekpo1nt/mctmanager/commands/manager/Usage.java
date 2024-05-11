package org.braekpo1nt.mctmanager.commands.manager;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds the usage of a command's arguments
 */
public class Usage {
    protected final @NotNull List<@NotNull Arg> args = new ArrayList<>();
    
    protected static class Arg {
        private final @NotNull String value;
        private final @Nullable TextDecoration decoration;
        
        protected Arg(@NotNull String value) {
            this.value = value;
            this.decoration = null;
        }
    
        protected Arg(@NotNull String value, @Nullable TextDecoration decoration) {
            this.value = value;
            this.decoration = decoration;
        }
        
        protected Component toComponent() {
            if (decoration != null) {
                return Component.text(value)
                        .decorate(decoration);
            }
            return Component.text(value);
        }
    }
    
    public Usage(@NotNull String arg) {
        args.add(new Arg(arg));
    }
    
    /**
     * Append a downstream argument to this {@link Usage}
     * @param arg the downstream argument to append
     * @return this {@link Usage}
     */
    public Usage of(@NotNull String arg) {
        args.add(new Arg(arg));
        return this;
    }
    
    /**
     * Append all given downstream arguments (must provide at least one)
     * @param args one or more additional downstream arguments to append, in order.
     * @return this {@link Usage}
     * @throws IllegalArgumentException if no arguments are given
     */
    public Usage of(@NotNull String @NotNull... args) {
        Preconditions.checkArgument(args.length >= 1, "Must provide at least one argument in the args array");
        for (String anArg : args) {
            this.args.add(new Arg(anArg));
        }
        return this;
    }
    
    /**
     * Append a downstream argument to this {@link Usage} with a {@link TextDecoration} to be applied
     * @param arg the downstream argument to append
     * @param decoration the {@link TextDecoration} to be applied to the given argument alone
     * @return this {@link Usage}
     */
    public Usage of(@NotNull String arg, TextDecoration decoration) {
        args.add(new Arg(arg, decoration));
        return this;
    }
    
    /**
     * Append the given {@link Usage}'s arguments to this one
     * @param other the other {@link Usage} to append
     * @return this {@link Usage}
     */
    public Usage of(@NotNull Usage other) {
        this.args.addAll(other.args);
        return this;
    }
    
    /**
     * Results are of the form "Usage: /arg1 arg2 ..." 
     * @return all this {@link Usage}'s arguments combined in a component (with their respective {@link TextDecoration}s applied), prefixed by "Usage: /"
     */
    public @NotNull Component toComponent() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Usage: /"));
        for (int i = 0; i < args.size(); i++) {
            builder.append(args.get(i).toComponent());
            if (i < args.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.asComponent();
    }
    
    /**
     * Given a collection of Strings, returns a {@link Usage} in the form of {@code "<arg1|arg2|arg3>"}
     * @param args the args to combine to an argument-options string
     * @return Returns a Usage in the form of {@code "<arg1|arg2|arg3>"} using the given arguments. If there is only one argument, just returns a usage of that argument with no {@code <>} brackets. {@code "<options>"} if the provided args collection is empty.
     */
    public static Usage toArgOptions(@NotNull Collection<@NotNull String> args) {
        if (args.isEmpty()) {
            return new Usage("<options>");
        } else if (args.size() == 1) {
            return new Usage(args.iterator().next());
        } else {
            return new Usage(String.format("<%s>", String.join("|", args)));
        }
    }
}
