package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    
    public Usage of(@NotNull String arg) {
        args.add(new Arg(arg));
        return this;
    }
    
    public Usage of(@NotNull String arg, TextDecoration decoration) {
        args.add(new Arg(arg, decoration));
        return this;
    }
    
    public Usage of(@NotNull Usage other) {
        this.args.addAll(other.args);
        return this;
    }
    
    public @NotNull Component toComponent() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Usage: /"));
        for (int i = 0; i < args.size(); i++) {
            builder.append(args.get(i).toComponent());
            if (i < args.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.color(NamedTextColor.RED).asComponent();
    }
    
    
}
