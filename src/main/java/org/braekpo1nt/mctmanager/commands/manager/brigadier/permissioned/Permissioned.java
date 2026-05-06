package org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A wrapper for an {@link ArgumentBuilder} that makes automatically generating and enforcing
 * permission nodes easy when making many and/or deep-treed commands. Designed
 * to function indistinguishably from the default Brigadier command builder, {@link Commands},
 * aside from the name.<br>
 * {@link #literal(String)} is an analogue for {@link Commands#literal(String)},
 * {@link #argument(String, ArgumentType)} is an analogue for {@link Commands#argument(String, ArgumentType)}.
 * @param <S> usually a {@link CommandSourceStack}
 */
@Slf4j
public class Permissioned<S> {
    
    private final @NotNull ArgumentBuilder<S, ?> argument;
    @Getter
    private final @NotNull String name;
    @Getter
    private @Nullable String permissionNode;
    @Getter
    private final @NotNull List<Permissioned<S>> children;
    
    public static PermissionedLiteral<CommandSourceStack> literal(@NotNull String literal) {
        return new PermissionedLiteral<>(Commands.literal(literal));
    }
    
    public static PermissionedArgument<CommandSourceStack, ?> argument(@NotNull String name, ArgumentType<?> argumentType) {
        return new PermissionedArgument<>(Commands.argument(name, argumentType));
    }
    
    public Permissioned(@NotNull ArgumentBuilder<S, ?> argument, @NotNull String name) {
        this.argument = argument;
        this.name = name;
        this.children = new ArrayList<>();
    }
    
    public Permissioned<S> then(@NotNull ArgumentBuilder<S, ?> argument, @NotNull String name) {
        return then(new Permissioned<>(argument, name));
    }
    
    public Permissioned<S> then(LiteralArgumentBuilder<S> argument) {
        return then(argument, argument.getLiteral());
    }
    
    public Permissioned<S> then(RequiredArgumentBuilder<S, ?> argument) {
        return then(argument, argument.getName());
    }
    
    public Permissioned<S> then(Permissioned<S> argument) {
        children.add(argument);
        return this;
    }
    
    public Permissioned<S> executes(final Command<S> command) {
        argument.executes(command);
        return this;
    }
    
    public Permissioned<S> requires(final Predicate<S> requirement) {
        argument.requires(requirement);
        return this;
    }
    
    /**
     * Adds the given string as a root to the {@link #permissionNode}. The default {@link #permissionNode}
     * is just the {@link #name}, but using this makes it "permissionRoot.name".<br>
     * Note this is only effective for the root {@link Permissioned} node, because the {@link #build(PluginManager)}
     * operation overwrites the sub-nodes {@link #permissionNode}
     * @param permissionRoot the root to add to the permission node
     * @return this
     */
    public Permissioned<S> permissionRoot(@NotNull String permissionRoot) {
        this.permissionNode = String.format("%s.%s", permissionRoot, name);
        return this;
    }
    
    public void setPermissionNode(@Nullable String permissionNode) {
        log.atDebug().log("setting permission for {}", name);
        if (permissionNode == null) {
            return;
        }
        this.permissionNode = permissionNode;
        for (Permissioned<S> child : children) {
            child.setPermissionNode(String.format("%s.%s", this.permissionNode, child.getName()));
        }
    }
    
    /**
     * Different from {@link #build(PluginManager)} only in that
     * it doesn't assign the permission nodes recursively. This prevents
     * redundant assignment that a single public method would produce.
     * @param pluginManager the pluginManager to register the permission nodes with
     * @return the build CommandNode
     */
    private CommandNode<S> buildChildren(PluginManager pluginManager) {
        log.atDebug().log("building individual {} with permission node {}", name, permissionNode);
        if (permissionNode != null && pluginManager.getPermission(permissionNode) == null) {
            pluginManager.addPermission(new Permission(permissionNode));
        }
        if (permissionNode != null) {
            log.atDebug().log("requires permission node {} for {}", permissionNode, name);
            // prevents recursive requirement
            final Predicate<S> oldRequirement = argument.getRequirement();
            argument
                    .requires(s -> {
                        if (!(s instanceof CommandSourceStack source)) {
                            return oldRequirement.test(s);
                        }
                        return source.getSender().hasPermission(permissionNode) && oldRequirement.test(s);
                    });
        }
        for (Permissioned<S> child : children) {
            argument.then(child.buildChildren(pluginManager));
        }
        return argument.build();
    }
    
    /**
     * Sets the permission nodes of this and all children first,
     * then builds the Command
     * @param pluginManager the pluginManager to register the permission nodes with
     * @return the build CommandNode
     */
    public LiteralCommandNode<S> build(PluginManager pluginManager) {
        log.atDebug().log("building entire tree for {}", name);
        this.setPermissionNode(permissionNode != null ? permissionNode : getName());
        CommandNode<S> commandNode = buildChildren(pluginManager);
        if (!(commandNode instanceof LiteralCommandNode<S> literalCommandNode)) {
            throw new IllegalStateException("Attempted to build a command where the root node is a not an instance of LiteralCommandNode<S>");
        }
        return literalCommandNode;
    }
}
