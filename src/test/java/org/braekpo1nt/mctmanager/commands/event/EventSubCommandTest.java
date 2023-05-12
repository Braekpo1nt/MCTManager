package org.braekpo1nt.mctmanager.commands.event;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.*;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class EventSubCommandTest {
    
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    
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
        command = plugin.getCommand("mct");
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Running /mct event with no args returns usage message")
    void noArgsShowsUsage() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event"}));
        TestUtils.assertComponentPlaintextEquals("Usage: /mct event <options>", server.getConsoleSender().nextComponentMessage());
    }
    
    @Test
    @DisplayName("Running /mct event blank gives unrecognized")
    void unrecognizedOption() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "blank"}));
        TestUtils.assertComponentPlaintextEquals("Unrecognized option blank", server.getConsoleSender().nextComponentMessage());
    }
    
    @Test
    @DisplayName("`/mct event start` starts the event")
    void startTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start"}));
        Assertions.assertTrue(plugin.getGameManager().isEventActive());
    }
    
    @Test
    @DisplayName("`/mct event start` twice returns event already running message")
    void startTwiceTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start"}));
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start"}));
        TestUtils.assertComponentPlaintextEquals("An event is already running.", server.getConsoleSender().nextComponentMessage());
    }
}