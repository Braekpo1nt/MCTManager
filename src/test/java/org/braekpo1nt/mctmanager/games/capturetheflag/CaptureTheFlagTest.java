package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Level;

import static org.mockito.Mockito.*;

public class CaptureTheFlagTest {
    
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    private CommandSender sender;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(Main.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        FastBoardManager mockFastBoardManager = mock(FastBoardManager.class, RETURNS_DEFAULTS);
        plugin.setFastBoardManager(mockFastBoardManager);
        command = plugin.getCommand("mct");
        sender = server.getConsoleSender();
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    void startGame() {
        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", "red", "\"Red\"", "red"});
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", "blue", "\"Blue\"", "blue"});
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", "red", player1.getName()});
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", "blue", player2.getName()});
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
    }
    
}
