package org.braekpo1nt.mctmanager.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.EventParticipantStanding;
import org.braekpo1nt.mctmanager.database.entities.EventTeamStanding;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.ParticipantData;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.database.entities.SystemState;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Getter
public class Database {
    
    // ScoreService
    private @Deprecated Dao<ParticipantData, String> participantDataDao;
    
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<EventInfo, String> eventInfoDao;
    private final @NotNull Dao<SystemState, Integer> systemStateDao;
    private final @NotNull Dao<MaintenanceTeam, String> maintenanceTeamsDao;
    private final @NotNull Dao<PracticeTeam, String> practiceTeamsDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<MaintenanceParticipantEntity, String> maintenanceParticipantsDao;
    private final @NotNull Dao<PracticeParticipantEntity, String> practiceParticipantsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ScoreEvent, Integer> scoreEventsDao;
    private final @NotNull Dao<EventTeamStanding, Integer> eventTeamStandingsDao;
    private final @NotNull Dao<EventParticipantStanding, Integer> eventParticipantStandingsDao;
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
        this.maintenanceParticipantsDao = DaoManager.createDao(connectionSource, MaintenanceParticipantEntity.class);
        this.practiceParticipantsDao = DaoManager.createDao(connectionSource, PracticeParticipantEntity.class);
        this.eventParticipantsDao = DaoManager.createDao(connectionSource, EventParticipantEntity.class);
        this.gameSessionDao = DaoManager.createDao(connectionSource, GameSession.class);
        this.scoreEventsDao = DaoManager.createDao(connectionSource, ScoreEvent.class);
        this.eventTeamStandingsDao = DaoManager.createDao(connectionSource, EventTeamStanding.class);
        this.eventParticipantStandingsDao = DaoManager.createDao(connectionSource, EventParticipantStanding.class);
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
