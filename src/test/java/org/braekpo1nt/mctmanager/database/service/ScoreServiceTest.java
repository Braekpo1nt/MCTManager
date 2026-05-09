package org.braekpo1nt.mctmanager.database.service;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ScoreServiceTest {
    
    Database database;
    Path dbPath;
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
                "classpath:db/migration/test",
                false
        );
        scoreService = new ScoreService(mode, database);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        database.close();
        Files.deleteIfExists(dbPath);
    }
    
    
}
