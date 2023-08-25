package org.braekpo1nt.mctmanager.games.game.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfig;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.logging.Level;

public class DefaultConfigTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    void setupServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
    }
    
    @Test
    void parkourPathway() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin);
        Assertions.assertDoesNotThrow(() -> {
            ParkourPathwayConfig defaultConfig = parkourPathwayStorageUtil.getDefaultConfig();
            Assertions.assertNotNull(defaultConfig.getCheckpoints());
            Assertions.assertNotNull(defaultConfig.getWorld());
        });
    }
    
    @Test
    void mecha() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin);
        Assertions.assertDoesNotThrow(() -> {
            MechaConfig defaultConfig = mechaStorageUtil.getDefaultConfig();
            Assertions.assertNotNull(defaultConfig.spawnLootTable());
            Assertions.assertNotNull(defaultConfig.weightedMechaLootTables());
            Assertions.assertNotNull(defaultConfig.borderStages());
            Assertions.assertNotNull(defaultConfig.spawnChestCoords());
            Assertions.assertNotNull(defaultConfig.mapChestCoords());
        });
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
}
