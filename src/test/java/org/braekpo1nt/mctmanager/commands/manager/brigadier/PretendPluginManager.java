package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginManagerMock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Used to check if the right permission nodes are being created
 */
class PretendPluginManager extends PluginManagerMock {
    
    private final Map<String, Permission> permissionNodes = new HashMap<>();
    
    public PretendPluginManager() {
        super(mock(ServerMock.class));
    }
    
    @Override
    public Permission getPermission(@NotNull String permissionNode) {
        return permissionNodes.get(permissionNode);
    }
    
    @Override
    public void addPermission(@NotNull Permission permission) {
        permissionNodes.put(permission.getName(), permission);
    }
    
    public Map<String, Permission> getPermissionNodes() {
        return permissionNodes;
    }
}
