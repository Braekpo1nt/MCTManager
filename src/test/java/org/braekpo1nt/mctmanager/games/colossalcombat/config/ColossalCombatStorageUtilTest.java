package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
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

public class ColossalCombatStorageUtilTest {
    String configFileName = "colossalCombatConfig.json";
    String exampleConfigFileName = "exampleColossalCombatConfig.json";
    Main plugin;
    ColossalCombatStorageUtil storageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new ColossalCombatStorageUtil(plugin.getDataFolder());
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
        wellFormedJsonValidData("exampleColossalCombatConfig_v0.1.0.json");
    }

    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        json.addProperty("version", "0.0.0");
        TestUtils.saveJsonToFile(json, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
    
    void wellFormedJsonValidData(String filename) {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(filename);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }
}
