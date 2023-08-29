package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.inventory.InventoryMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a custom implementation of {@link ServerMock}. Some main things that needed to happen were:
 * - Supply a mock version of the Multiverse-Core plugin, which this plugin depends on
 * - Implement methods that the original {@link ServerMock} has not yet implemented (they throw {@link be.seeseemelk.mockbukkit.UnimplementedOperationException})
 */
public class MyCustomServerMock extends ServerMock {
    PluginManagerMock myCustomPluginManagerMock = new MyCustomPluginManagerMock(this);
    
    @Override
    public @NotNull PluginManagerMock getPluginManager() {
        return myCustomPluginManagerMock;
    }
    
    /**
     * Returns a mocked version of LootTable. Nothing is implemented, if it's attempted to be used, it will fail.
     * @param key the name of the LootTable
     * @return A mocked version of LootTable
     */
    @Override
    public LootTable getLootTable(NamespacedKey key) {
        LootTable lootTable = mock(LootTable.class);
        return lootTable;
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
    
    /**
     * A custom implementation of {@link PluginManagerMock}. Some key things it does are:
     * - Override {@link MyCustomPluginManagerMock#getPlugin(String)} to get a mocked Multiverse-Core plugin
     */
    public static class MyCustomPluginManagerMock extends PluginManagerMock {
    
        /**
         * A reference to the server that this PluginManager mock belongs to.
         * Used for
         * - Creating WorldMocks using the {@link ServerMock#addSimpleWorld(String)} method
         */
        private final ServerMock server;
        /**
         * Constructs a new {@link PluginManagerMock} for the provided {@link ServerMock}.
         * 
         * @param server The server this is for.
         */
        public MyCustomPluginManagerMock(@NotNull ServerMock server) {
            super(server);
            // Keep a reference to the server so that it can be used for things like creating simple worlds
            this.server = server;
        }
    
        /**
         * Returns a mocked {@link MultiverseCore} if name is "Multiverse-Core", and otherwise
         * returns the super method's result.
         * @param name Name of the plugin to check
         * @return Mocked {@link MultiverseCore} if name="Multiverse-Core", super method otherwise
         */
        @Override
        public Plugin getPlugin(@NotNull String name) {
            if (name.equals("Multiverse-Core")) {
                MVWorldManager mockMVWorldManager = mock(MVWorldManager.class);
                MultiverseWorld mvWorldMock = mock(MultiverseWorld.class);
                WorldMock worldMock = new WorldMock(Material.GRASS, -64, 320, 4);
                server.addWorld(worldMock);
                when(mvWorldMock.getCBWorld()).thenReturn(worldMock);
                when(mockMVWorldManager.getMVWorld(anyString())).thenReturn(mvWorldMock);
                
                AnchorManager mockAnchorManager = mock(AnchorManager.class);
                when(mockAnchorManager.getAnchorLocation(anyString())).thenReturn(new Location(worldMock, 0, 0, 0));
                
                MultiverseCore mockMultiverseCore = mock(MultiverseCore.class);
                when(mockMultiverseCore.getMVWorldManager()).thenReturn(mockMVWorldManager);
                when(mockMultiverseCore.getAnchorManager()).thenReturn(mockAnchorManager);
                return mockMultiverseCore;
            }
            return super.getPlugin(name);
        }
    }
}
