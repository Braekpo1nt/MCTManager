package org.braekpo1nt.mctmanager.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.braekpo1nt.mctmanager.database.entities.FinalPersonalScore;
import org.braekpo1nt.mctmanager.database.entities.FinalTeamScore;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.InstantPersonalScore;
import org.braekpo1nt.mctmanager.database.entities.InstantTeamScore;
import org.braekpo1nt.mctmanager.database.entities.ParticipantCurrency;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Getter
public class Database {
    
    private final @NotNull Dao<InstantPersonalScore, Integer> instantPersonalScoreDao;
    private final @NotNull Dao<InstantTeamScore, Integer> instantTeamScoreDao;
    private final @NotNull Dao<FinalPersonalScore, Integer> finalPersonalScoreDao;
    private final @NotNull Dao<FinalTeamScore, Integer> finalTeamScoreDao;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ParticipantCurrency, String> participantCurrencyDao;
    
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
    
    private Database(@NotNull ConnectionSource connectionSource) throws SQLException {
        // flyway creates the tables, no need for TableUtils
        
        // Create the DAOs
        this.instantPersonalScoreDao = DaoManager.createDao(connectionSource, InstantPersonalScore.class);
        this.instantTeamScoreDao = DaoManager.createDao(connectionSource, InstantTeamScore.class);
        this.gameSessionDao = DaoManager.createDao(connectionSource, GameSession.class);
        this.finalPersonalScoreDao = DaoManager.createDao(connectionSource, FinalPersonalScore.class);
        this.finalTeamScoreDao = DaoManager.createDao(connectionSource, FinalTeamScore.class);
        this.participantCurrencyDao = DaoManager.createDao(connectionSource, ParticipantCurrency.class);
    }
}
