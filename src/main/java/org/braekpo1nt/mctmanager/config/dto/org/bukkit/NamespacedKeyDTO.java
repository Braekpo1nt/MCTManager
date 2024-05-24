package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class NamespacedKeyDTO implements Validatable {
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
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(key, "key");
    }
    
    /**
     * @deprecated in favor of {@link NamespacedKeyDTO#validate(Validator)} 
     */
    @Deprecated
    public void isValid() {
        Preconditions.checkArgument(key != null, "key can't be null");
    }
    
}
