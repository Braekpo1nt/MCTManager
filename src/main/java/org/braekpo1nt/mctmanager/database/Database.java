package org.braekpo1nt.mctmanager.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.braekpo1nt.mctmanager.database.entities.SystemState;
import org.braekpo1nt.mctmanager.database.entities.admin.AdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Getter
public class Database {
    
    // ScoreService
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<EventInfo, String> eventInfoDao;
    private final @NotNull Dao<SystemState, Integer> systemStateDao;
    private final @NotNull Dao<MaintenanceTeam, String> maintenanceTeamsDao;
    private final @NotNull Dao<PracticeTeam, String> practiceTeamsDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<ActiveTeam, String> activeTeamsDao;
    private final @NotNull Dao<MaintenanceParticipantEntity, String> maintenanceParticipantsDao;
    private final @NotNull Dao<PracticeParticipantEntity, String> practiceParticipantsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    private final @NotNull Dao<ActiveParticipant, String> activeParticipantsDao;
    private final @NotNull Dao<AdminEntity, String> adminDao;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ScoreEventEntity, Integer> scoreEventsDao;
    private final @NotNull Dao<PlayerMetadata, String> playerMetadataDao;
    
    private Database(@NotNull ConnectionSource connectionSource) throws SQLException {
        // flyway creates the tables, no need for TableUtils:
//        TableUtils.createTableIfNotExists(connectionSource, EventInfo.class);
        
        // Create the DAOs
        this.allPlayersDao = DaoManager.createDao(connectionSource, AllPlayersEntity.class);
        this.eventInfoDao = DaoManager.createDao(connectionSource, EventInfo.class);
        this.systemStateDao = DaoManager.createDao(connectionSource, SystemState.class);
        this.maintenanceTeamsDao = DaoManager.createDao(connectionSource, MaintenanceTeam.class);
        this.practiceTeamsDao = DaoManager.createDao(connectionSource, PracticeTeam.class);
        this.eventTeamsDao = DaoManager.createDao(connectionSource, EventTeam.class);
        this.activeTeamsDao = DaoManager.createDao(connectionSource, ActiveTeam.class);
        this.maintenanceParticipantsDao = DaoManager.createDao(connectionSource, MaintenanceParticipantEntity.class);
        this.practiceParticipantsDao = DaoManager.createDao(connectionSource, PracticeParticipantEntity.class);
        this.eventParticipantsDao = DaoManager.createDao(connectionSource, EventParticipantEntity.class);
        this.activeParticipantsDao = DaoManager.createDao(connectionSource, ActiveParticipant.class);
        this.adminDao = DaoManager.createDao(connectionSource, AdminEntity.class);
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
        this(new JdbcConnectionSource(String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName), user, password));
    }
    
    public Database(String sqlitePath) throws SQLException {
        this(new JdbcConnectionSource("jdbc:sqlite:" + sqlitePath));
    }
}
