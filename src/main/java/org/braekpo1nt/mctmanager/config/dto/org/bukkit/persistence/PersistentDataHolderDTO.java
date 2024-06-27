package org.braekpo1nt.mctmanager.config.dto.org.bukkit.persistence;

import org.jetbrains.annotations.Nullable;

public interface PersistentDataHolderDTO {
    @Nullable
    PersistentDataContainerDTO getPersistentDataContainerDTO();
}
