package org.braekpo1nt.mctmanager.games.game.mecha.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class MechaStorageUtilTest {
    String validConfigFile = "validMechaConfig.json";
    String invalidConfigFile = "invalidMechaConfig.json";
    String configFileName = "mechaConfig.json";
    Main plugin;
    MechaStorageUtil storageUtil;

    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new MechaStorageUtil(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }

    @Test
    void malformedJson() {
        TestUtils.createFileInDirectory(plugin.getDataFolder(), configFileName, "{,");
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }

    @Test
    void wellFormedJsonValidData() {
        InputStream inputStream = getClass().getResourceAsStream(validConfigFile);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }

    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = getClass().getResourceAsStream(invalidConfigFile);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
}
