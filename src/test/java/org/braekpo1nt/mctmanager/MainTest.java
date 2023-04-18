package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.plugin.Plugin;
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
        server.addSimpleWorld("world");
        Plugin mv = MockBukkit.load(MultiverseCore.class);
        
        // Load your plugin
        plugin = MockBukkit.load(Main.class);
    }
    
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    public void checkSumHere() {
        int three = 3;
        int four = 4;
        int sum = three + four;
        Assertions.assertEquals(6, sum);
    }
    
}
