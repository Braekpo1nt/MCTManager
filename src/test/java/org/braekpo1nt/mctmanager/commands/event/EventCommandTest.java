package org.braekpo1nt.mctmanager.commands.event;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.games.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

class EventCommandTest {
    
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
        
        InputStream inputStream = EventConfigController.class.getResourceAsStream("exampleEventConfig.json");
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "eventConfig.json"));
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
        TestUtils.assertComponentPlaintextEquals("Argument blank is not recognized.", server.getConsoleSender().nextComponentMessage());
    }
    
    @Test
    @DisplayName("`/mct event start 6` starts the event with 6 games")
    void startTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start", "6"}));
        Assertions.assertTrue(plugin.getGameManager().getEventManager().eventIsActive());
    }
    
    @Test
    @DisplayName("`/mct event start 6` twice returns event already running message")
    void startTwiceTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start", "6"}));
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start", "6"}));
        Assertions.assertTrue(TestUtils.receivedMessagePlaintext(server.getConsoleSender(), "An event is already running."));
        
    }
    
    @Test
    @DisplayName("`/mct event start 4` starts the with 4 games")
    void startNumberTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start", "4"}));
        Assertions.assertTrue(plugin.getGameManager().getEventManager().eventIsActive());
        Assertions.assertEquals(4, plugin.getGameManager().getEventManager().getMaxGames());
    }
    
    @Test
    @DisplayName("`/mct event start blank` complains that blank isn't an integer")
    void startWrongArgsTest() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{"event", "start", "blank"}));
        TestUtils.assertComponentPlaintextEquals("blank is not an integer", server.getConsoleSender().nextComponentMessage());
    }
}