package org.braekpo1nt.mctmanager.games.game.spleef.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.config.validation.ConfigInvalidException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.logging.Level;

class SpleefStorageUtilTest {

    String configFileName = "spleefConfig.json";
    String exampleConfigFileName = "exampleSpleefConfig.json";
    Main plugin;
    SpleefStorageUtil storageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new SpleefStorageUtil(plugin.getDataFolder());
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
        wellFormedJsonValidData(exampleConfigFileName);
    }
    
    @Test
    void testBackwardsCompatibility() {
        wellFormedJsonValidData("exampleSpleefConfig_v0.1.0.json");
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        JsonObject spectatorArea = new JsonObject();
        spectatorArea.addProperty("minX", 0);
        spectatorArea.addProperty("minY", 0);
        spectatorArea.addProperty("minZ", 0);
        spectatorArea.addProperty("maxX", 0);
        spectatorArea.addProperty("maxY", 0);
        spectatorArea.addProperty("maxZ", 0);
        json.add("spectatorArea", spectatorArea);
        TestUtils.saveJsonToFile(json, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, storageUtil::loadConfig);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }
}
