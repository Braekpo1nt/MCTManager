package org.braekpo1nt.mctmanager.commands.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PermissionedArgumentBuilder {
    @Nullable String getPermissionNode();
    
    void setPermissionNode(@Nullable String permissionNode);
    
    @NotNull String getName();
}
