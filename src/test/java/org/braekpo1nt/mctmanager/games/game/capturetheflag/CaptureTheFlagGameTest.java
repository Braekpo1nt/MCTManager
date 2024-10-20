package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.*;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.CommandSender;
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
        gameManager.joinPlayerToTeam(sender, player, teamId);
        Assertions.assertTrue(gameManager.isParticipant(player.getUniqueId()));
        return player;
    }
    
    void addTeam(String teamId, String teamDisplayName, String teamColor) {
        gameManager.addTeam(teamId, teamDisplayName, teamColor);
        Assertions.assertTrue(gameManager.hasTeam(teamId));
    }
    
    @Test
    void testStartGame() {
        addTeam("yellow", "The Councel", "yellow");
        addTeam("aqua", "Aquaholics", "aqua");
        addTeam("red", "Red Rangers", "red");
        addTeam("purple", "Purple Paladins", "dark_purple");
        addTeam("blue", "Blue Bedtimers", "blue");
        addTeam("lime", "Lime Cacti", "green");
        addTeam("green", "Green Spartans", "dark_green");
        addTeam("orange", "Orange Oni's", "gold");
        addTeam("pink", "Pink Penguins", "magenta");
        addTeam("cyan", "Just the Builders", "cyan");
        createParticipant("Player1", "yellow");
        createParticipant("Player2", "aqua");
        createParticipant("Player3", "red");
        createParticipant("Player4", "purple");
        createParticipant("Player5", "blue");
        createParticipant("Player6", "lime");
        createParticipant("Player7", "green");
        createParticipant("Player8", "orange");
        createParticipant("Player9", "pink");
        createParticipant("Player10", "cyan");
        gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
//        gameManager.getTimerManager().skip();
//        gameManager.getTimerManager().skip();
        gameManager.manuallyStopGame(false);
    }
}
