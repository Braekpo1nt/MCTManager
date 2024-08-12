package org.braekpo1nt.inventory;

import be.seeseemelk.mockbukkit.inventory.ItemFactoryMock;
import com.google.common.base.Preconditions;
import org.braekpo1nt.inventory.meta.MyPotionMetaMock;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MyItemFactoryMock extends ItemFactoryMock {
    
    @Override
    public @NotNull ItemMeta getItemMeta(@NotNull Material material) {
        Preconditions.checkNotNull(material, "Material cannot be null");
        Class<? extends ItemMeta> clazz = switch (material) {
            case POTION, LINGERING_POTION, SPLASH_POTION -> MyPotionMetaMock.class;
            default -> null;
        };
        if (clazz != null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new UnsupportedOperationException("Can't instantiate class '" + clazz + "'");
            }
        }
        return super.getItemMeta(material);
    }
}
