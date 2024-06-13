package org.braekpo1nt.mctmanager.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

class MCTCommandTest {
    
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    
        MockGameStateStorageUtil mockGameStateStorageUtil = new MockGameStateStorageUtil(plugin);
        plugin.getGameManager().setGameStateStorageUtil(mockGameStateStorageUtil);
        
        command = plugin.getCommand("mct");
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
//    @Test
//    @DisplayName("Running /mct with no args returns usage message")
//    void noArgsShowsUsage() {
//        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{}));
//        Component message = plugin.getMctCommand().getUsage().toComponent();
//        Assertions.assertNotNull(message);
//        server.getConsoleSender().assertSaid(message);
//    }
    
}
