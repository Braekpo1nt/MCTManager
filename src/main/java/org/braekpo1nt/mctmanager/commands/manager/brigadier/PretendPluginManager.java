package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

public class PretendPluginManager {
    
    private final Map<String, Permission> permissionNodes = new HashMap<>();
    
    public Permission getPermission(String permissionNode) {
        return permissionNodes.get(permissionNode);
    }
    
    public void addPermission(Permission permission) {
        permissionNodes.put(permission.getName(), permission);
    }
    
    public Map<String, Permission> getPermissionNodes() {
        return permissionNodes;
    }
    
}
