package org.braekpo1nt.mctmanager.games.gamemanager;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.braekpo1nt.mctmanager.TestUtils.futureIsNotFailure;

class RebuildTest {
    ServerMock server;
    MockMain plugin;
    GameManager gameManager;
    Database database;
    ScoreService scoreService;
    
    String eventId;
    EventInfo eventInfo;
    
    String teamId1;
    UUID uuid1;
    String ign1;
    PlayerMock player1;
    
    String teamId2;
    UUID uuid2;
    String ign2;
    PlayerMock player2;
    
    @BeforeEach
    void setUpServerAndPlugin() throws SQLException, IOException {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            Main.logger().log(Level.SEVERE, "UnimplementedOperationException from MockBukkit", ex);
            System.exit(1);
        }
        Main.logger().setLevel(Level.SEVERE);
        gameManager = plugin.getGameManager();
        database = plugin.getDatabase();
        scoreService = gameManager.getScoreService();
        
        // TODO: make a more centralized way to create the default config files, likely within MockMain or MockGameManager
        EventConfigController eventConfigController = new EventConfigController(plugin.getDataFolder());
        InputStream eventStream = eventConfigController.getClass().getResourceAsStream("exampleEventConfig.json");
        TestUtils.copyInputStreamToFile(eventStream, new File(plugin.getDataFolder(), "eventConfig.json"));
        FootRaceConfigController footRaceConfigController = new FootRaceConfigController(plugin.getDataFolder(), GameType.FOOT_RACE.getId());
        InputStream footRaceStream = footRaceConfigController.getClass().getResourceAsStream("exampleFootRaceConfig.json");
        File footRaceDirectory = new File(plugin.getDataFolder(), GameType.FOOT_RACE.getId());
        Files.createDirectory(footRaceDirectory.toPath());
        TestUtils.copyInputStreamToFile(footRaceStream, new File(footRaceDirectory, "default.json"));
        
        teamId1 = "red";
        uuid1 = UUID.randomUUID();
        ign1 = "Player1";
        player1 = new MyPlayerMock(server, ign1, uuid1);
        
        teamId2 = "blue";
        gameManager.addTeam(teamId2, "Orange", "gold");
        uuid2 = UUID.randomUUID();
        ign2 = "Player2";
        player2 = new MyPlayerMock(server, ign2, uuid2);
        
        server.addPlayer(player1);
        server.addPlayer(player2);
        
        Date now = new Date();
        eventId = "Test";
        futureIsNotFailure(gameManager.createEvent(eventId, now, "Test Event", Component.text("Test Event"), true));
        eventInfo = gameManager.getEventService().getEventInfo(eventId);
        List<EventTeam> teams = List.of(
                EventTeam.builder()
                        .eventId(eventId)
                        .teamId(teamId1)
                        .displayName("Red")
                        .color("red")
                        .modifiedAt(now)
                        .build(),
                EventTeam.builder()
                        .eventId(eventId)
                        .teamId(teamId2)
                        .displayName("Blue")
                        .color("blue")
                        .modifiedAt(now)
                        .build()
        );
        List<EventParticipantEntity> participants = List.of(
                EventParticipantEntity.builder()
                        .eventId(eventId)
                        .participantUUID(uuid1.toString())
                        .teamId(teamId1)
                        .substitute(false)
                        .build(),
                EventParticipantEntity.builder()
                        .eventId(eventId)
                        .participantUUID(uuid2.toString())
                        .teamId(teamId2)
                        .substitute(false)
                        .build()
        );
        gameManager.getEventService().replaceEventTeamsAndParticipants(teams, participants, eventId);
        
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        server.getScheduler().performOneTick();
        MockBukkit.unmock();
    }
    
    @Test
    void testSum() {
        futureIsNotFailure(gameManager.startEvent(eventInfo, 7, 2));
        assertThat(gameManager.getOnlineParticipants()).hasSize(2);
        futureIsNotFailure(gameManager.startEvent(eventInfo, 7, 2));
        assertThat(gameManager.getMultiplier()).isEqualTo(1.5);
        futureIsNotFailure(gameManager.startGame(GameType.FOOT_RACE, "default.json"));
        gameManager.getTimerManager().skip();
        server.getScheduler().performOneTick();
        assertThat(gameManager.getActiveGameIds()).hasSize(1);
        MCTGame activeGame = gameManager.getActiveGame(new GameInstanceId(GameType.FOOT_RACE, "default.json"));
        assertThat(activeGame).isNotNull();
        assertThat(activeGame).isInstanceOf(FootRaceGame.class);
        FootRaceGame game = (FootRaceGame) activeGame;
        FootRaceParticipant participant1 = game.getParticipant(uuid1);
        assertThat(participant1).isNotNull();
        assertThat(participant1.getScore()).isEqualTo(0);
        game.awardPoints(participant1, 1, "Test points 1");
        assertThat(participant1.getScore()).isEqualTo(1);
        futureIsNotFailure(gameManager.stopGame(GameType.FOOT_RACE, "default.json"));
        Participant pPreLoad = gameManager.getOnlineParticipant(uuid1);
        assertThat(pPreLoad).isNotNull();
        assertThat(pPreLoad.getScore()).isEqualTo(1);
        Team teamPreLoad = gameManager.getTeam(teamId1);
        assertThat(teamPreLoad).isNotNull();
        assertThat(teamPreLoad.getScore()).isEqualTo(1);
        
        futureIsNotFailure(gameManager.loadGameState());
        assertThat(gameManager.getMode()).isEqualTo(Mode.EVENT);
        server.getScheduler().performOneTick();
        
        Participant pPostLoad = gameManager.getOnlineParticipant(uuid1);
        assertThat(pPostLoad).isNotNull();
        assertThat(pPostLoad.getScore()).isEqualTo(1);
        Team teamPostLoad = gameManager.getTeam(teamId1);
        assertThat(teamPostLoad).isNotNull();
        assertThat(teamPostLoad.getScore()).isEqualTo(1);
        
        futureIsNotFailure(gameManager.stopEvent());
        server.getScheduler().performOneTick();
    }
    
}
