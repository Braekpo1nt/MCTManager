package org.braekpo1nt.mctmanager.games.gamemanager;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.FailureCommandResult;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RebuildMaintenanceTest {
    ServerMock server;
    MockMain plugin;
    GameManager gameManager;
    Database database;
    ScoreService scoreService;
    GameStateService gameStateService;
    
    String teamId1;
    UUID uuid1;
    String ign1;
    PlayerMock player1;
    
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
        gameStateService = gameManager.getGameStateService();
        
        teamId1 = "red";
        uuid1 = UUID.randomUUID();
        ign1 = "Player1";
        
        gameManager.addTeam(teamId1, "Red", "red");
        gameManager.joinOfflineParticipant(uuid1, ign1, teamId1);
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        server.getScheduler().waitAsyncTasksFinished();
        MockBukkit.unmock();
    }
    
    @Test
    void test() {
        double multiplier = 1.5;
        GameSession gameSession = scoreService.createGameSession(GameSession.builder()
                .gameType(GameType.FOOT_RACE)
                .eventId(null)
                .configFile("default.json")
                .startTime(new Date())
                .mode(Mode.MAINTENANCE)
                .multiplier(multiplier)
                .build());
        assertThat(gameSession).isNotNull();
        ScoreEventEntity scoreEventEntity = scoreService.logScoreEvent(ScoreEventEntity.builder()
                .sourceType(ScoreEvent.SourceType.GAME)
                .gameSessionId(gameSession.getId())
                .eventId(null)
                .mode(Mode.MAINTENANCE)
                .participantUUID(uuid1.toString())
                .teamId(teamId1)
                .pointsBase(1)
                .description("joined game")
                .createdAt(new Date())
                .build());
        assertThat(scoreEventEntity).isNotNull();
        assertThat(gameManager.loadGameState()).isNotInstanceOf(FailureCommandResult.class);
        server.getScheduler().waitAsyncTasksFinished();
        server.getScheduler().performOneTick();
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(uuid1);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getScore())
                .describedAs("participant does not reflect multiplier")
                .isEqualTo(1);
        Team team = gameManager.getTeam(teamId1);
        assertThat(team).isNotNull();
        assertThat(team.getScore())
                .describedAs("team reflects truncated score from multiplier")
                .isEqualTo(1);
    }
}
