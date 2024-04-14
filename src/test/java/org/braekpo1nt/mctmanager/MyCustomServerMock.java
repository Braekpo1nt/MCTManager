package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.inventory.InventoryMock;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.enchantments.MyEnchantmentMock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a custom implementation of {@link ServerMock}. Some main things that needed to happen were:
 * - Implement methods that the original {@link ServerMock} has not yet implemented (they throw {@link be.seeseemelk.mockbukkit.UnimplementedOperationException})
 * - Create an initial test world called "TestWorld" which is used in many tests
 */
public class MyCustomServerMock extends ServerMock {
    
    static {
        NamespacedKey key = NamespacedKey.minecraft("efficiency");
        Enchantment.registerEnchantment(new MyEnchantmentMock(key, "efficiency", 0, 5));
    }
    
    private static void reRegister(@NotNull String name, int startLevel, int maxLevel) {
        NamespacedKey key = NamespacedKey.minecraft(name);
        Enchantment enchantment = Enchantment.getByKey(key);
        if (enchantment != null) {
            if (enchantment instanceof EnchantmentMock enchantmentMock) {
                enchantmentMock.setStartLevel(startLevel);
                enchantmentMock.setMaxLevel(maxLevel);
            }
        }
    }
    
    public MyCustomServerMock() {
        super();
        reRegister("protection", 0, 1);
        reRegister("fire_protection", 0, 1);
        reRegister("feather_falling", 0, 1);
        reRegister("blast_protection", 0, 1);
        reRegister("respiration", 0, 1);
        reRegister("projectile_protection", 0, 1);
        reRegister("aqua_affinity", 0, 1);
        reRegister("thorns", 0, 1);
        reRegister("depth_strider", 0, 1);
        reRegister("frost_walker", 0, 1);
        reRegister("binding_curse", 0, 1);
        reRegister("sharpness", 0, 1);
        reRegister("smite", 0, 1);
        reRegister("bane_of_arthropods", 0, 1);
        reRegister("knockback", 0, 1);
        reRegister("fire_aspect", 0, 1);
        reRegister("looting", 0, 1);
        reRegister("sweeping", 0, 1);
        reRegister("efficiency", 0, 5);
        reRegister("silk_touch", 0, 1);
        reRegister("unbreaking", 0, 1);
        reRegister("fortune", 0, 1);
        reRegister("power", 0, 1);
        reRegister("punch", 0, 1);
        reRegister("flame", 0, 1);
        reRegister("infinity", 0, 1);
        reRegister("luck_of_the_sea", 0, 1);
        reRegister("lure", 0, 1);
        reRegister("loyalty", 0, 1);
        reRegister("impaling", 0, 1);
        reRegister("riptide", 0, 1);
        reRegister("channeling", 0, 1);
        reRegister("multishot", 0, 1);
        reRegister("quick_charge", 0, 1);
        reRegister("piercing", 0, 1);
        reRegister("mending", 0, 1);
        reRegister("vanishing_curse", 0, 1);
        reRegister("swift_sneak", 0, 1);
    }
    
    @Override
    public World getWorld(String name) {
        initializeTestWorld();
        return super.getWorld(name);
    }
    
    @Override
    public World getWorld(UUID uid) {
        initializeTestWorld();
        return super.getWorld(uid);
    }
    
    @Override
    public @NotNull List<World> getWorlds() {
        initializeTestWorld();
        return super.getWorlds();
    }
    
    private void initializeTestWorld() {
        if (super.getWorld("TestWorld") == null) {
            WorldMock worldMock = new WorldMock(Material.GRASS, -64, 320, 4);
            worldMock.setName("TestWorld");
            addWorld(worldMock);
        }
    }
    
    /**
     * Returns a mocked version of LootTable. Nothing is implemented, if it's attempted to be used, it will fail.
     * @param key the name of the LootTable
     * @return A mocked version of LootTable
     */
    @Override
    public LootTable getLootTable(NamespacedKey key) {
        return mock(LootTable.class);
    }
    
    @Override
    public @NotNull StructureManager getStructureManager() {
        StructureManager mockStructureManager = mock(StructureManager.class);
        when(mockStructureManager.loadStructure((NamespacedKey) any())).thenReturn(mock(Structure.class));
        return mockStructureManager;
    }
    
    @Override
    public @NotNull InventoryMock createInventory(@Nullable InventoryHolder owner, int size, @NotNull Component title) throws IllegalArgumentException {
        return new InventoryMock(owner, size, InventoryType.PLAYER);
    }
    
    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        List<Audience> audiences = new ArrayList<>(this.getOnlinePlayers());
        audiences.add(this.getConsoleSender());
        return audiences;
    }
}
