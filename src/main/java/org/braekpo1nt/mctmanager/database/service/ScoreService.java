package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.FinalPersonalScore;
import org.braekpo1nt.mctmanager.database.entities.FinalTeamScore;
import org.braekpo1nt.mctmanager.database.entities.GameSession;
import org.braekpo1nt.mctmanager.database.entities.InstantPersonalScore;
import org.braekpo1nt.mctmanager.database.entities.InstantTeamScore;
import org.braekpo1nt.mctmanager.database.entities.ParticipantCurrency;
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
    private final @NotNull Dao<InstantPersonalScore, Integer> instantPersonalScoreDao;
    private final @NotNull Dao<InstantTeamScore, Integer> instantTeamScoreDao;
    private final @NotNull Dao<GameSession, Integer> gameSessionDao;
    private final @NotNull Dao<FinalPersonalScore, Integer> finalPersonalScoreDao;
    private final @NotNull Dao<FinalTeamScore, Integer> finalTeamScoreDao;
    private final @NotNull Dao<ParticipantCurrency, String> participantCurrencyDao;
    
    public ScoreService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.instantPersonalScoreDao = database.getInstantPersonalScoreDao();
        this.instantTeamScoreDao = database.getInstantTeamScoreDao();
        this.gameSessionDao = database.getGameSessionDao();
        this.finalPersonalScoreDao = database.getFinalPersonalScoreDao();
        this.finalTeamScoreDao = database.getFinalTeamScoreDao();
        this.participantCurrencyDao = database.getParticipantCurrencyDao();
    }
    
    
    /**
     * Persist the given instantPersonalScore to the database
     * @param instantPersonalScore the instantPersonalScore to persist
     * @return the given InstantPersonalScore object with its assigned ID
     */
    public @Nullable InstantPersonalScore logInstantScore(@NotNull InstantPersonalScore instantPersonalScore) {
        try {
            instantPersonalScoreDao.create(instantPersonalScore);
            return instantPersonalScore;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error creating AllScore %s", instantPersonalScore), e);
            return null;
        }
    }
    
    /**
     * Persist the given InstantPersonalScores to the database
     * @param instantPersonalScores the InstantPersonalScores to persist
     * @return the given InstantPersonalScore objects with their assigned IDs
     */
    public @Nullable Collection<InstantPersonalScore> logInstantPersonalScores(@NotNull Collection<InstantPersonalScore> instantPersonalScores) {
        try {
            instantPersonalScoreDao.create(instantPersonalScores);
            return instantPersonalScores;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "Error logging InstantPersonalScores", e);
            return null;
        }
    }
    
    public @Nullable InstantTeamScore logInstantScore(@NotNull InstantTeamScore instantTeamScore) {
        try {
            instantTeamScoreDao.create(instantTeamScore);
            return instantTeamScore;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "Error logging InstantPersonalScores", e);
            return null;
        }
    }
    
    public @Nullable Collection<InstantTeamScore> logInstantTeamScores(@NotNull Collection<InstantTeamScore> instantTeamScores) {
        try {
            instantTeamScoreDao.create(instantTeamScores);
            return instantTeamScores;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "Error logging InstantPersonalScores", e);
            return null;
        }
    }
    
    public @Nullable Collection<FinalPersonalScore> logFinalPersonalScores(@NotNull Collection<FinalPersonalScore> finalPersonalScores) throws SQLException {
        finalPersonalScoreDao.create(finalPersonalScores);
        return finalPersonalScores;
    }
    
    public @Nullable Collection<FinalTeamScore> logFinalTeamScores(@NotNull Collection<FinalTeamScore> finalTeamScores) throws SQLException {
        finalTeamScoreDao.create(finalTeamScores);
        return finalTeamScores;
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
        instantPersonalScoreDao.deleteBuilder().delete();
        instantTeamScoreDao.deleteBuilder().delete();
        gameSessionDao.deleteBuilder().delete();
        finalPersonalScoreDao.deleteBuilder().delete();
        finalTeamScoreDao.deleteBuilder().delete();
        return true;
    }
    
    public @Nullable ParticipantCurrency createParticipantCurrencyIfNotExists(@NotNull ParticipantCurrency participantCurrency) {
        try {
            participantCurrencyDao.createIfNotExists(participantCurrency);
            return participantCurrency;
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("Error creating ParticipantCurrency %s", participantCurrency), e);
            return null;
        }
    }
    
    /**
     * @param uuid the uuid of the {@link ParticipantCurrency} object to add the given amount to
     * @param amount the amount to add to the {@link ParticipantCurrency#getCurrent()} and
     * {@link ParticipantCurrency#getLifetime()} values
     * @return The {@link ParticipantCurrency} object associated with the given uuid with the updated
     * {@link ParticipantCurrency#getCurrent()} and {@link ParticipantCurrency#getLifetime()} values
     * after the given amount has been added to them
     * @throws SQLException if there is an issue communicating with the database, or if the given uuid doesn't exist in
     * the database
     */
    public @NotNull ParticipantCurrency addParticipantCurrency(@NotNull UUID uuid, int amount) throws SQLException {
        return TransactionManager.callInTransaction(participantCurrencyDao.getConnectionSource(), () -> {
            ParticipantCurrency currency = participantCurrencyDao.queryForId(uuid.toString());
            if (currency == null) {
                throw new SQLException(String.format("No entry found in ParticipantCurrency table for UUID %s", uuid));
            }
            currency.setCurrent(currency.getCurrent() + amount);
            currency.setLifetime(currency.getLifetime() + amount);
            participantCurrencyDao.update(currency);
            return currency;
        });
    }
    
    /**
     * @param amounts Each participant's uuid mapped to the amount to add to their
     * {@link ParticipantCurrency#getCurrent()} and {@link ParticipantCurrency#getLifetime()} values
     * @return A list of the {@link ParticipantCurrency} associated with the given uuids with the updated
     * {@link ParticipantCurrency#getCurrent()} and {@link ParticipantCurrency#getLifetime()} values
     * after the given amount has been added to them
     * @throws SQLException if there is an issue communicating with the database, or if any of the given uuids don't
     * exist in the database
     */
    public @NotNull List<ParticipantCurrency> addParticipantCurrencies(@NotNull Map<UUID, Integer> amounts) throws SQLException {
        return TransactionManager.callInTransaction(participantCurrencyDao.getConnectionSource(), () -> {
            List<ParticipantCurrency> updated = new ArrayList<>(amounts.size());
            
            for (Map.Entry<UUID, Integer> entry : amounts.entrySet()) {
                UUID uuid = entry.getKey();
                int amount = entry.getValue();
                
                ParticipantCurrency currency = participantCurrencyDao.queryForId(uuid.toString());
                if (currency == null) {
                    throw new SQLException(String.format("No entry found in ParticipantCurrency table  for UUID %s", uuid));
                }
                
                currency.setCurrent(currency.getCurrent() + amount);
                currency.setLifetime(currency.getLifetime() + amount);
                participantCurrencyDao.update(currency);
                updated.add(currency);
            }
            return updated;
        });
    }
}
