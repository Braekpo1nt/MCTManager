package org.braekpo1nt.mctmanager.config.dto.org.bukkit;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamespacedKeyDTO implements Validatable {
    private @Nullable String namespace;
    private @Nullable String key;
    
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
    public void validate(@NotNull Validator validator) {
        validator.notNull(key, "key");
        validator.validate(ConfigUtils.isValidKey(key), "key must be [a-z0-9/._-]: %s", key);
        if (namespace != null) {
            validator.validate(ConfigUtils.isValidNamespace(namespace), "namespace must be [a-z0-9._-]: %s", namespace);
        }
    }
    
}
