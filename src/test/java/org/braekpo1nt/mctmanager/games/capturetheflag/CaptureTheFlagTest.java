package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
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
    void twoPlayersGetToMatchStart() {
        try {
            PlayerMock player1 = new MyPlayerMock(server, "Player1");
            server.addPlayer(player1);
            PlayerMock player2 = new MyPlayerMock(server, "Player2");
            server.addPlayer(player2);
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", "red", "\"Red\"", "red"});
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", "blue", "\"Blue\"", "blue"});
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", "red", player1.getName()});
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", "blue", player2.getName()});
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            server.getScheduler().performTicks((20 * 10) + 1); // speed through the startMatchesStartingCountDown()
            server.getScheduler().performTicks((20 * 20) + 1); // speed through the startClassSelectionPeriod()
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in startGame()");
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    PlayerMock createParticipant(String name, String teamName, String displayName, NamedTextColor teamColor) {
        PlayerMock player = new MyPlayerMock(server, name);
        server.addPlayer(player);
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", teamName, player.getName()});
        Component formattedDisplayName = formatTeamDisplayName(displayName, teamColor);
        player.assertSaid(Component.text("You've been joined to ")
                .append(formattedDisplayName));
        return player;
    }
    
    void addTeam(String teamName, String teamDisplayName, String teamColor) {
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamName, teamDisplayName, teamColor});
    }
    
    Component formatTeamDisplayName(String displayName, NamedTextColor teamColor) {
        return Component.text(displayName).color(teamColor).decorate(TextDecoration.BOLD);
    }
    
    
    @Test
    @DisplayName("With 3 teams, the third team gets notified they're on deck")
    void threePlayerOnDeckTest() {
        try {
            addTeam("red", "\"Red\"", "red");
            addTeam("blue", "\"Blue\"", "blue");
            addTeam("green", "\"Green\"", "green");
            PlayerMock player1 = createParticipant("Player1", "red", "Red", NamedTextColor.RED);
            PlayerMock player2 = createParticipant("Player2", "blue", "Blue", NamedTextColor.BLUE);
            PlayerMock player3 = createParticipant("Player3", "green", "Green", NamedTextColor.GREEN);
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            player1.assertSaid("Red is competing against Blue this round.");
            player2.assertSaid("Blue is competing against Red this round.");
            player3.assertSaid("Green is not competing in this round. Their next round is 1");
            
            Assertions.assertTrue(true);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in startGame()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }
    }
    
}
