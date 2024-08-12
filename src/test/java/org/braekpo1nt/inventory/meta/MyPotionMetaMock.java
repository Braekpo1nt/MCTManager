package org.braekpo1nt.inventory.meta;

import be.seeseemelk.mockbukkit.inventory.meta.PotionMetaMock;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyPotionMetaMock extends PotionMetaMock {
    protected @Nullable PotionType basePotionType;
    
    @Override
    public void setBasePotionType(@NotNull PotionType type) {
        this.basePotionType = type;
    }
}
