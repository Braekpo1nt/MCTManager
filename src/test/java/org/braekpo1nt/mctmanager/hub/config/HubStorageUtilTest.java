package org.braekpo1nt.mctmanager.hub.config;

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

public class HubStorageUtilTest {
    String configFileName = "hubConfig.json";
    String exampleConfigFileName = "exampleHubConfig.json";
    Main plugin;
    HubStorageUtil storageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new HubStorageUtil(plugin.getDataFolder());
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
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(exampleConfigFileName);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = storageUtil.getClass().getResourceAsStream(exampleConfigFileName);
        JsonObject json = TestUtils.inputStreamToJson(inputStream);
        JsonObject podium = new JsonObject();
        podium.addProperty("y", 50);
        json.add("podium", podium);
        json.addProperty("yValue", 100);
        TestUtils.saveJsonToFile(json, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
}
