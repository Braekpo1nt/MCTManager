package org.braekpo1nt.mctmanager.enchantments;

import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MyEnchantmentMock extends EnchantmentMock {
    private final List<Material> validTypes;
    private final List<NamespacedKey> incompatibleTypes;
    
    /**
     * Constructs a new {@link EnchantmentMock} with the provided {@link NamespacedKey} and name.
     *
     * @param key  The key for the enchantment.
     * @param name The name of the enchantment.
     * @param validTypes a list of materials which this enchantment is allowed to be applied to
     */
    public MyEnchantmentMock(@NotNull NamespacedKey key, @NotNull String name, int maxLevel, @NotNull List<@NotNull Material> validTypes, @NotNull List<@NotNull NamespacedKey> incompatibleTypes) {
        super(key, false, false, 0, 0, name, new Component[0], new int[0], new int[0], false, false, Collections.emptySet());
        this.validTypes = validTypes;
        this.incompatibleTypes = incompatibleTypes;
        setStartLevel(1);
        setMaxLevel(maxLevel);
    }
    
    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");
        Set<Enchantment> enchantments = item.getEnchantments().keySet();
        for (Enchantment enchantment : enchantments) {
            if (isIncompatible(enchantment)) {
                return false;
            }
        }
        return this.validTypes.contains(item.getType());
    }
    
    private boolean isIncompatible(Enchantment enchantment) {
        return this.incompatibleTypes.contains(enchantment.getKey());
    }
}
