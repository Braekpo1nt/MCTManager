package org.braekpo1nt.mctmanager.commands.manager;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.PermissionedCommandNode;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PermissionedLiteralArgumentBuilder<S> extends ArgumentBuilder<S, PermissionedLiteralArgumentBuilder<S>> implements PermissionedArgumentBuilder {
    
    private final @NotNull String literal;
    private final @NotNull PluginManager pluginManager;
    private @Nullable String permissionNode;
    
    protected PermissionedLiteralArgumentBuilder(@NotNull String literal, @NotNull PluginManager pluginManager) {
        this.literal = literal;
        this.pluginManager = pluginManager;
        this.permissionNode = literal;
    }
    
    public static PermissionedLiteralArgumentBuilder<CommandSourceStack> literal(String literal, @NotNull PluginManager pluginManager) {
        return new PermissionedLiteralArgumentBuilder<>(literal, pluginManager);
    }
    
    @Override
    protected PermissionedLiteralArgumentBuilder<S> getThis() {
        return this;
    }
    
    @Override
    public void setPermissionNode(@Nullable String permissionNode) {
        this.permissionNode = permissionNode;
    }
    
    @Override
    public @Nullable String getPermissionNode() {
        return permissionNode;
    }
    
    public @NotNull String getLiteral() {
        return literal;
    }
    
    @Override
    public @NotNull String getName() {
        return getLiteral();
    }
    
    @Override
    public PermissionedLiteralArgumentBuilder<S> then(ArgumentBuilder<S, ?> argument) {
        if (argument instanceof PermissionedArgumentBuilder permArg) {
            permArg.setPermissionNode(String.format("%s.%s", getPermissionNode(), permArg.getName()));
        }
        return super.then(argument);
    }
    
    @Override
    public Predicate<S> getRequirement() {
        if (permissionNode == null) {
            return super.getRequirement();
        }
        return s -> {
            if (!(s instanceof CommandSourceStack source)) {
                return super.getRequirement().test(s);
            }
            return source.getSender().hasPermission(permissionNode) && super.getRequirement().test(s);
        };
    }
    
    /**
     * Register all the downstream permissions as well as this {@link MasterCommandManager}'s permissions (if not
     * already registered).
     * @param pluginManager the pluginManager to register the permissions with
     */
    private void registerPermissions(@NotNull PluginManager pluginManager) {
        if (getPermissionNode() != null && pluginManager.getPermission(getPermissionNode()) == null) {
            Main.logf("registering permission: %s", getPermissionNode());
            pluginManager.addPermission(new Permission(getPermissionNode()));
        }
//        for (CommandNode<S> argument : getArguments()) {
//            if (argument instanceof PermissionedCommandNode permedArg) {
//                registerPermissions(pluginManager, permedArg);
//            }
//        }
    }
    
    private void registerPermissions(@NotNull PluginManager pluginManager, @NotNull PermissionedCommandNode permedArg) {
        if (permedArg.getPermissionNode() != null) {
            Main.logf("registering permission: %s", getPermissionNode());
            pluginManager.addPermission(new Permission(permedArg.getPermissionNode()));
        }
        for (PermissionedCommandNode child : permedArg.getPermissionedChildren()) {
            if (child instanceof PermissionedCommandNode permedChild) {
                registerPermissions(pluginManager, permedChild);
            }
        }
    }
    
    @Override
    public LiteralCommandNode<S> build() {
        registerPermissions(pluginManager);
        
        final LiteralCommandNode<S> result = new PermissionedLiteralCommandNode<>(getLiteral(), getCommand(), permissionNode, getRequirement(), getRedirect(), getRedirectModifier(), isFork());
        
        for (final CommandNode<S> argument : getArguments()) {
            result.addChild(argument);
        }
        
        return result;
    }
}
