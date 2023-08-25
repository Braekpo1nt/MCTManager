package org.braekpo1nt.mctmanager.games.game.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void parkourPathwayDefault() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtilNull = new ParkourPathwayStorageUtil(null);
        Assertions.assertNotNull(parkourPathwayStorageUtilNull.getDefaultConfig());
        
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(parkourPathwayStorageUtil.getDefaultConfig());
    }
    
    @Test
    void parkourPathwayLoad() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(parkourPathwayStorageUtil::loadConfig);
    }
    
    @Test
    void mechaDefault() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(mechaStorageUtil.getDefaultConfig());
    }
    
    @Test
    void mechaLoad() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(mechaStorageUtil::loadConfig);
    }
    
    @Test
    void spleefDefault() {
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(spleefStorageUtil.getDefaultConfig());
    }
    
    @Test
    void spleefLoad() {
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(spleefStorageUtil::loadConfig);
    }
    
}
