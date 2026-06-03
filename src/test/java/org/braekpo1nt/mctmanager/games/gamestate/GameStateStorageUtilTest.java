package org.braekpo1nt.mctmanager.games.gamestate;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.database.service.RegisterConflictType;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class GameStateStorageUtilTest {
    
    Database database;
    Path dbPath;
    GameStateService gameStateService;
    GameStateStorageUtil gameStateStorageUtil;
    String teamId;
    
    @BeforeEach
    void setup() throws IOException, SQLException {
        dbPath = Files.createTempFile("test-db-", "mctmanager.db");
        String sqlitePath = dbPath.toAbsolutePath().toString();
        database = new Database(sqlitePath, Runnable::run);
        
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
                "classpath:db/migration/test",
                false
        );
        gameStateService = new GameStateService(mode, database);
        gameStateStorageUtil = new MockGameStateStorageUtil(logger, gameStateService);
        teamId = "purple";
        gameStateStorageUtil.addTeam(teamId, "Purple", "dark_purple");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        database.close();
        Files.deleteIfExists(dbPath);
    }
    
    @Test
    void addPlayer() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String ign = "Player1";
        
        assertThat(gameStateStorageUtil.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        gameStateStorageUtil.joinPlayer(uuid, ign, teamId);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getName()).isEqualTo(ign);
    }
    
    @Test
    void addPlayerIdempotence() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String ign = "Player1";
        
        assertThat(gameStateStorageUtil.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        gameStateStorageUtil.joinPlayer(uuid, ign, teamId);
        assertThat(gameStateStorageUtil.registerPlayer(uuid, ign)).isEqualTo(RegisterConflictType.NONE);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getName()).isEqualTo(ign);
    }
    
    @Test
    void addThenRemovePlayer() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String ign = "Player1";
        
        gameStateStorageUtil.joinPlayer(uuid, ign, teamId);
        gameStateStorageUtil.leavePlayer(uuid);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
        assertThat(offlineParticipant).isNull();
    }
    
    @Test
    void joinLeaveJoin() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String ign = "Player1";
        
        gameStateStorageUtil.joinPlayer(uuid, ign, teamId);
        gameStateStorageUtil.leavePlayer(uuid);
        gameStateStorageUtil.joinPlayer(uuid, ign, teamId);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getName()).isEqualTo(ign);
    }
    
    @Test
    void addPlayerWrongIgn() throws SQLException {
        UUID uuid = UUID.randomUUID();
        String wrongIGN = "WrongIGN";
        String correctIGN = "Player1";
        
        // offline player added to game state, wrong ign
        gameStateStorageUtil.joinPlayer(uuid, wrongIGN, teamId);
        
        // player with that uuid and a different ign logs in
        assertThat(gameStateStorageUtil.registerPlayer(uuid, correctIGN)).isEqualTo(RegisterConflictType.MIGRATE_IGN);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(correctIGN);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(uuid);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getName())
                .describedAs("The gameStateStorageUtil should have the correct IGN for the given uuid")
                .isEqualTo(correctIGN);
    }
    
    @Test
    void addPlayerWrongUUID() throws SQLException {
        UUID wrongUUID = UUID.randomUUID();
        UUID rightUUID = UUID.randomUUID();
        String ign = "Player1";
        
        // offline player added to game state, wrong uuid
        gameStateStorageUtil.joinPlayer(wrongUUID, ign, teamId);
        
        // player with that username but a different uuid logs in
        assertThat(gameStateStorageUtil.registerPlayer(rightUUID, ign)).isEqualTo(RegisterConflictType.MIGRATE_UUID);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(rightUUID.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(ign);
        OfflineParticipant offlineParticipant = gameStateStorageUtil.getOfflineParticipant(rightUUID);
        assertThat(offlineParticipant).isNotNull();
        assertThat(offlineParticipant.getName())
                .describedAs("The gameStateStorageUtil should have the correct uuid for the given ign")
                .isEqualTo(ign);
    }
    
    @Test
    void bothAreWrongAndRightLogsIn() throws SQLException {
        UUID wrongUUID = UUID.randomUUID();
        UUID rightUUID = UUID.randomUUID();
        String wrongIGN = "wrongIGN";
        String rightIGN = "Player1";
        
        // offline player added to game state, wrong ign
        gameStateStorageUtil.joinPlayer(rightUUID, wrongIGN, teamId);
        // offline player added to the game state, wrong uuid right ign
        gameStateStorageUtil.joinPlayer(wrongUUID, rightIGN, teamId);
        
        // player with the right uuid and ign combo logs in
        assertThat(gameStateStorageUtil.registerPlayer(rightUUID, rightIGN)).isEqualTo(RegisterConflictType.MIGRATE_UUID);
        
        AllPlayersEntity actual = database.getAllPlayersDao().queryForId(rightUUID.toString());
        assertThat(actual).isNotNull();
        assertThat(actual.getIgn()).isEqualTo(rightIGN);
        OfflineParticipant shouldExist = gameStateStorageUtil.getOfflineParticipant(rightUUID);
        assertThat(shouldExist).isNotNull();
        OfflineParticipant shouldNotExist = gameStateStorageUtil.getOfflineParticipant(wrongUUID);
        assertThat(shouldNotExist).isNull();
        assertThat(shouldExist.getName())
                .describedAs("The gameStateStorageUtil should have the correct uuid for the given ign")
                .isEqualTo(rightIGN);
    }
    
    
}
