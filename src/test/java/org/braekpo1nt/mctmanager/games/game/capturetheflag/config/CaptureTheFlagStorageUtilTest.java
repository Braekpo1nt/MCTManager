package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

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

class CaptureTheFlagStorageUtilTest {
    String validConfigFile = "validCaptureTheFlagConfig.json";
    String invalidConfigFile = "invalidCaptureTheFlagConfig.json";
    String configFileName = "captureTheFlagConfig.json";
    Main plugin;
    CaptureTheFlagStorageUtil storageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new CaptureTheFlagStorageUtil(plugin.getDataFolder());
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
    
    @Test
    void exampleJson() {
        InputStream inputStream = storageUtil.getExampleResourceStream();
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }
}
