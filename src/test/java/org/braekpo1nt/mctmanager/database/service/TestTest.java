import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;

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
}
