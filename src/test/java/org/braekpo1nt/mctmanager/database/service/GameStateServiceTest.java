package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.MaintenanceAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.PracticeAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.InGameParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.InGameTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateServiceTest {
    
    Database database;
    Path dbPath;
    GameStateService gameStateService;
    EventService eventService;
    ScoreService scoreService;
    
    @BeforeEach
    void setup() throws SQLException, IOException {
        dbPath = Files.createTempFile("test-db-", "mctmanager.db");
        String sqlitePath = dbPath.toAbsolutePath().toString();
        database = new Database(sqlitePath);
        
        Logger logger = Logger.getLogger("test");
        logger.setLevel(Level.OFF);
        String user = "root";
        String password = "";
        String jdbcUrl = "jdbc:sqlite:" + sqlitePath;
        String mode = "prod";
        Main.flywayMigration(jdbcUrl, user, password, "", "", mode, logger, Main.class.getClassLoader());
        gameStateService = new GameStateService(mode, database);
        eventService = new EventService(mode, database);
        scoreService = new ScoreService(mode, database);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        database.close();
        Files.deleteIfExists(dbPath);
    }
    
    @Test
    void testMigrationNoOtherTables() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign = "Player1";
        Dao<AllPlayersEntity, String> allPlayersDao = database.getAllPlayersDao();
        allPlayersDao.create(AllPlayersEntity.builder()
                .uuid(fromUUID)
                .ign(ign)
                .firstSeenAt(new Date())
                .build());
        gameStateService.migrateFromUUIDToUUID(fromUUID, toUUID, ign);
        AllPlayersEntity old = allPlayersDao.queryForId(fromUUID);
        assertThat(old).isNull();
        AllPlayersEntity newPlayer = allPlayersDao.queryForId(toUUID);
        assertThat(newPlayer).isNotNull();
        assertThat(newPlayer.getIgn()).isEqualTo(ign);
    }
    
    @Test
    void testMigrationEveryTable() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign = "Player1";
        Date now = new Date();
        database.getAllPlayersDao().create(AllPlayersEntity.builder()
                .uuid(fromUUID)
                .ign(ign)
                .firstSeenAt(now)
                .build());
        database.getPlayerMetadataDao().create(PlayerMetadata.builder()
                .participantUUID(fromUUID)
                .ign(ign)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
        String eventId = "test";
        eventService.addEventInfo(EventInfo.builder()
                .eventId(eventId)
                .plainTextName("Test")
                .componentName(Component.text("Test"))
                .eventDate(now)
                .createdAt(now)
                .canonical(true)
                .modifiedAt(now)
                .build());
        int gameSessionId = Objects.requireNonNull(scoreService.createGameSession(GameSession.builder()
                .multiplier(1.0)
                .gameType(GameType.FOOT_RACE)
                .startTime(now)
                .configFile("nonexistent.json")
                .eventId(null)
                .mode(Mode.MAINTENANCE)
                .sessionUndone(false)
                .build()), "could not create game session").getId();
        String teamId = "purple";
        gameStateService.addTeam(MaintenanceTeam.builder()
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .modifiedAt(now)
                .build());
        gameStateService.addTeam(PracticeTeam.builder()
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .modifiedAt(now)
                .build());
        gameStateService.addTeam(EventTeam.builder()
                .eventId(eventId)
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .modifiedAt(now)
                .build());
        gameStateService.addTeam(ActiveTeam.builder()
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .score(10)
                .build());
        gameStateService.addAdmin(new ActiveAdminEntity(fromUUID));
        gameStateService.addAdmin(new MaintenanceAdminEntity(fromUUID));
        gameStateService.addAdmin(new PracticeAdminEntity(fromUUID));
        gameStateService.addAdmin(new EventAdminEntity(0, eventId, fromUUID));
        gameStateService.addParticipant(MaintenanceParticipantEntity.builder()
                .participantUUID(fromUUID)
                .teamId(teamId)
                .build(), ign);
        gameStateService.addParticipant(PracticeParticipantEntity.builder()
                .participantUUID(fromUUID)
                .teamId(teamId)
                .build(), ign);
        gameStateService.addParticipant(EventParticipantEntity.builder()
                .eventId(eventId)
                .participantUUID(fromUUID)
                .teamId(teamId)
                .build(), ign);
        scoreService.logScoreEvents(List.of(
                ScoreEventEntity.builder()
                        .sourceType(ScoreEvent.SourceType.GAME)
                        .gameSessionId(gameSessionId)
                        .mode(Mode.MAINTENANCE)
                        .participantUUID(fromUUID)
                        .teamId(teamId)
                        .pointsBase(10)
                        .description("description")
                        .createdAt(now)
                        .build(),
                ScoreEventEntity.builder()
                        .sourceType(ScoreEvent.SourceType.ADMIN)
                        .mode(Mode.PRACTICE)
                        .participantUUID(fromUUID)
                        .teamId(teamId)
                        .pointsBase(10)
                        .description("description")
                        .createdAt(now)
                        .build()
        ));
        gameStateService.addParticipant(ActiveParticipant.builder()
                .participantUUID(fromUUID)
                .teamId(teamId)
                .ign(ign)
                .score(10)
                .build());
        gameStateService.createOrUpdate(InGameTeam.builder()
                .teamId(teamId)
                .gameScore(10)
                .gameSessionId(gameSessionId)
                .build());
        gameStateService.createOrUpdate(InGameParticipant.builder()
                .gameScore(10)
                .gameSessionId(gameSessionId)
                .participantUUID(fromUUID)
                .build());
        gameStateService.migrateFromUUIDToUUID(fromUUID, toUUID, ign);
    }
    
}
