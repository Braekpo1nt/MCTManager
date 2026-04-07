package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.UpdateBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.ScoreEventEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("UnusedReturnValue")
public class ScoreService {
    private final @NotNull String mode;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ScoreEventEntity, Integer> scoreEventsDao;
    
    public ScoreService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.gameSessionDao = database.getGameSessionDao();
        this.scoreEventsDao = database.getScoreEventsDao();
    }
    
    /**
     * Persist the given {@link ScoreEventEntity} to the database
     * @param scoreEvent the {@link ScoreEventEntity} to persist
     * @return the given {@link ScoreEventEntity} with its assigned ID, or null if something went wrong
     */
    public @Nullable ScoreEventEntity logScoreEvent(@NotNull ScoreEventEntity scoreEvent) {
        try {
            scoreEventsDao.create(scoreEvent);
            return scoreEvent;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error persisting ScoreEvent to the database: %s", scoreEvent), e);
            return null;
        }
    }
    
    public @Nullable Collection<ScoreEventEntity> logScoreEvents(@NotNull Collection<ScoreEventEntity> scoreEvents) {
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
    
    public @NotNull List<Integer> getGameSessionId() throws SQLException {
        String sql = """
                
                """;
        try (GenericRawResults<String[]> raw =
                     gameSessionDao.queryRaw(sql)) {
            List<String[]> rawResults = raw.getResults();
            List<Integer> results = new ArrayList<>(rawResults.size());
            for (String[] row : rawResults) {
                
            }
            return results;
            
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting GameSessionId", e);
        }
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
        scoreEventsDao.deleteBuilder().delete();
        return true;
    }
    
    public void undoGameSession(int sessionId) throws SQLException {
        UpdateBuilder<GameSession, Integer> updateBuilder = gameSessionDao.updateBuilder();
        updateBuilder
                .where()
                .idEq(sessionId);
        updateBuilder
                .updateColumnValue("session_undone", true);
        updateBuilder.update();
    }
    
    public record PointTotal(UUID participantUUID, String teamId, int totalPoints) {
        
    }
    
    /**
     * @param sessionId the id of the {@link GameSession} to get the total scores for.
     * If there is no game session with the given id, an empty map is returned.
     * @return a map from participant UUIDs to their total (un-multiplied) scores
     * from the given session. If the session is undone (see {@link GameSession#isSessionUndone()})
     * then no scores will be returned.
     * @throws SQLException if there is a database error
     */
    public @NotNull Map<UUID, PointTotal> getParticipantSessionTotals(int sessionId) throws SQLException {
        
        String sql = """
                    SELECT
                        se.participant_uuid,
                        se.team_id,
                        SUM(se.points_base) AS total
                    FROM score_events se
                    JOIN game_sessions gs
                      ON gs.id = se.session_id
                    WHERE se.session_id = ?
                      AND se.participant_uuid IS NOT NULL
                      AND gs.session_undone = FALSE
                    GROUP BY se.participant_uuid
                """;
        
        Map<UUID, PointTotal> result = new HashMap<>();
        
        try (GenericRawResults<String[]> raw =
                     scoreEventsDao.queryRaw(sql, String.valueOf(sessionId))) {
            
            for (String[] row : raw.getResults()) {
                UUID participantUuid = UUID.fromString(row[0]);
                String teamId = row[1];
                int total = row[2] == null ? 0 : (int) Double.parseDouble(row[2]);
                result.put(participantUuid, new PointTotal(participantUuid, teamId, total));
            }
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting participant session totals", e);
        }
        
        return result;
    }
    
    /**
     * @param sessionId the id of the {@link GameSession} to get the total scores for.
     * If there is no game session with the given id, an empty map is returned.
     * @return a map from teamIds to their total (multiplied) scores
     * from the given session. If the session is undone (see {@link GameSession#isSessionUndone()})
     * then no scores will be returned.
     * @throws SQLException if there is a database error
     */
    public @NotNull Map<String, Integer> getTeamSessionTotals(int sessionId) throws SQLException {
        
        String sql = """
                    SELECT
                        se.team_id,
                        SUM(se.points_base * gs.multiplier) AS total
                    FROM score_events se
                    JOIN game_sessions gs
                      ON gs.id = se.session_id
                    WHERE se.session_id = ?
                      AND gs.session_undone = FALSE
                    GROUP BY se.team_id
                """;
        
        Map<String, Integer> result = new HashMap<>();
        
        try (GenericRawResults<String[]> raw =
                     scoreEventsDao.queryRaw(sql, String.valueOf(sessionId))) {
            
            for (String[] row : raw.getResults()) {
                String teamId = row[0];
                int total = row[1] == null ? 0 : (int) Double.parseDouble(row[1]);
                result.put(teamId, total);
            }
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting team session totals", e);
        }
        
        return result;
    }
}
