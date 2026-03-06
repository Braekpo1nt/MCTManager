package org.braekpo1nt.mctmanager.commands.manager;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.PermissionedCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class PermissionedLiteralCommandNode<S> extends LiteralCommandNode<S> implements PermissionedCommandNode {
    
    private final @Nullable String permissionNode;
    
    public PermissionedLiteralCommandNode(String literal, Command<S> command, @Nullable String permissionNode, Predicate<S> requirement, CommandNode<S> redirect, RedirectModifier<S> modifier, boolean forks) {
        super(literal, command, requirement, redirect, modifier, forks);
        this.permissionNode = permissionNode;
    }
    
    @Override public @Nullable String getPermissionNode() {
        return permissionNode;
    }
    
    @Override
    public @NotNull Collection<PermissionedCommandNode> getPermissionedChildren() {
        return getChildren().stream()
                .filter(child -> child instanceof PermissionedCommandNode)
                .map(child -> (PermissionedCommandNode) child)
                .toList();
    }
}
