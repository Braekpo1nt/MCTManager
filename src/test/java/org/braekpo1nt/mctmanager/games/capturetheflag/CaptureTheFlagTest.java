package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.braekpo1nt.mctmanager.ui.MockFastBoardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.*;

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
//        FastBoardManager mockFastBoardManager = mock(FastBoardManager.class, RETURNS_DEFAULTS);
        FastBoardManager mockFastBoardManager = new MockFastBoardManager();
        plugin.setFastBoardManager(mockFastBoardManager);
        command = plugin.getCommand("mct");
        sender = server.getConsoleSender();
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Starting capture the flag with two players has no errors up to the class selection period")
    void twoPlayersGetToMatchStart() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            createParticipant("Player1", "red", "Red");
            createParticipant("Player2", "blue", "Blue");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through the startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through the startClassSelectionPeriod()
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in twoPlayersGetToMatchStart()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    MyPlayerMock createParticipant(String name, String teamName, String displayName) {
        MyPlayerMock player = new MyPlayerMock(server, name);
        server.addPlayer(player);
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", teamName, player.getName()});
        player.assertSaidPlaintext("You've been joined to "+displayName);
        return player;
    }
    
    void addTeam(String teamName, String teamDisplayName, String teamColor) {
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamName, String.format("\"%s\"", teamDisplayName), teamColor});
    }
    
    @Test
    @DisplayName("With 3 teams, the third team gets notified they're on deck")
    void threePlayerOnDeckTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            player1.assertSaidPlaintext("Red is competing against Blue this round.");
            player2.assertSaidPlaintext("Blue is competing against Red this round.");
            player3.assertSaidPlaintext("Green is not competing in this round. Their next round is 1");
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in threePlayerOnDeckTest()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
}
