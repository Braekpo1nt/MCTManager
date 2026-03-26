package org.braekpo1nt.mctmanager.database.service;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;

import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TestTest {
    
    static MariaDBContainer<?> mariaDB =
            new MariaDBContainer<>("mariadb:11.4")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
    
    Database database;
    GameStateService service;
    
    @BeforeAll
    static void startContainer() {
        mariaDB.start();
    }
    
    @AfterAll
    static void stopContainer() {
        mariaDB.stop();
    }
    
    @BeforeEach
    void setup() throws Exception {
        String jdbcUrl = mariaDB.getJdbcUrl();
        
        Main.flywayMigration(
                jdbcUrl,
                mariaDB.getUsername(),
                mariaDB.getPassword(),
                "",
                "",
                "prod",
                Logger.getLogger("test"),
                Main.class.getClassLoader()
        );
        
        database = new Database(jdbcUrl);
        service = new GameStateService("prod", database);
    }
    
    @Test
    void test() {
        assertThat(database).isNotNull();
    }
}
