package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import lombok.RequiredArgsConstructor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.entities.InstantPersonalScore;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ScoreService {
    private final @NotNull Main plugin;
    private final @NotNull Dao<InstantPersonalScore, Integer> allScoreDao;
    
    /**
     * Persist the given allScore to the database
     * @param allScore the allScore to persist
     */
    public void logAllScore(@NotNull InstantPersonalScore allScore) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                allScoreDao.create(allScore);
            } catch (SQLException e) {
                Main.logger().log(Level.SEVERE, String.format("Error creating AllScore %s", allScore), e);
            }
        });
    }
}
