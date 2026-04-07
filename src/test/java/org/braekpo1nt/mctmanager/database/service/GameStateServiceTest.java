package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
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
        Main.flywayMigration(
                jdbcUrl,
                user,
                password,
                "",
                "",
                mode,
                logger,
                MockMain.class.getClassLoader(),
                "classpath:db/migration/test"
        );
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
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
        AllPlayersEntity old = allPlayersDao.queryForId(fromUUID);
        assertThat(old).isNull();
        AllPlayersEntity newPlayer = allPlayersDao.queryForId(toUUID);
        assertThat(newPlayer).isNotNull();
        assertThat(newPlayer.getIgn()).isEqualTo(ign);
    }
    
    /**
     * if the to-uuid exists already and from-uuid does not,
     * running migrate doesn't change anything or give errors
     */
    @Test
    void testMigrationIdempotent() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign = "Player1";
        Dao<AllPlayersEntity, String> allPlayersDao = database.getAllPlayersDao();
        allPlayersDao.create(AllPlayersEntity.builder()
                .uuid(toUUID)
                .ign(ign)
                .firstSeenAt(new Date())
                .build());
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
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
        String teamId = "purple";
        String eventId = "test";
        Date now = new Date();
        addTeamToEveryTable(teamId, eventId, now);
        addPlayerToEveryTable(fromUUID, ign, teamId, eventId, now);
        
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
        toExistsFromDoesnt(database.getAllPlayersDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getInGameParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPlayerMetadataDao(), fromUUID, toUUID);
        // eventAdmins
        assertThat(database.getEventAdminDao().queryForEq("uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventAdminDao().queryForEq("uuid", fromUUID)).isEmpty();
        // eventParticipants
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
        // score events
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
    }
    
    @Test
    void testMigrationEveryTableIdempotent() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign = "Player1";
        String teamId = "purple";
        String eventId = "test";
        Date now = new Date();
        addTeamToEveryTable(teamId, eventId, now);
        addPlayerToEveryTable(fromUUID, ign, teamId, eventId, now);
        
        // can we run the operation twice without errors and with the same end result as once
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
        
        toExistsFromDoesnt(database.getAllPlayersDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getInGameParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPlayerMetadataDao(), fromUUID, toUUID);
        // eventAdmins
        assertThat(database.getEventAdminDao().queryForEq("uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventAdminDao().queryForEq("uuid", fromUUID)).isEmpty();
        // eventParticipants
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
        // score events
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
    }
    
    @Test
    void testMigrationEveryTable_ToExists() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign1 = "Player1";
        String ign2 = "Player2";
        String teamId = "purple";
        String eventId = "test";
        Date now = new Date();
        addTeamToEveryTable(teamId, eventId, now);
        addPlayerToEveryTable(fromUUID, ign1, teamId, eventId, now);
        addPlayerToEveryTable(toUUID, ign2, teamId, eventId, now);
        
        gameStateService.migrateUUID(fromUUID, toUUID, ign1);
        toExistsFromDoesnt(database.getAllPlayersDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeAdminDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getMaintenanceParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPracticeParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getActiveParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getInGameParticipantsDao(), fromUUID, toUUID);
        toExistsFromDoesnt(database.getPlayerMetadataDao(), fromUUID, toUUID);
        // eventAdmins
        assertThat(database.getEventAdminDao().queryForEq("uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventAdminDao().queryForEq("uuid", fromUUID)).isEmpty();
        // eventParticipants
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
        // score events
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", toUUID)).isNotEmpty();
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", fromUUID)).isEmpty();
        assertThat(database.getAllPlayersDao().queryForId(toUUID).getIgn()).describedAs("the correct ign is in place").isEqualTo(ign1);
        // active_participants
        assertThat(database.getActiveParticipantsDao().queryForId(toUUID).getIgn()).describedAs("the correct ign is in place").isEqualTo(ign1);
        // player_metadata
        assertThat(database.getPlayerMetadataDao().queryForId(toUUID).getIgn()).describedAs("the correct ign is in place").isEqualTo(ign1);
    }
    
    @Test
    void testMigrationOnNoOne() throws SQLException {
        String fromUUID = "from-uuid";
        String toUUID = "to-uuid";
        String ign = "Player1";
        
        gameStateService.migrateUUID(fromUUID, toUUID, ign);
        assertThat(database.getAllPlayersDao().queryForId(toUUID)).describedAs("creates the all_players entry for the toUUID").isNotNull();
        assertThat(database.getActiveAdminDao().queryForId(toUUID)).isNull();
        assertThat(database.getMaintenanceAdminDao().queryForId(toUUID)).isNull();
        assertThat(database.getPracticeAdminDao().queryForId(toUUID)).isNull();
        assertThat(database.getMaintenanceParticipantsDao().queryForId(toUUID)).isNull();
        assertThat(database.getPracticeParticipantsDao().queryForId(toUUID)).isNull();
        assertThat(database.getActiveParticipantsDao().queryForId(toUUID)).isNull();
        assertThat(database.getInGameParticipantsDao().queryForId(toUUID)).isNull();
        assertThat(database.getPlayerMetadataDao().queryForId(toUUID)).isNull();
        // eventAdmins
        assertThat(database.getEventAdminDao().queryForEq("uuid", toUUID)).isEmpty();
        // eventParticipants
        assertThat(database.getEventParticipantsDao().queryForEq("participant_uuid", toUUID)).isEmpty();
        // score events
        assertThat(database.getScoreEventsDao().queryForEq("participant_uuid", toUUID)).isEmpty();
    }
    
    @Test
    void testMigrateIgn() throws SQLException {
        String oldIGN = "Player1";
        String teamId = "test";
        String uuid = "uuid";
        Date now = new Date();
        gameStateService.addTeam(ActiveTeam.builder()
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .score(10)
                .build());
        database.getAllPlayersDao().create(AllPlayersEntity.builder()
                .uuid(uuid)
                .ign(oldIGN)
                .firstSeenAt(now)
                .build());
        gameStateService.addParticipant(ActiveParticipant.builder()
                .participantUUID(uuid)
                .teamId(teamId)
                .ign(oldIGN)
                .score(10)
                .build());
        database.getPlayerMetadataDao().create(PlayerMetadata.builder()
                .participantUUID(uuid)
                .ign(oldIGN)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
        
        assertThat(database.getAllPlayersDao().queryForId(uuid).getIgn()).isEqualTo(oldIGN);
        assertThat(database.getActiveParticipantsDao().queryForId(uuid).getIgn()).isEqualTo(oldIGN);
        assertThat(database.getPlayerMetadataDao().queryForId(uuid).getIgn()).isEqualTo(oldIGN);
        String newIGN = "Player2";
        boolean result = gameStateService.migrateIgn(uuid, newIGN);
        
        assertThat(result).isTrue();
        assertThat(database.getAllPlayersDao().queryForId(uuid).getIgn()).isEqualTo(newIGN);
        assertThat(database.getActiveParticipantsDao().queryForId(uuid).getIgn()).isEqualTo(newIGN);
        assertThat(database.getPlayerMetadataDao().queryForId(uuid).getIgn()).isEqualTo(newIGN);
    }
    
    @Test
    void testMigrateIgnUUIDDoesNotExist() throws SQLException {
        String uuid = "uuid";
        String newIGN = "Player2";
        boolean result = gameStateService.migrateIgn(uuid, newIGN);
        
        assertThat(result).describedAs("nothing should happen, the uuid doesn't exist").isFalse();
        assertThat(database.getAllPlayersDao().queryForId(uuid)).isNull();
        assertThat(database.getActiveParticipantsDao().queryForId(uuid)).isNull();
        assertThat(database.getPlayerMetadataDao().queryForId(uuid)).isNull();
    }
    
    @Test
    void testMigrateIgnNameExists() throws SQLException {
        String oldIGN = "Player1";
        String teamId = "test";
        String uuid1 = "uuid1";
        String uuid2 = "uuid2";
        Date now = new Date();
        gameStateService.addTeam(ActiveTeam.builder()
                .teamId(teamId)
                .displayName("purple")
                .color("dark_purple")
                .score(10)
                .build());
        database.getAllPlayersDao().create(AllPlayersEntity.builder()
                .uuid(uuid1)
                .ign(oldIGN)
                .firstSeenAt(now)
                .build());
        gameStateService.addParticipant(ActiveParticipant.builder()
                .participantUUID(uuid1)
                .teamId(teamId)
                .ign(oldIGN)
                .score(10)
                .build());
        database.getPlayerMetadataDao().create(PlayerMetadata.builder()
                .participantUUID(uuid1)
                .ign(oldIGN)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
        
        String newIGN = "Player2";
        database.getAllPlayersDao().create(AllPlayersEntity.builder()
                .uuid(uuid2)
                .ign(newIGN)
                .firstSeenAt(now)
                .build());
        gameStateService.addParticipant(ActiveParticipant.builder()
                .participantUUID(uuid2)
                .teamId(teamId)
                .ign(newIGN)
                .score(10)
                .build());
        database.getPlayerMetadataDao().create(PlayerMetadata.builder()
                .participantUUID(uuid2)
                .ign(newIGN)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
        
        boolean result = gameStateService.migrateIgn(uuid1, newIGN);
        assertThat(result).isFalse();
        assertThat(database.getAllPlayersDao().queryForId(uuid1).getIgn()).isEqualTo(oldIGN);
        assertThat(database.getActiveParticipantsDao().queryForId(uuid1).getIgn()).isEqualTo(oldIGN);
        assertThat(database.getPlayerMetadataDao().queryForId(uuid1).getIgn()).isEqualTo(oldIGN);
        assertThat(database.getAllPlayersDao().queryForId(uuid2).getIgn()).isEqualTo(newIGN);
        assertThat(database.getActiveParticipantsDao().queryForId(uuid2).getIgn()).isEqualTo(newIGN);
        assertThat(database.getPlayerMetadataDao().queryForId(uuid2).getIgn()).isEqualTo(newIGN);
    }
    
    // registerPlayer tests start
    @Test
    void testRegisterNewPlayer() throws SQLException {
        // the player doesn't exist in the database and there are no conflicts
        String uuid = "uuid";
        String ign = "Player1";
        assertThat(gameStateService.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid);
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
    }
    
    @Test
    void testRegisterNewPlayerIdempotent() throws SQLException {
        String uuid = "uuid";
        String ign = "Player1";
        assertThat(gameStateService.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        // the player exists in the database with the correct ign
        assertThat(gameStateService.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid);
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
    }
    
    @Test
    void testRegisterNewPlayerWrongIgn() throws SQLException {
        String uuid = "uuid";
        String wrongIGN = "WrongIGN";
        assertThat(gameStateService.registerPlayer(uuid, wrongIGN)).isEqualTo(RegisterConflictType.NONE);
        String correctIGN = "Player1";
        assertThat(gameStateService.registerPlayer(uuid, correctIGN)).isEqualTo(RegisterConflictType.MIGRATE_IGN);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid);
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(correctIGN);
    }
    
    @Test
    void testRegisterPlayerAutomaticMigration() throws SQLException {
        String wrongUUID = "wrong-uuid";
        String rightUUID = "right-uuid";
        String ign = "Player1";
        assertThat(gameStateService.registerPlayer(wrongUUID, ign)).isEqualTo(RegisterConflictType.NONE);
        assertThat(gameStateService.registerPlayer(rightUUID, ign)).isEqualTo(RegisterConflictType.MIGRATE_UUID);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(rightUUID);
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
    }
    
    @Test
    void testRegisterPlayerIgnExists() throws SQLException {
        String wrongUUID = "wrong-uuid";
        String rightUUID = "uuid";
        String wrongIGN = "wrongIGN";
        String rightIGN = "Player1";
        assertThat(gameStateService.registerPlayer(rightUUID, wrongIGN)).isEqualTo(RegisterConflictType.NONE);
        assertThat(gameStateService.registerPlayer(wrongUUID, rightIGN)).isEqualTo(RegisterConflictType.NONE);
        assertThat(gameStateService.registerPlayer(rightUUID, rightIGN)).isEqualTo(RegisterConflictType.MIGRATE_UUID);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(rightUUID);
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(rightIGN);
        AllPlayersEntity shouldNotExist = database.getAllPlayersDao().queryForId(wrongUUID);
        assertThat(shouldNotExist).isNull();
    }
    // registerPlayer tests end
    
    // game_sessions tests start
    @Test
    void listGameSessionIds() throws SQLException {
        Date now = new Date();
        double multiplier = 1.0;
        String eventId = "TestEvent";
        eventService.addEventInfo(EventInfo.builder()
                .eventId(eventId)
                .plainTextName("Test")
                .componentName(Component.text("Test"))
                .eventDate(now)
                .createdAt(now)
                .canonical(true)
                .modifiedAt(now)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(null)
                .gameType(GameType.FOOT_RACE)
                .configFile("nonexistent.json")
                .mode(Mode.MAINTENANCE)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(null)
                .gameType(GameType.FOOT_RACE)
                .configFile("nonexistent.json")
                .mode(Mode.MAINTENANCE)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(null)
                .gameType(GameType.FOOT_RACE)
                .configFile("default.json")
                .mode(Mode.MAINTENANCE)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(null)
                .gameType(GameType.FOOT_RACE)
                .configFile("default.json")
                .mode(Mode.PRACTICE)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        assertThat(scoreService.getGameSessionIds(
                eventId,
                GameType.FOOT_RACE,
                "default.json",
                Mode.EVENT
        ))
                .describedAs("searching for sessions that don't exist return empty list")
                .isEmpty();
        assertThat(scoreService.getGameSessionIds(
                eventId,
                GameType.FOOT_RACE,
                "default.json",
                Mode.MAINTENANCE
        ))
                .describedAs("searching for sessions that don't exist return empty list")
                .isEmpty();
        assertThat(scoreService.getGameSessionIds(
                null,
                GameType.FOOT_RACE,
                "default.json",
                Mode.MAINTENANCE
        ))
                .describedAs("when there's one match, returns 1 entry")
                .hasSize(1);
        assertThat(scoreService.getGameSessionIds(
                null,
                GameType.FOOT_RACE,
                "nonexistent.json",
                Mode.MAINTENANCE
        ))
                .describedAs("when there are two matches, return both entries")
                .hasSize(2);
    }
    
    @Test
    void listEventGameSessionIds() throws SQLException {
        Date now = new Date();
        double multiplier = 1.0;
        String eventId = "TestEvent";
        eventService.addEventInfo(EventInfo.builder()
                .eventId(eventId)
                .plainTextName("Test")
                .componentName(Component.text("Test"))
                .eventDate(now)
                .createdAt(now)
                .canonical(true)
                .modifiedAt(now)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.FOOT_RACE)
                .configFile("default.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.FOOT_RACE)
                .configFile("default.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.PARKOUR_PATHWAY)
                .configFile("default.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        assertThat(scoreService.getEventGameSessionIds(
                eventId,
                GameType.CAPTURE_THE_FLAG,
                "default.json"
        ))
                .describedAs("return no entries for no matches")
                .isEmpty();
        assertThat(scoreService.getEventGameSessionIds(
                eventId,
                GameType.FOOT_RACE,
                "default.json"
        ))
                .describedAs("when there are two matches, return both entries")
                .hasSize(2);
        assertThat(scoreService.getEventGameSessionIds(
                eventId,
                GameType.PARKOUR_PATHWAY,
                "default.json"
        ))
                .describedAs("when there is one match, return the one")
                .hasSize(1);
    }
    
    @Test
    void listPartialMatches() throws SQLException {
        Date now = new Date();
        double multiplier = 1.0;
        String eventId = "TestEvent";
        eventService.addEventInfo(EventInfo.builder()
                .eventId(eventId)
                .plainTextName("Test")
                .componentName(Component.text("Test"))
                .eventDate(now)
                .createdAt(now)
                .canonical(true)
                .modifiedAt(now)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.FOOT_RACE)
                .configFile("first.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.FOOT_RACE)
                .configFile("second.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(eventId)
                .gameType(GameType.PARKOUR_PATHWAY)
                .configFile("second.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        
        String wrongEventId = "WrongEvent";
        eventService.addEventInfo(EventInfo.builder()
                .eventId(wrongEventId)
                .plainTextName("Test")
                .componentName(Component.text("Test"))
                .eventDate(now)
                .createdAt(now)
                .canonical(true)
                .modifiedAt(now)
                .build());
        scoreService.createGameSession(GameSession.builder()
                .multiplier(multiplier)
                .eventId(wrongEventId)
                .gameType(GameType.PARKOUR_PATHWAY)
                .configFile("default.json")
                .mode(Mode.EVENT)
                .startTime(now)
                .endTime(now)
                .sessionUndone(false)
                .build());
        assertThat(scoreService.getGameSessions(
                eventId
        ))
                .describedAs("lists all entries for a given event id")
                .hasSize(3);
        assertThat(scoreService.getGameSessions(
                wrongEventId
        ))
                .describedAs("lists all entries for a given event id")
                .hasSize(1);
        assertThat(scoreService.getGameSessions(
                eventId,
                GameType.FOOT_RACE
        ))
                .describedAs("list both foot race games for the eventId")
                .hasSize(2);
        assertThat(scoreService.getGameSessions(
                eventId,
                GameType.PARKOUR_PATHWAY
        ))
                .describedAs("list the only parkour game for the eventId")
                .hasSize(1);
        assertThat(scoreService.getGameSessions(
                wrongEventId,
                GameType.FOOT_RACE
        ))
                .describedAs("list no foot race games for the wrong eventId")
                .isEmpty();
        assertThat(scoreService.getGameSessions(
                eventId,
                GameType.FOOT_RACE,
                "first.json"
        ))
                .describedAs("list the only entry that matches all three")
                .hasSize(1);
        assertThat(scoreService.getGameSessions(
                eventId,
                GameType.FOOT_RACE,
                "second.json"
        ))
                .describedAs("list the only entry that matches all three")
                .hasSize(1);
        assertThat(scoreService.getGameSessions(
                eventId,
                GameType.PARKOUR_PATHWAY,
                "second.json"
        ))
                .describedAs("list the only entry that matches all three")
                .hasSize(1);
    }
    // game_sessions tests end
    
    // HELPER METHODS 
    
    void addTeamToEveryTable(String teamId, String eventId, Date now) throws SQLException {
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
    }
    
    /**
     * Adds a player to the database such that they are present in every table
     * Note that they are an admin and a participant in all three modes, this is not a valid
     * game state
     * @param uuid the uuid of the player
     * @param ign the name of the player
     * @param teamId the teamId of the player
     * @param now an arbitrary date
     * @throws SQLException if there's a sql error
     */
    void addPlayerToEveryTable(String uuid, String ign, String teamId, String eventId, Date now) throws SQLException {
        database.getAllPlayersDao().create(AllPlayersEntity.builder()
                .uuid(uuid)
                .ign(ign)
                .firstSeenAt(now)
                .build());
        database.getPlayerMetadataDao().create(PlayerMetadata.builder()
                .participantUUID(uuid)
                .ign(ign)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
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
        
        gameStateService.addAdmin(new ActiveAdminEntity(uuid));
        gameStateService.addAdmin(new MaintenanceAdminEntity(uuid));
        gameStateService.addAdmin(new PracticeAdminEntity(uuid));
        gameStateService.addAdmin(new EventAdminEntity(0, eventId, uuid));
        gameStateService.addParticipant(MaintenanceParticipantEntity.builder()
                .participantUUID(uuid)
                .teamId(teamId)
                .build(), ign);
        gameStateService.addParticipant(PracticeParticipantEntity.builder()
                .participantUUID(uuid)
                .teamId(teamId)
                .build(), ign);
        gameStateService.addParticipant(EventParticipantEntity.builder()
                .eventId(eventId)
                .participantUUID(uuid)
                .teamId(teamId)
                .build(), ign);
        scoreService.logScoreEvents(List.of(
                ScoreEventEntity.builder()
                        .sourceType(ScoreEvent.SourceType.GAME)
                        .gameSessionId(gameSessionId)
                        .mode(Mode.MAINTENANCE)
                        .participantUUID(uuid)
                        .teamId(teamId)
                        .pointsBase(10)
                        .description("description")
                        .createdAt(now)
                        .build(),
                ScoreEventEntity.builder()
                        .sourceType(ScoreEvent.SourceType.ADMIN)
                        .mode(Mode.PRACTICE)
                        .participantUUID(uuid)
                        .teamId(teamId)
                        .pointsBase(10)
                        .description("description")
                        .createdAt(now)
                        .build()
        ));
        gameStateService.addParticipant(ActiveParticipant.builder()
                .participantUUID(uuid)
                .teamId(teamId)
                .ign(ign)
                .score(10)
                .build());
        gameStateService.addOrUpdateTeam(InGameTeam.builder()
                .teamId(teamId)
                .gameScore(10)
                .gameSessionId(gameSessionId)
                .build());
        gameStateService.addOrUpdateParticipant(InGameParticipant.builder()
                .gameScore(10)
                .gameSessionId(gameSessionId)
                .participantUUID(uuid)
                .build());
    }
    
    <T> void toExistsFromDoesnt(Dao<?, T> dao, T from, T to) throws SQLException {
        assertThat(dao.idExists(to)).isTrue();
        assertThat(dao.idExists(from)).isFalse();
    }
    
}
