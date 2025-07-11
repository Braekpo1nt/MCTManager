package org.braekpo1nt.mctmanager.hub.config;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class HubConfigControllerTest {
    String configFileName = "hubConfig.json";
    String exampleConfigFileName = "exampleHubConfig.json";
    Main plugin;
    HubConfigController controller;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new HubConfigController(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void defaultConfigIsCreatedIfConfigDoesNotExist() {
        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertNotNull(controller.getConfig());
            Assertions.assertTrue(new File(plugin.getDataFolder(), configFileName).exists());
        });
    }
    
    @Test
    void defaultConfigIsValid() {
        Assertions.assertDoesNotThrow(() -> {
            HubConfigDTO defaultConfig = controller.createDefaultConfig();
            Assertions.assertDoesNotThrow(() -> defaultConfig.validate(new Validator("hubConfig")));
        });
    }
    
    @Test
    void malformedJson() {
        TestUtils.createFileInDirectory(plugin.getDataFolder(), configFileName, "{,");
        Assertions.assertThrows(ConfigInvalidException.class, controller::getConfig);
    }
    
    @Test
    void wellFormedJsonValidData() {
        wellFormedJsonValidData(exampleConfigFileName);
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        JsonObject podium = new JsonObject();
        podium.addProperty("y", 50);
        json.add("podium", podium);
        json.addProperty("yValue", 100);
        TestUtils.saveJsonToFile(json, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, controller::getConfig);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = controller.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertDoesNotThrow(controller::getConfig);
    }
}
