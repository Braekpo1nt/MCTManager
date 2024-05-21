package org.braekpo1nt.mctmanager.config.dto;

import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NamespacedKeyDTO {
    private @Nullable String namespace;
    private @Nullable String key;
    
    public @Nullable String namespace() {
        return namespace;
    }
    
    public @Nullable String key() {
        return key;
    }
    
    /**
     * @return the NamespacedKey represented by this DTO. If namespace is null, the default "minecraft" will be used.
     * @throws IllegalArgumentException if key is null
     */
    public @NotNull NamespacedKey toNamespacedKey() {
        Preconditions.checkArgument(key != null, "key can't be null");
        if (namespace == null) {
            return NamespacedKey.minecraft(key);
        }
        return new NamespacedKey(namespace, key);
    }
    
    public void isValid() {
        Preconditions.checkArgument(key != null, "key can't be null");
    }
    
}
