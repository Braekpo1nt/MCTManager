package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PermissionedCommandNode {
    @Nullable String getPermissionNode();
    
    @NotNull Collection<PermissionedCommandNode> getPermissionedChildren();
}
