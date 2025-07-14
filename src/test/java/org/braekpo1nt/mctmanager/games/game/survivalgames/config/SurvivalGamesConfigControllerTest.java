package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

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
import java.io.InputStream;
import java.util.logging.Level;

class SurvivalGamesConfigControllerTest {
    String configFileName = "survivalGamesConfig.json";
    String exampleConfigFileName = "exampleSurvivalGamesConfig.json";
    Main plugin;
    SurvivalGamesConfigController controller;
    File configFolder;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new SurvivalGamesConfigController(plugin.getDataFolder(), GameType.SURVIVAL_GAMES.getId());
        configFolder = new File(plugin.getDataFolder(), GameType.SURVIVAL_GAMES.getId());
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
        wellFormedJsonValidData(exampleConfigFileName);
    }
    
    @Test
    void fileDoesNotExist() {
        Assertions.assertThrows(ConfigIOException.class, () -> controller.getConfig(configFileName));
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = controller.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        json.add("mapChestCoords", null);
        TestUtils.saveJsonToFile(json, new File(configFolder, configFileName));
        Assertions.assertThrows(ConfigInvalidException.class, () -> controller.getConfig(configFileName));
    }
    
    void wellFormedJsonValidData(String filename) {
        wellFormedJsonValidData(filename, configFileName);
    }
    
    void wellFormedJsonValidData(String filename, String configFilename) {
        InputStream inputStream = controller.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(configFolder, configFilename));
        Assertions.assertDoesNotThrow(() -> controller.getConfig(configFileName));
    }
    
}
