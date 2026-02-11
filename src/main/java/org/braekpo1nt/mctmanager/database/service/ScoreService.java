package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.ParticipantData;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("UnusedReturnValue")
public class ScoreService {
    private final @NotNull String mode;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<ParticipantData, String> participantDataDao;
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<PlayerMetadata, String> playerMetadataDao;
    private final @NotNull Dao<ScoreEvent, Integer> scoreEventsDao;
    
    public ScoreService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.gameSessionDao = database.getGameSessionDao();
        this.participantDataDao = database.getParticipantDataDao();
        this.scoreEventsDao = database.getScoreEventsDao();
        this.allPlayersDao = database.getAllPlayersDao();
        this.playerMetadataDao = database.getPlayerMetadataDao();
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
    
    /**
     * @param uuid the uuid of the {@link ParticipantData} object to add the given amount to
     * @param amount the amount of tokens to add to the {@link ParticipantData#getCurrentTokens()} and
     * {@link ParticipantData#getLifetimeTokens()} values
     * @return The {@link ParticipantData} object associated with the given uuid with the updated
     * {@link ParticipantData#getCurrentTokens()} and {@link ParticipantData#getLifetimeTokens()} values
     * after the given amount has been added to them
     * @throws SQLException if there is an issue communicating with the database, or if the given uuid doesn't exist in
     * the database
     */
    public @NotNull ParticipantData addParticipantTokens(@NotNull UUID uuid, int amount) throws SQLException {
        return TransactionManager.callInTransaction(participantDataDao.getConnectionSource(), () -> {
            ParticipantData data = participantDataDao.queryForId(uuid.toString());
            if (data == null) {
                throw new SQLException(String.format("No entry found in ParticipantCurrency table for UUID %s", uuid));
            }
            data.setCurrentTokens(data.getCurrentTokens() + amount);
            data.setLifetimeTokens(data.getLifetimeTokens() + amount);
            participantDataDao.update(data);
            return data;
        });
    }
    
    /**
     * @param amounts Each participant's uuid mapped to the amount to add to their
     * {@link ParticipantData#getCurrentTokens()} and {@link ParticipantData#getLifetimeTokens()} values
     * @return A list of the {@link ParticipantData} associated with the given uuids with the updated
     * {@link ParticipantData#getCurrentTokens()} and {@link ParticipantData#getLifetimeTokens()} values
     * after the given amount has been added to them
     * @throws SQLException if there is an issue communicating with the database, or if any of the given uuids don't
     * exist in the database
     */
    public @NotNull List<ParticipantData> addParticipantCurrencies(@NotNull Map<UUID, Integer> amounts) throws SQLException {
        return TransactionManager.callInTransaction(participantDataDao.getConnectionSource(), () -> {
            List<ParticipantData> updated = new ArrayList<>(amounts.size());
            
            for (Map.Entry<UUID, Integer> entry : amounts.entrySet()) {
                UUID uuid = entry.getKey();
                int amount = entry.getValue();
                
                ParticipantData data = participantDataDao.queryForId(uuid.toString());
                if (data == null) {
                    throw new SQLException(String.format("No entry found in ParticipantCurrency table  for UUID %s", uuid));
                }
                
                data.setCurrentTokens(data.getCurrentTokens() + amount);
                data.setLifetimeTokens(data.getLifetimeTokens() + amount);
                participantDataDao.update(data);
                updated.add(data);
            }
            return updated;
        });
    }
}
