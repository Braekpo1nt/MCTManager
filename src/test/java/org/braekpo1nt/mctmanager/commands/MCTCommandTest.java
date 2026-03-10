package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.commands.mct.MCTCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.logging.Level;

import static org.mockito.Mockito.mock;

class MCTCommandTest {
    
    private ServerMock server;
    private Main plugin;
    private GameManager gameManager;
    private PluginCommand command;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
            gameManager = plugin.getGameManager();
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        
        command = plugin.getCommand("mct");
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    void testBuild() {
        new MCTCommand(plugin, gameManager, mock(BlockEffectsListener.class)).build();
    }
    
}
