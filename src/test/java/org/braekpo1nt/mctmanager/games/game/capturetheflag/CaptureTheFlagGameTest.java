package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.*;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.ClassSelectionState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.PreRoundState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.RoundActiveState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.RoundOverState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;

public class CaptureTheFlagGameTest {
    
    private ServerMock server;
    private Main plugin;
    private GameManager gameManager;
    private CommandSender sender;
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            Main.logger().log(Level.SEVERE, "UnimplementedOperationException from MockBukkit", ex);
            System.exit(1);
        }
        gameManager = plugin.getGameManager();
        sender = server.getConsoleSender();
        InputStream inputStream = CaptureTheFlagConfigController.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "captureTheFlagConfig.json"));
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    MyPlayerMock createParticipant(String name, String teamId) {
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        gameManager.joinParticipantToTeam(sender, player, teamId);
        Assertions.assertTrue(gameManager.isParticipant(player.getUniqueId()));
        return player;
    }
    
    void removeParticipant(OfflinePlayer player, @NotNull String playerName) {
        gameManager.leaveParticipant(sender, player, playerName);
    }
    
    void addTeam(String teamId, String teamDisplayName, String teamColor) {
        gameManager.addTeam(teamId, teamDisplayName, teamColor);
        Assertions.assertTrue(gameManager.hasTeam(teamId));
    }
    
    @Test
    void playerLeavingStillEndsGame() {
        addTeam("aqua", "Aquaholics", "aqua");
        addTeam("red", "Red Rangers", "red");
        addTeam("yellow", "The Councel", "yellow");
        createParticipant("Player1", "aqua");
        createParticipant("Player2", "red");
        MyPlayerMock player3 = createParticipant("Player3", "yellow");
        gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
        CaptureTheFlagGame game = (CaptureTheFlagGame) gameManager.getActiveGame();
        
        gameManager.getTimerManager().skip();
        Assertions.assertInstanceOf(PreRoundState.class, game.getState(), "we should be in the PreRoundState");
        removeParticipant(player3, "Player3");
        
        gameManager.getTimerManager().skip();
        Assertions.assertInstanceOf(RoundActiveState.class, game.getState());
        CaptureTheFlagMatch match = ((RoundActiveState) game.getState()).getMatch("red");
        Assertions.assertNotNull(match, "there should be an active match");
        Assertions.assertInstanceOf(ClassSelectionState.class, match.getState());
        
        gameManager.getTimerManager().skip(); // should realize that Player3 isn't online, and end the match, which ends the round
        Assertions.assertInstanceOf(RoundOverState.class, game.getState());
        
        gameManager.manuallyStopGame(false);
    }
}
