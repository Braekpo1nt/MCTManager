package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateServiceTest {
    
    Database database;
    Path dbPath;
    GameStateService gameStateService;
    
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
    }
    
    @AfterEach
    void tearDown() throws Exception {
        database.close();
        Files.deleteIfExists(dbPath);
    }
    
    @Test
    void testMigration() throws SQLException {
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
    
}
