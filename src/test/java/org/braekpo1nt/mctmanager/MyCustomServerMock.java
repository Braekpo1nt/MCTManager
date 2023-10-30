package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.inventory.InventoryMock;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
