package org.braekpo1nt.mctmanager.games.game.footrace.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
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

class FootRaceConfigControllerTest {
    
    String configFileName = "footRaceConfig.json";
    String exampleConfigFileName = "exampleFootRaceConfig.json";
    Main plugin;
    FootRaceConfigController controller;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new FootRaceConfigController(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(ConfigIOException.class, controller::getConfig);
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
    void testBackwardsCompatibility_0_1_2() {
        wellFormedJsonValidData("exampleFootRaceConfig_v0.1.1.json");
    }
    
    @Test
    void testBackwardsCompatibility_0_1_1() {
        wellFormedJsonValidData("exampleFootRaceConfig_v0.1.0.json");
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        JsonObject finishLine = new JsonObject();
        finishLine.addProperty("minX", 0);
        finishLine.addProperty("minY", 0);
        finishLine.addProperty("minZ", 0);
        finishLine.addProperty("maxX", 0);
        finishLine.addProperty("maxY", 0);
        finishLine.addProperty("maxZ", 0);
        json.add("finishLine", finishLine);
        TestUtils.saveJsonToFile(json, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, controller::getConfig);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = controller.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertDoesNotThrow(controller::getConfig);
    }
    
}
