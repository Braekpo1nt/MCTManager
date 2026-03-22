package org.braekpo1nt.mctmanager.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.EventInfoDto;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.braekpo1nt.mctmanager.database.entities.SystemState;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.MaintenanceAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.PracticeAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.InGameParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.InGameTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@Getter
public class Database {
    
    // ScoreService
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<EventInfoDto, String> eventInfoDao;
    private final @NotNull Dao<SystemState, Integer> systemStateDao;
    private final @NotNull Dao<MaintenanceTeam, String> maintenanceTeamsDao;
    private final @NotNull Dao<PracticeTeam, String> practiceTeamsDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<ActiveTeam, String> activeTeamsDao;
    private final @NotNull Dao<InGameTeam, String> inGameTeamsDao;
    private final @NotNull Dao<MaintenanceParticipantEntity, String> maintenanceParticipantsDao;
    private final @NotNull Dao<PracticeParticipantEntity, String> practiceParticipantsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    private final @NotNull Dao<ActiveParticipant, String> activeParticipantsDao;
    private final @NotNull Dao<InGameParticipant, String> inGameParticipantsDao;
    private final @NotNull Dao<ActiveAdminEntity, String> activeAdminDao;
    private final @NotNull Dao<MaintenanceAdminEntity, String> maintenanceAdminDao;
    private final @NotNull Dao<PracticeAdminEntity, String> practiceAdminDao;
    private final @NotNull Dao<EventAdminEntity, Integer> eventAdminDao;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ScoreEventEntity, Integer> scoreEventsDao;
    private final @NotNull Dao<PlayerMetadata, String> playerMetadataDao;
    private final @NotNull ConnectionSource connectionSource;
    
    private Database(@NotNull ConnectionSource connectionSource) throws SQLException {
        // flyway creates the tables, no need for TableUtils:
//        TableUtils.createTableIfNotExists(connectionSource, EventInfo.class);
        this.connectionSource = connectionSource;
        
        // Create the DAOs
        this.allPlayersDao = DaoManager.createDao(connectionSource, AllPlayersEntity.class);
        this.eventInfoDao = DaoManager.createDao(connectionSource, EventInfoDto.class);
        this.systemStateDao = DaoManager.createDao(connectionSource, SystemState.class);
        this.maintenanceTeamsDao = DaoManager.createDao(connectionSource, MaintenanceTeam.class);
        this.practiceTeamsDao = DaoManager.createDao(connectionSource, PracticeTeam.class);
        this.eventTeamsDao = DaoManager.createDao(connectionSource, EventTeam.class);
        this.activeTeamsDao = DaoManager.createDao(connectionSource, ActiveTeam.class);
        this.inGameTeamsDao = DaoManager.createDao(connectionSource, InGameTeam.class);
        this.maintenanceParticipantsDao = DaoManager.createDao(connectionSource, MaintenanceParticipantEntity.class);
        this.practiceParticipantsDao = DaoManager.createDao(connectionSource, PracticeParticipantEntity.class);
        this.eventParticipantsDao = DaoManager.createDao(connectionSource, EventParticipantEntity.class);
        this.activeParticipantsDao = DaoManager.createDao(connectionSource, ActiveParticipant.class);
        this.inGameParticipantsDao = DaoManager.createDao(connectionSource, InGameParticipant.class);
        this.activeAdminDao = DaoManager.createDao(connectionSource, ActiveAdminEntity.class);
        this.maintenanceAdminDao = DaoManager.createDao(connectionSource, MaintenanceAdminEntity.class);
        this.practiceAdminDao = DaoManager.createDao(connectionSource, PracticeAdminEntity.class);
        this.eventAdminDao = DaoManager.createDao(connectionSource, EventAdminEntity.class);
        this.gameSessionDao = DaoManager.createDao(connectionSource, GameSession.class);
        this.scoreEventsDao = DaoManager.createDao(connectionSource, ScoreEventEntity.class);
        this.playerMetadataDao = DaoManager.createDao(connectionSource, PlayerMetadata.class);
    }
    
    public Database(
            String host,
            String port,
            String user,
            String password,
            String databaseName) throws SQLException {
        this(getConnectionSource(host, port, user, password, databaseName));
    }
    
    /**
     * A {@link JdbcPooledConnectionSource} is better for a 24/7 plugin because it recreates connections
     * when lost automatically. Handles DB server timeouts, handles router resets, internet disconnections, etc.
     * @param host the host address (e.g. localhost)
     * @param port the port (e.g. 8330680)
     * @param user the username (e.g. "admin")
     * @param password the password (e.g. "password")
     * @param databaseName the name of the database (e.g. "challenger_trials")
     * @return a ConnectionSource usable by ORMLite
     * @throws SQLException if there is an issue connecting to the database
     */
    private static @NotNull JdbcPooledConnectionSource getConnectionSource(String host, String port, String user, String password, String databaseName) throws SQLException {
        JdbcPooledConnectionSource jdbcPooledConnectionSource = new JdbcPooledConnectionSource(String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName), user, password);
        jdbcPooledConnectionSource.setMaxConnectionsFree(5);
        jdbcPooledConnectionSource.setCheckConnectionsEveryMillis(30_000);
        return jdbcPooledConnectionSource;
    }
    
    public Database(String sqlitePath) throws SQLException {
        this(new JdbcConnectionSource("jdbc:sqlite:" + sqlitePath));
    }
    
    public static Database createInMemorySQLite() throws SQLException {
        return new Database(new JdbcConnectionSource("jdbc:sqlite::memory:"));
    }
    
    public void close() throws Exception {
        connectionSource.close();
    }
    
    /**
     * @param original the original throwable
     * @return true if the given throwable has a parent cause (no matter how far up) which
     * indicates a foreign key violation exception.
     */
    public static boolean containsForeignKeyViolation(final Throwable original) {
        Throwable throwable = original;
        while (throwable != null) {
            
            if (throwable instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            
            if (throwable instanceof SQLException sqlException) {
                // MariaDB / MySQL FK violation error code
                if (sqlException.getErrorCode() == 1451) {
                    return true;
                }
            }
            
            throwable = throwable.getCause();
        }
        return false;
    }
}
