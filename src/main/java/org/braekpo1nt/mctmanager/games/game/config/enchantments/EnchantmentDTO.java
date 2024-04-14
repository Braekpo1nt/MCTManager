package org.braekpo1nt.mctmanager.games.game.config.enchantments;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
public class EnchantmentDTO {
    /**
     * the NamespacedKey associated with the enchantment
     */
    private @Nullable NamespacedKey namespacedKey;
    /**
     * the level of the enchantment (defaults to 1)
     */
    private int level = 1;
    
    public @Nullable Enchantment toEnchantment() {
        return Enchantment.getByKey(namespacedKey);
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
    
    @SuppressWarnings("ConstantConditions")
    public void isValid() {
        Preconditions.checkArgument(namespacedKey != null, "enchantment can't be null");
        Preconditions.checkArgument(namespacedKey.namespace() != null, "namespace can't be null");
        Preconditions.checkArgument(namespacedKey.key() != null, "key can't be null");
        Enchantment trueEnchantment = Enchantment.getByKey(namespacedKey);
        Preconditions.checkArgument(trueEnchantment != null, "could not find enchantment for key %s" + namespacedKey);
    }
}
