package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

class CaptureTheFlagControllerTest {
    String configFileName = "captureTheFlagConfig.json";
    String exampleConfigFileName = "exampleCaptureTheFlagConfig.json";
    Main plugin;
    CaptureTheFlagConfigController controller;
    File configFolder;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new CaptureTheFlagConfigController(plugin.getDataFolder(), GameType.CAPTURE_THE_FLAG.getId());
        configFolder = new File(plugin.getDataFolder(), GameType.CAPTURE_THE_FLAG.getId());
        configFolder.mkdirs();
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(ConfigIOException.class, () -> controller.getConfig(configFileName));
    }
    
    @Test
    void malformedJson() {
        TestUtils.createFileInDirectory(configFolder, configFileName, "{,");
        Assertions.assertThrows(ConfigInvalidException.class, () -> controller.getConfig(configFileName));
    }
    
    @Test
    void wellFormedJsonValidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        TestUtils.copyInputStreamToFile(inputStream, new File(configFolder, configFileName));
        Assertions.assertDoesNotThrow(() -> controller.getConfig(configFileName));
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        JsonObject spectatorArea = new JsonObject();
        spectatorArea.addProperty("minX", 0);
        spectatorArea.addProperty("minY", 0);
        spectatorArea.addProperty("minZ", 0);
        spectatorArea.addProperty("maxX", 0);
        spectatorArea.addProperty("maxY", 0);
        spectatorArea.addProperty("maxZ", 0);
        json.add("spectatorArea", spectatorArea);
        TestUtils.saveJsonToFile(json, new File(configFolder, configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, () -> controller.getConfig(configFileName));
    }
}
