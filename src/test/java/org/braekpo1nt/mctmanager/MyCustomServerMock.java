package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a custom implementation of {@link ServerMock}. Some main things that needed to happen were:
 * - Supply a mock version of the Multiverse-Core plugin, which this plugin depends on
 * - Implement methods that the original {@link ServerMock} has not yet implemented (they throw {@link be.seeseemelk.mockbukkit.UnimplementedOperationException})
 */
public class MyCustomServerMock extends ServerMock {
    @Override
    public @NotNull PluginManagerMock getPluginManager() {
        return new MyCustomPluginManagerMock(this);
    }
    
    /**
     * Returns a mocked version of LootTable. Nothing is implemented, if it's attemted to be used, it will fail.
     * @param key the name of the LootTable
     * @return A mocked version of LootTable
     */
    @Override
    public LootTable getLootTable(NamespacedKey key) {
        LootTable lootTable = mock(LootTable.class);
        return lootTable;
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
                MultiverseWorld simpleMvWorld = mock(MultiverseWorld.class);
                WorldMock simpleWorldMock = server.addSimpleWorld("Simple Server");
                when(simpleMvWorld.getCBWorld()).thenReturn(simpleWorldMock);
                when(mockMVWorldManager.getMVWorld(anyString())).thenReturn(simpleMvWorld);
                
                AnchorManager mockAnchorManager = mock(AnchorManager.class);
                when(mockAnchorManager.getAnchorLocation(anyString())).thenReturn(new Location(simpleWorldMock, 0, 0, 0));
                
                MultiverseCore mockMultiverseCore = mock(MultiverseCore.class);
                when(mockMultiverseCore.getMVWorldManager()).thenReturn(mockMVWorldManager);
                when(mockMultiverseCore.getAnchorManager()).thenReturn(mockAnchorManager);
                return mockMultiverseCore;
            }
            return super.getPlugin(name);
        }
    }
}
