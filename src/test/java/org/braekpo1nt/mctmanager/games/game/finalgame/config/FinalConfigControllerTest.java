package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

class FinalConfigControllerTest {
    String configFileName = "colossalCombatConfig.json";
    String exampleConfigFileName = "exampleColossalCombatConfig.json";
    Main plugin;
    FinalConfigController controller;
    File configFolder;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(MockMain.class);
        controller = new FinalConfigController(plugin.getDataFolder(), GameType.FINAL.getId());
        configFolder = new File(plugin.getDataFolder(), GameType.FINAL.getId());
        configFolder.mkdirs();
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void wellFormedJsonValidData() {
        wellFormedJsonValidData(exampleConfigFileName);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = controller.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(configFolder, configFileName));
        Assertions.assertDoesNotThrow(() -> controller.getConfig(configFileName));
    }
}
