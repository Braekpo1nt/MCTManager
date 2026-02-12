package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

@SuppressWarnings("UnusedReturnValue")
public class ScoreService {
    private final @NotNull String mode;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<PlayerMetadata, String> playerMetadataDao;
    private final @NotNull Dao<ScoreEvent, Integer> scoreEventsDao;
    
    private final @NotNull Dao<ActiveTeam, Integer> activeTeamsDao;
    private final @NotNull Dao<ActiveParticipant, Integer> activeParticipantsDao;
    
    public ScoreService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.gameSessionDao = database.getGameSessionDao();
        this.scoreEventsDao = database.getScoreEventsDao();
        this.allPlayersDao = database.getAllPlayersDao();
        this.playerMetadataDao = database.getPlayerMetadataDao();
        
        this.activeTeamsDao = database.getActiveTeamsDao();
        this.activeParticipantsDao = database.getActiveParticipantsDao();
    }
    
    public void rebuildPracticeMode() throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            
            // rebuild teams
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_teams (team_id, display_name, color, score)
                        SELECT
                            mt.team_id,
                            mt.display_name,
                            mt.color,
                            COALESCE(SUM(se.points_base * se.multiplier), 0)
                        FROM practice_teams mt
                        LEFT JOIN score_events se
                          ON se.team_id = mt.team_id
                         AND se.mode = 'practice'
                        GROUP BY mt.team_id, mt.display_name, mt.color
                    """);
            
            // rebuild participants
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                        SELECT
                            mp.participant_uuid,
                            mp.team_id,
                            ap.ign,
                            COALESCE(SUM(se.points_base), 0)
                        FROM practice_participants mp
                        JOIN all_players ap
                          ON ap.uuid = mp.participant_uuid
                        LEFT JOIN score_events se
                          ON se.participant_uuid = mp.participant_uuid
                         AND se.mode = 'practice'
                        GROUP BY mp.participant_uuid, mp.team_id, ap.ign
                    """);
            
            return null;
        });
    }
    
    /**
     * Persist the given {@link ScoreEvent} to the database
     * @param scoreEvent the {@link ScoreEvent} to persist
     * @return the given {@link ScoreEvent} with its assigned ID, or null if something went wrong
     */
    public @Nullable ScoreEvent logScoreEvent(@NotNull ScoreEvent scoreEvent) {
        try {
            scoreEventsDao.create(scoreEvent);
            return scoreEvent;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error persisting ScoreEvent to the database: %s", scoreEvent), e);
            return null;
        }
    }
    
    public @Nullable Collection<ScoreEvent> logScoreEvents(@NotNull Collection<ScoreEvent> scoreEvents) {
        try {
            scoreEventsDao.create(scoreEvents);
            return scoreEvents;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error persisting %s ScoreEvents to the database", scoreEvents.size()), e);
            return null;
        }
    }
    
    public @Nullable GameSession createGameSession(@NotNull GameSession gameSession) {
        try {
            gameSessionDao.create(gameSession);
            return gameSession;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error creating GameSession %s", gameSession), e);
            return null;
        }
    }
    
    public @NotNull GameSession setGameSessionEndDate(int id, @NotNull Date endTime) throws SQLException {
        GameSession gameSession = gameSessionDao.queryForId(id);
        gameSession.setEndTime(endTime);
        gameSessionDao.update(gameSession);
        return gameSession;
    }
    
    /**
     * Deletes all entries in the ScoreService databases
     * @throws SQLException if there is an error clearing the databases
     */
    public boolean clearDatabase() throws SQLException {
        if (!mode.equals("test")) {
            return false;
        }
        gameSessionDao.deleteBuilder().delete();
        allPlayersDao.deleteBuilder().delete();
        playerMetadataDao.deleteBuilder().delete();
        scoreEventsDao.deleteBuilder().delete();
        return true;
    }
    
    public void registerParticipantIfNotRegistered(@NotNull AllPlayersEntity allPlayersEntity, @NotNull PlayerMetadata playerMetadata) {
        try {
            TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
                allPlayersDao.createOrUpdate(allPlayersEntity);
                playerMetadataDao.createIfNotExists(playerMetadata);
                return null;
            });
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error creating AllPlayersEntity %s and PlayerMetadata %s", allPlayersEntity, playerMetadata), e);
        }
    }
}
