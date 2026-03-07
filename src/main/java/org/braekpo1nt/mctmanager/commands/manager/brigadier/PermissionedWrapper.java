package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PermissionedWrapper<S> {
    
    private final @NotNull ArgumentBuilder<S, ?> argument;
    @Getter
    private final @NotNull String name;
    @Setter
    private @Nullable String permissionNode;
    private final @NotNull List<PermissionedWrapper<S>> children;
    
    public static PermissionedWrapper<CommandSourceStack> literal(@NotNull String literal) {
        return new PermissionedWrapper<>(Commands.literal(literal), literal);
    }
    
    public PermissionedWrapper(@NotNull ArgumentBuilder<S, ?> argument, @NotNull String name) {
        this.argument = argument;
        this.name = name;
        this.permissionNode = name;
        this.children = new ArrayList<>();
    }
    
    public PermissionedWrapper<S> then(LiteralArgumentBuilder<S> argument) {
        return then(new PermissionedWrapper<>(argument, argument.getLiteral()));
    }
    
    public PermissionedWrapper<S> then(RequiredArgumentBuilder<S, ?> argument) {
        return then(new PermissionedWrapper<>(argument, argument.getName()));
    }
    
    public PermissionedWrapper<S> then(PermissionedWrapper<S> argument) {
        argument.setPermissionNode(String.format("%s.%s", permissionNode, argument.getName()));
        children.add(argument);
        return this;
    }
    
    public CommandNode<S> build(PretendPluginManager pluginManager) {
        if (permissionNode != null && pluginManager.getPermission(permissionNode) == null) {
            pluginManager.addPermission(new Permission(permissionNode));
        }
        for (PermissionedWrapper<S> child : children) {
            argument.then(child.build(pluginManager));
        }
        return argument.build();
    }
}
