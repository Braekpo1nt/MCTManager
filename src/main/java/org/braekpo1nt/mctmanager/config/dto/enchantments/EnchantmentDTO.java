package org.braekpo1nt.mctmanager.config.dto.enchantments;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.dto.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.validation.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an enchantment with a level
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EnchantmentDTO implements Validatable {
    /**
     * the NamespacedKey associated with the enchantment
     */
    private @Nullable NamespacedKeyDTO namespacedKey;
    /**
     * the level of the enchantment (defaults to 1)
     */
    private int level = 1;
    
    public @Nullable Enchantment toEnchantment() {
        if (namespacedKey == null) {
            throw new IllegalStateException("namespaceKey is null");
        }
        return Enchantment.getByKey(namespacedKey.toNamespacedKey());
    }
    
    /**
     * 
     * @param enchantmentDTOs a list of EnchantmentDTOs
     * @return a non-null map of the equivalent enchantments to their respective levels. Any null entries in enchantmentDTOs is ignored. 
     */
    public static @NotNull Map<@NotNull Enchantment, @NotNull Integer> toEnchantments(@NotNull List<@Nullable EnchantmentDTO> enchantmentDTOs) {
        Map<Enchantment, Integer> enchantments = new HashMap<>(enchantmentDTOs.size());
        for (EnchantmentDTO enchantmentDTO : enchantmentDTOs) {
            if (enchantmentDTO != null) {
                enchantments.put(enchantmentDTO.toEnchantment(), enchantmentDTO.getLevel());
            }
        }
        return enchantments;
    }
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(namespacedKey, "namespacedKey");
        namespacedKey.validate(validator.path("namespacedKey"));
        Enchantment trueEnchantment = Enchantment.getByKey(namespacedKey.toNamespacedKey());
        validator.validate(trueEnchantment != null, "namespacedKey: could not find enchantment for key \"%s\"" + namespacedKey.toNamespacedKey());
    }
    
    /**
     * @deprecated in favor of {@link Validatable}
     */
    @Deprecated
    public void isValid() {
        Preconditions.checkArgument(namespacedKey != null, "enchantment can't be null");
        Preconditions.checkArgument(namespacedKey.namespace() != null, "namespace can't be null");
        Preconditions.checkArgument(namespacedKey.key() != null, "key can't be null");
        Enchantment trueEnchantment = Enchantment.getByKey(namespacedKey.toNamespacedKey());
        Preconditions.checkArgument(trueEnchantment != null, "could not find enchantment for key %s" + namespacedKey);
    }
}
