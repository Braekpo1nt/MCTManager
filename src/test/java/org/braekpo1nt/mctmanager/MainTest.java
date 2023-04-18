package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
        // Load your plugin
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }
    
    @Test
    public void thisTestWillFail() {
        // Perform your test
        Assertions.assertTrue(true);
    }
    
}
