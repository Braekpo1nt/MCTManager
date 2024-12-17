package org.braekpo1nt.mctmanager.config.dto.org.bukkit.enchantments;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
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
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey.toNamespacedKey());
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
    public void validate(@NotNull Validator validator) {
        validator.notNull(namespacedKey, "namespacedKey");
        namespacedKey.validate(validator.path("namespacedKey"));
        Enchantment trueEnchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey.toNamespacedKey());
        validator.validate(trueEnchantment != null, "namespacedKey: could not find enchantment for key \"%s\"" + namespacedKey.toNamespacedKey());
    }
    
}
