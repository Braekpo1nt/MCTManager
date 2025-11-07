package org.braekpo1nt.mctmanager.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.braekpo1nt.mctmanager.database.entities.InstantPersonalScore;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@Getter
public class Database {
    
    private final @NotNull Dao<InstantPersonalScore, Integer> allScoreDao;
    
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
        this.allScoreDao = DaoManager.createDao(connectionSource, InstantPersonalScore.class);
    }
}
