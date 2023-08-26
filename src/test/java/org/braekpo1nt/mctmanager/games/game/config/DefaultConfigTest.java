package org.braekpo1nt.mctmanager.games.game.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class DefaultConfigTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    void setupServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
//        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void parkourPathwayDefault() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtilNull = new ParkourPathwayStorageUtil(null);
        Assertions.assertNotNull(parkourPathwayStorageUtilNull.getDefaultConfig());
        
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(parkourPathwayStorageUtil.getDefaultConfig());
    }
    
    @Test
    void parkourPathwayLoad() {
        ParkourPathwayStorageUtil parkourPathwayStorageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(parkourPathwayStorageUtil::loadConfig);
    }
    
    @Test
    void mechaDefault() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(mechaStorageUtil.getDefaultConfig());
    }
    
    @Test
    void mechaLoad() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(mechaStorageUtil::loadConfig);
    }
    
    @Test
    void spleefDefault() {
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertNotNull(spleefStorageUtil.getDefaultConfig());
    }
    
    @Test
    void spleefLoad() {
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(spleefStorageUtil::loadConfig);
    }
    
    @Test
    void malformedJsonSpleef() {
        createFileInDirectory(plugin.getDataFolder(), "spleefConfig.json", "{,");
        SpleefStorageUtil spleefStorageUtil = new SpleefStorageUtil(plugin.getDataFolder());
        Assertions.assertDoesNotThrow(spleefStorageUtil::loadConfig);
    }
    
    public static void createFileInDirectory(File directory, String fileName, String fileContents) {
        // Check if the provided "directory" is indeed a directory
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Provided file is not a directory.");
        }
        
        // Create a File object representing the new file within the specified directory
        File newFile = new File(directory, fileName);
        
        // Create the new file and write the contents
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(fileContents);
        } catch (IOException e) {
            // Handle or propagate the IOException if necessary
            Assertions.fail(String.format("Unable to create file %s in %s with contents %s", fileName, directory, fileContents));
        }
    }
    
}
