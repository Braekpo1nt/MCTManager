package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class FarmRushConfigControllerTest {
    String configFileName = "farmRushConfig.json";
    String exampleConfigFileName = "exampleFarmRushConfig.json";
    Main plugin;
    FarmRushConfigController controller;
    File configFolder;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new FarmRushConfigController(plugin.getDataFolder(), GameType.FARM_RUSH.getId());
        configFolder = new File(plugin.getDataFolder(), GameType.FARM_RUSH.getId());
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
        // TODO: change the exclusion of the materialsBook when MockBukkit implements BookMeta.toBuilder()
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        json.addProperty("doNotGiveBookDebug", true);
        TestUtils.saveJsonToFile(json, new File(configFolder, configFileName));
        Assertions.assertDoesNotThrow(() -> controller.getConfig(configFileName));
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        json.remove("starterChestContents");
        TestUtils.saveJsonToFile(json, new File(configFolder, configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, () -> controller.getConfig(configFileName));
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = controller.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(configFolder, configFileName));
        Assertions.assertDoesNotThrow(() -> controller.getConfig(configFileName));
    }
}
