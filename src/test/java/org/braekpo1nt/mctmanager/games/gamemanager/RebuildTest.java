package org.braekpo1nt.mctmanager.games.gamemanager;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.UUID;
import java.util.logging.Level;

class RebuildTest {
    ServerMock server;
    MockMain plugin;
    GameManager gameManager;
    Database database;
    ScoreService scoreService;
    String teamId;
    UUID uuid;
    String ign;
    OfflineParticipant offlineParticipant;
    
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
        database = plugin.getDatabase();
        scoreService = gameManager.getScoreService();
        
        teamId = "purple";
        gameManager.addTeam(teamId, "Purple", "dark_purple");
        uuid = UUID.randomUUID();
        ign = "Player1";
        gameManager.joinOfflineParticipant(uuid, ign, teamId);
        offlineParticipant = gameManager.getOfflineParticipant(uuid);
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    void testSum() {
        fail
        gameManager.startGame(GameType.FOOT_RACE, "default.json");
    }
    
}
