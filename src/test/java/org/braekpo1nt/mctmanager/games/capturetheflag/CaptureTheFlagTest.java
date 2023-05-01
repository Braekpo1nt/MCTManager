package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.ui.FastBoardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    
    PlayerMock createParticipant(String name, String teamName) {
        PlayerMock player = new MyPlayerMock(server, name);
        server.addPlayer(player);
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", teamName, player.getName()});
        return player;
    }
    
    void addTeam(String teamName, String teamDisplayName, String teamColor) {
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamName, teamDisplayName, teamColor});
    }
    
    @Test
    @DisplayName("With 3 teams, the third team gets notified they're on deck")
    void threePlayerOnDeckTest() {
        try {
            addTeam("red", "\"Red\"", "red");
            addTeam("blue", "\"Blue\"", "blue");
            addTeam("green", "\"Green\"", "green");
            PlayerMock player1 = createParticipant("Player1", "red");
            PlayerMock player2 = createParticipant("Player2", "blue");
            PlayerMock player3 = createParticipant("Player3", "green");
            plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"game", "start", "capture-the-flag"});
            
            Component redDisplayName = plugin.getGameManager().getFormattedTeamDisplayName("red");
            Component blueDisplayName = plugin.getGameManager().getFormattedTeamDisplayName("blue");
            Component greenDisplayName = plugin.getGameManager().getFormattedTeamDisplayName("green");
            
            player1.assertSaid(Component.empty()
                    .append(redDisplayName)
                    .append(Component.text(" is competing against "))
                    .append(blueDisplayName)
                    .append(Component.text(" this round.")));
            
            player2.assertSaid(Component.empty()
                    .append(blueDisplayName)
                    .append(Component.text(" is competing against "))
                    .append(redDisplayName)
                    .append(Component.text(" this round.")));
            
            player3.assertSaid(Component.empty()
                    .append(greenDisplayName)
                    .append(Component.text(" is not competing in this round. Their next round is "))
                    .append(Component.text(1)));
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in startGame()");
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
}
