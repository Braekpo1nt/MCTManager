package org.braekpo1nt.mctmanager.games.game.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;

public class DefaultConfigTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    void setupServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void spleefLoad() {
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertFalse(spleefStorageUtil.loadConfig());
    }
    
    @Test
    void spleefMalformedJson() {
        createFileInDirectory(new File(plugin.getDataFolder(), "spleef.json"), "spleefConfig.json", "{,");
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertFalse(spleefStorageUtil.loadConfig());
    }
    
    @Test
    void spleefWellFormedJson() {
        InputStream inputStream = SpleefStorageUtil.class.getResourceAsStream("defaultSpleefConfig.json");
        copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "spleefConfig.json"));
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertTrue(spleefStorageUtil.loadConfig());
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
