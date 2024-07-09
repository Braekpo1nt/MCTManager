package org.braekpo1nt.mctmanager.config.dto.org.bukkit.persistence;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class PersistentDataContainerDTO implements Validatable {
    
    private @Nullable List<StringData> stringData;
    
    @Data
    public static class StringData implements Validatable {
        private @Nullable NamespacedKeyDTO namespacedKey;
        private @Nullable String value;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(namespacedKey, "key");
            namespacedKey.validate(validator.path("key"));
        }
        
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(stringData, "stringData");
        validator.validateList(stringData, "stringData");
    }
    
    /**
     * Applies the data stored in this DTO to the given real persistentDataContainer
     * @param persistentDataContainer the container to apply this DTO's data to
     */
    public void toPersistentDataContainer(PersistentDataContainer persistentDataContainer) {
        if (stringData == null) {
            return;
        }
        for (StringData data : stringData) {
            NamespacedKeyDTO keyDTO = data.getNamespacedKey();
            String value = data.getValue() == null ? "" : data.getValue();
            if (keyDTO != null) {
                Bukkit.getLogger().info(String.format("stringData.size(): %d, value: %s, namespace: %s, key: %s", stringData.size(), value, keyDTO.getNamespace(), keyDTO.getKey()));
                persistentDataContainer.set(keyDTO.toNamespacedKey(), PersistentDataType.STRING, value);
            }
        }
    }
}
