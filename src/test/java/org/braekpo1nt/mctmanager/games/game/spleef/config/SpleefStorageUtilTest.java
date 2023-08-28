package org.braekpo1nt.mctmanager.games.game.spleef.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.logging.Level;

public class SpleefStorageUtilTest {

    Main plugin;
    SpleefStorageUtil spleefStorageUtil;
    
    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(IllegalArgumentException.class, spleefStorageUtil::loadConfig);
    }
    
    @Test
    void spleefMalformedJson() {
        createFileInDirectory(plugin.getDataFolder(), "spleefConfig.json", "{,");
        Assertions.assertThrows(IllegalArgumentException.class, spleefStorageUtil::loadConfig);
    }
    
    @Test
    void spleefWellFormedJsonValidData() {
        InputStream inputStream = SpleefStorageUtilTest.class.getResourceAsStream("validSpleefConfig.json");
        copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "spleefConfig.json"));
        Assertions.assertTrue(spleefStorageUtil.loadConfig());
    }

    @Test
    void spleefWellFormedJsonInvalidData() {
        InputStream inputStream = SpleefStorageUtilTest.class.getResourceAsStream("invalidSpleefConfig.json");
        copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "spleefConfig.json"));
        Assertions.assertThrows(IllegalArgumentException.class, spleefStorageUtil::loadConfig);
    }
    
    public static void copyInputStreamToFile(InputStream inputStream, File destinationFile) {
        Assertions.assertNotNull(inputStream);
        try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Assertions.fail(String.format("Unable to copy stream to %s \n%s", destinationFile, e));
        }
    }
    
    public static void createFileInDirectory(File directory, String fileName, String fileContents) {
        File newFile = new File(directory, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(fileContents);
        } catch (IOException e) {
            Assertions.fail(String.format("Unable to create file %s in %s with contents %s", fileName, directory, fileContents));
        }
    }
    
}
