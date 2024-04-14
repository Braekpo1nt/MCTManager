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
import java.util.Collections;
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
    }
    
    private static void reRegister(@NotNull String name, int maxLevel, @NotNull List<@NotNull Material> validItemTypes) {
        NamespacedKey key = NamespacedKey.minecraft(name);
        Enchantment.registerEnchantment(new MyEnchantmentMock(key, name, 5, validItemTypes));
    }
    
    public MyCustomServerMock() {
        super();
        reRegister("aqua_affinity", 1, List.of(Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET));
        reRegister("bane_of_arthropods", 5, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("blast_protection", 4, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
        reRegister("binding_curse", 1, Collections.emptyList());
        reRegister("vanishing_curse", 1, Collections.emptyList());
        reRegister("channeling", 1, List.of(Material.TRIDENT));
        reRegister("cleaving", 3, List.of(Material.WOODEN_AXE,Material.STONE_AXE,Material.GOLDEN_AXE,Material.IRON_AXE,Material.DIAMOND_AXE,Material.NETHERITE_AXE));
        reRegister("depth_strider", 3, List.of(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
        reRegister("efficiency", 5, List.of(Material.WOODEN_PICKAXE,Material.STONE_PICKAXE,Material.GOLDEN_PICKAXE,Material.IRON_PICKAXE,Material.DIAMOND_PICKAXE,Material.NETHERITE_PICKAXE,Material.WOODEN_SHOVEL,Material.STONE_SHOVEL,Material.GOLDEN_SHOVEL,Material.IRON_SHOVEL,Material.DIAMOND_SHOVEL,Material.NETHERITE_SHOVEL,Material.WOODEN_AXE,Material.STONE_AXE,Material.GOLDEN_AXE,Material.IRON_AXE,Material.DIAMOND_AXE,Material.NETHERITE_AXE,Material.WOODEN_HOE,Material.STONE_HOE,Material.GOLDEN_HOE,Material.IRON_HOE,Material.DIAMOND_HOE,Material.NETHERITE_HOE));
        reRegister("feather_falling", 4, List.of());
        reRegister("fire_aspect", 2, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("fire_protection", 4, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
        reRegister("flame", 1, List.of(Material.BOW));
        reRegister("fortune", 3, List.of(Material.WOODEN_PICKAXE,Material.STONE_PICKAXE,Material.GOLDEN_PICKAXE,Material.IRON_PICKAXE,Material.DIAMOND_PICKAXE,Material.NETHERITE_PICKAXE,Material.WOODEN_SHOVEL,Material.STONE_SHOVEL,Material.GOLDEN_SHOVEL,Material.IRON_SHOVEL,Material.DIAMOND_SHOVEL,Material.NETHERITE_SHOVEL,Material.WOODEN_AXE,Material.STONE_AXE,Material.GOLDEN_AXE,Material.IRON_AXE,Material.DIAMOND_AXE,Material.NETHERITE_AXE,Material.WOODEN_HOE,Material.STONE_HOE,Material.GOLDEN_HOE,Material.IRON_HOE,Material.DIAMOND_HOE,Material.NETHERITE_HOE));
        reRegister("frost_walker", 2, List.of());
        reRegister("impaling", 5, List.of(Material.TRIDENT));
        reRegister("infinity", 1, List.of(Material.BOW));
        reRegister("knockback", 2, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("looting", 3, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("loyalty", 3, List.of(Material.TRIDENT));
        reRegister("luck_of_the_sea", 3, List.of(Material.FISHING_ROD));
        reRegister("lure", 3, List.of(Material.FISHING_ROD));
        reRegister("mending", 1, Collections.emptyList());
        reRegister("multishot", 1, List.of(Material.CROSSBOW));
        reRegister("piercing", 4, List.of(Material.CROSSBOW));
        reRegister("power", 5, List.of(Material.BOW));
        reRegister("projectile_protection", 4, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
        reRegister("protection", 4, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
        reRegister("punch", 2, List.of(Material.BOW));
        reRegister("respiration", 3, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET));
        reRegister("riptide", 3, List.of(Material.TRIDENT));
        reRegister("sharpness", 5, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("silk_touch", 1, List.of(Material.WOODEN_PICKAXE,Material.STONE_PICKAXE,Material.GOLDEN_PICKAXE,Material.IRON_PICKAXE,Material.DIAMOND_PICKAXE,Material.NETHERITE_PICKAXE,Material.WOODEN_SHOVEL,Material.STONE_SHOVEL,Material.GOLDEN_SHOVEL,Material.IRON_SHOVEL,Material.DIAMOND_SHOVEL,Material.NETHERITE_SHOVEL,Material.WOODEN_AXE,Material.STONE_AXE,Material.GOLDEN_AXE,Material.IRON_AXE,Material.DIAMOND_AXE,Material.NETHERITE_AXE,Material.WOODEN_HOE,Material.STONE_HOE,Material.GOLDEN_HOE,Material.IRON_HOE,Material.DIAMOND_HOE,Material.NETHERITE_HOE));
        reRegister("smite", 5, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("soul_speed", 3, List.of());
        reRegister("sweeping", 3, List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD));
        reRegister("swift_sneak", 3, List.of());
        reRegister("thorns", 3, List.of(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE));
        reRegister("unbreaking", 3, List.of(Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS, Material.WOODEN_PICKAXE,Material.STONE_PICKAXE,Material.GOLDEN_PICKAXE,Material.IRON_PICKAXE,Material.DIAMOND_PICKAXE,Material.NETHERITE_PICKAXE,Material.WOODEN_SHOVEL,Material.STONE_SHOVEL,Material.GOLDEN_SHOVEL,Material.IRON_SHOVEL,Material.DIAMOND_SHOVEL,Material.NETHERITE_SHOVEL,Material.WOODEN_AXE,Material.STONE_AXE,Material.GOLDEN_AXE,Material.IRON_AXE,Material.DIAMOND_AXE,Material.NETHERITE_AXE,Material.WOODEN_HOE,Material.STONE_HOE,Material.GOLDEN_HOE,Material.IRON_HOE,Material.DIAMOND_HOE,Material.NETHERITE_HOE, Material.TRIDENT, Material.BOW, Material.FISHING_ROD, Material.CROSSBOW));
        reRegister("quick_charge", 3, List.of(Material.CROSSBOW));
    
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
