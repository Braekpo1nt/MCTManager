package org.braekpo1nt.mctmanager.enchantments;

import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyEnchantmentMock extends EnchantmentMock {
    private final List<Material> validTypes;
    
    /**
     * Constructs a new {@link EnchantmentMock} with the provided {@link NamespacedKey} and name.
     *
     * @param key  The key for the enchantment.
     * @param name The name of the enchantment.
     * @param validTypes a list of materials which this enchantment is allowed to be applied to
     */
    public MyEnchantmentMock(@NotNull NamespacedKey key, @NotNull String name, int maxLevel, @NotNull List<@NotNull Material> validTypes) {
        super(key, name);
        this.validTypes = validTypes;
        setStartLevel(1);
        setMaxLevel(maxLevel);
    }
    
    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");
        if (validTypes == null || validTypes.isEmpty()) {
            return true;
        }
        return this.validTypes.contains(item.getType());
    }
}
