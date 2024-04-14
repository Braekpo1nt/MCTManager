package org.braekpo1nt.mctmanager.games.game.config.enchantments;

import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an enchantment with a level
 * @param enchantment the NamespacedKey associated with the enchantment
 * @param level
 */
public record EnchantmentDTO(@Nullable NamespacedKey enchantment, int level) {
    public @Nullable Enchantment toEnchantment() {
        return Enchantment.getByKey(enchantment);
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
                enchantments.put(enchantmentDTO.toEnchantment(), enchantmentDTO.level());
            }
        }
        return enchantments;
    }
    
    @SuppressWarnings("ConstantConditions")
    public void isValid() {
        Preconditions.checkArgument(enchantment != null, "enchantment can't be null");
        Preconditions.checkArgument(enchantment.namespace() != null, "namespace can't be null");
        Preconditions.checkArgument(enchantment.key() != null, "key can't be null");
        Enchantment trueEnchantment = Enchantment.getByKey(enchantment);
        Preconditions.checkArgument(trueEnchantment != null, "could not find enchantment for key %s" + enchantment);
        Preconditions.checkArgument(trueEnchantment.getStartLevel() <= level && level <= trueEnchantment.getMaxLevel(), "enchantment %s must have a level between %s and %s, inclusive. Provided %s", enchantment, trueEnchantment.getStartLevel(), trueEnchantment.getMaxLevel(), level);
    }
}
