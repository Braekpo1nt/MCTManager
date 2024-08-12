package org.braekpo1nt.inventory;

import be.seeseemelk.mockbukkit.inventory.ItemStackMock;
import be.seeseemelk.mockbukkit.inventory.ItemTypeMock;
import be.seeseemelk.mockbukkit.inventory.meta.ItemMetaMock;
import org.braekpo1nt.inventory.meta.MyPotionMetaMock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class MyItemStackMock extends ItemStackMock {
    private ItemType type = ItemTypeMock.AIR;
    private int amount = 1;
    private ItemMeta itemMeta = new ItemMetaMock();
    private short durability;
    
    private static final MyItemStackMock EMPTY = new MyItemStackMock((Void) null);
    private static final String ITEMMETA_INITIALIZATION_ERROR = "Failed to instanciate item meta class ";
    
    public MyItemStackMock(@NotNull Material type) {
        this(type, 1);
    }
    
    public MyItemStackMock(@NotNull ItemStack stack) throws IllegalArgumentException {
//        this.type = switch (stack.getType()) {
//            case POTION, LINGERING_POTION, SPLASH_POTION -> new MyPotionMetaMock();
//            default -> stack.getType().asItemType();
//        };
        this.type = stack.getType().asItemType();
        this.amount = stack.getAmount();
        this.durability = type.getMaxDurability();
        if (type.asMaterial() != Material.AIR && type.getItemMetaClass() != ItemMeta.class)
        {
            try
            {
                this.itemMeta = type.getItemMetaClass().getConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                   NoSuchMethodException e)
            {
                throw new RuntimeException(ITEMMETA_INITIALIZATION_ERROR + type.getItemMetaClass(), e);
            }
        }
    }
    
    public MyItemStackMock(@NotNull Material type, int amount) {
        
    }
    
    public MyItemStackMock(@Nullable Void v) {
        this.type = ItemTypeMock.AIR;
        this.amount = 0;
    }
    
}
