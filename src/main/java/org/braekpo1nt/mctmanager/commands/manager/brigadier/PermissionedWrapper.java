package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PermissionedWrapper<S> {
    
    private final @NotNull ArgumentBuilder<S, ?> argument;
    @Getter
    private final @NotNull String name;
    @Getter
    private @Nullable String permissionNode;
    @Getter
    private final @NotNull List<PermissionedWrapper<S>> children;
    
    public static PermissionedWrapper<CommandSourceStack> literal(@NotNull String literal) {
        return new PermissionedWrapper<>(Commands.literal(literal), literal);
    }
    
    public PermissionedWrapper(@NotNull ArgumentBuilder<S, ?> argument, @NotNull String name) {
        this.argument = argument;
        this.name = name;
        this.children = new ArrayList<>();
    }
    
    public PermissionedWrapper<S> then(LiteralArgumentBuilder<S> argument) {
        return then(new PermissionedWrapper<>(argument, argument.getLiteral()));
    }
    
    public PermissionedWrapper<S> then(RequiredArgumentBuilder<S, ?> argument) {
        return then(new PermissionedWrapper<>(argument, argument.getName()));
    }
    
    public PermissionedWrapper<S> then(PermissionedWrapper<S> argument) {
        children.add(argument);
        return this;
    }
    
    public void setPermissionNode(@Nullable String permissionNode) {
        if (permissionNode == null) {
            return;
        }
        this.permissionNode = permissionNode;
        for (PermissionedWrapper<S> child : children) {
            child.setPermissionNode(String.format("%s.%s", this.permissionNode, child.getName()));
        }
    }
    
    /**
     * Different from {@link #build(PretendPluginManager)} only in that
     * it doesn't assign the permission nodes recursively. This prevents
     * redundant assignment that a single public method would produce.
     * @param pluginManager the pluginManager to register the permission nodes with
     * @return the build CommandNode
     */
    private CommandNode<S> buildChildren(PretendPluginManager pluginManager) {
        if (permissionNode != null && pluginManager.getPermission(permissionNode) == null) {
            pluginManager.addPermission(new Permission(permissionNode));
        }
        if (permissionNode != null) {
            argument
                    .requires(s -> {
                        if (!(s instanceof CommandSourceStack source)) {
                            return argument.getRequirement().test(s);
                        }
                        return source.getSender().hasPermission(permissionNode) && argument.getRequirement().test(s);
                    });
        }
        for (PermissionedWrapper<S> child : children) {
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
    public CommandNode<S> build(PretendPluginManager pluginManager) {
        this.setPermissionNode(getName());
        return buildChildren(pluginManager);
    }
}
