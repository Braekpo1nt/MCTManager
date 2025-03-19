package org.braekpo1nt.mctmanager.games.game.clockwork;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.*;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfigController;
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

public class ClockworkGameTest {
    
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
            ex.printStackTrace();
            System.exit(1);
        }
        gameManager = plugin.getGameManager();
        sender = server.getConsoleSender();
        InputStream inputStream = ClockworkConfigController.class.getResourceAsStream("exampleClockworkConfig.json");
        TestUtils.copyInputStreamToFile(inputStream, new File(new File(plugin.getDataFolder(), GameType.CLOCKWORK.getId()), "clockworkConfig.json"));
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    MyPlayerMock createParticipant(String name, String teamId) {
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        gameManager.joinParticipantToTeam(sender, player, name, teamId);
        Assertions.assertTrue(gameManager.isParticipant(player.getUniqueId()));
        return player;
    }
    
    void addTeam(String teamId, String teamDisplayName, String teamColor) {
        gameManager.addTeam(teamId, teamDisplayName, teamColor);
        Assertions.assertTrue(gameManager.hasTeam(teamId));
    }
    
    @Test
    void testStartGame() {
        addTeam("red", "red", "red");
        addTeam("blue", "blue", "blue");
        createParticipant("Player1", "red");
        createParticipant("Player2", "blue");
        gameManager.startGame(GameType.CLOCKWORK, "clockworkConfig.json", sender);
        gameManager.getTimerManager().skip();
        gameManager.getTimerManager().skip();
        gameManager.manuallyStopGame(false);
    }
}
