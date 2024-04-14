package org.braekpo1nt.mctmanager.enchantments;

import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MyEnchantmentMock extends EnchantmentMock {
    /**
     * Constructs a new {@link EnchantmentMock} with the provided {@link NamespacedKey} and name.
     *
     * @param key  The key for the enchantment.
     * @param name The name of the enchantment.
     */
    public MyEnchantmentMock(@NotNull NamespacedKey key, @NotNull String name, int startLevel, int maxLevel) {
        super(key, name);
        setStartLevel(startLevel);
        setMaxLevel(maxLevel);
    }
    
    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");
        return true;
    }
}
