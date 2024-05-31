package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

class ParkourPathwayConfigControllerTest {
    String configFileName = "parkourPathwayConfig.json";
    String exampleConfigFileName = "exampleParkourPathwayConfig.json";
    Main plugin;
    ParkourPathwayConfigController storageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new ParkourPathwayConfigController(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(ConfigIOException.class, storageUtil::getConfig);
    }
    
    @Test
    void malformedJson() {
        TestUtils.createFileInDirectory(plugin.getDataFolder(), configFileName, "{,");
        Assertions.assertThrows(ConfigInvalidException.class, storageUtil::getConfig);
    }
    
    @Test
    void wellFormedJsonValidData() {
        wellFormedJsonValidData(exampleConfigFileName);
    }
    
    @Test
    void testBackwardsCompatibility() {
        wellFormedJsonValidData("exampleParkourPathwayConfig_v0.1.0.json");
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
        Assertions.assertThrows(ConfigInvalidException.class, storageUtil::getConfig);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertDoesNotThrow(storageUtil::getConfig);
    }
}
