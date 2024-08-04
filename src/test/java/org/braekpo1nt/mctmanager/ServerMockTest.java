package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ServerMockTest {
    
    private ServerMock server;
    
    @BeforeEach
    void setUp() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Make sure the temporary files created by plugin.getDataFolder() are getting deleted on MockBukkit.unmock()")
    void getDataFolder_CleanEnvironment_CreatesTemporaryDataDirectory() throws IOException {
        Main plugin = MockBukkit.load(MockMain.class);
        File folder = plugin.getDataFolder();
        Assertions.assertNotNull(folder);
        Assertions.assertTrue(folder.isDirectory());
        File file = new File(folder, "data.txt");
        Assertions.assertFalse(file.exists());
        file.createNewFile();
        Assertions.assertTrue(file.exists());
        MockBukkit.unmock();
        MockBukkit.mock();
        Assertions.assertFalse(file.exists());
    }
    
}
