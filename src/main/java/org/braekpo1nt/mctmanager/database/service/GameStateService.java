package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.entities.SystemState;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.MaintenanceAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.admin.PracticeAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.InGameParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.InGameTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
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

@SuppressWarnings("UnusedReturnValue")
public class GameStateService {
    private final @NotNull String mode;
    private final @NotNull Dao<AllPlayersEntity, String> allPlayersDao;
    private final @NotNull Dao<PlayerMetadata, String> playerMetadataDao;
    private final @NotNull Dao<SystemState, Integer> systemStateDao;
    
    private final @NotNull Dao<ActiveTeam, String> activeTeamsDao;
    private final @NotNull Dao<ActiveParticipant, String> activeParticipantsDao;
    private final @NotNull Dao<ActiveAdminEntity, String> activeAdminDao;
    private final @NotNull Dao<MaintenanceAdminEntity, String> maintenanceAdminDao;
    private final @NotNull Dao<PracticeAdminEntity, String> practiceAdminDao;
    private final @NotNull Dao<EventAdminEntity, Integer> eventAdminDao;
    
    private final @NotNull Dao<InGameTeam, String> inGameteamsDao;
    private final @NotNull Dao<InGameParticipant, String> inGameParticipantsDao;
    
    private final @NotNull Dao<MaintenanceTeam, String> maintenanceTeamsDao;
    private final @NotNull Dao<PracticeTeam, String> practiceTeamsDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<MaintenanceParticipantEntity, String> maintenanceParticipantsDao;
    private final @NotNull Dao<PracticeParticipantEntity, String> practiceParticipantsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    
    public GameStateService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.allPlayersDao = database.getAllPlayersDao();
        this.playerMetadataDao = database.getPlayerMetadataDao();
        this.systemStateDao = database.getSystemStateDao();
        
        this.activeTeamsDao = database.getActiveTeamsDao();
        this.activeParticipantsDao = database.getActiveParticipantsDao();
        this.activeAdminDao = database.getActiveAdminDao();
        this.maintenanceAdminDao = database.getMaintenanceAdminDao();
        this.practiceAdminDao = database.getPracticeAdminDao();
        this.eventAdminDao = database.getEventAdminDao();
        
        this.inGameteamsDao = database.getInGameTeamsDao();
        this.inGameParticipantsDao = database.getInGameParticipantsDao();
        
        this.maintenanceTeamsDao = database.getMaintenanceTeamsDao();
        this.practiceTeamsDao = database.getPracticeTeamsDao();
        this.eventTeamsDao = database.getEventTeamsDao();
        this.maintenanceParticipantsDao = database.getMaintenanceParticipantsDao();
        this.practiceParticipantsDao = database.getPracticeParticipantsDao();
        this.eventParticipantsDao = database.getEventParticipantsDao();
    }
    
    // Maintenance
    
    public void addTeam(@NotNull MaintenanceTeam team) throws SQLException {
        maintenanceTeamsDao.create(team);
    }
    
    /**
     * Deletes all participants associated with the team, then deletes the team
     * @param teamId the id of the team to delete
     * @return true if the id was deleted successfully, false if the id doesn't exist
     * @throws SQLException if there's a database error
     */
    public boolean deleteMaintenanceTeam(@NotNull String teamId) throws SQLException {
        return TransactionManager.callInTransaction(maintenanceTeamsDao.getConnectionSource(), () -> {
            if (!maintenanceTeamsDao.idExists(teamId)) {
                return false;
            }
            DeleteBuilder<MaintenanceParticipantEntity, String> deleteBuilder = maintenanceParticipantsDao.deleteBuilder();
            deleteBuilder.where()
                    .eq("team_id", teamId);
            deleteBuilder.delete();
            maintenanceTeamsDao.deleteById(teamId);
            return true;
        });
    }
    
    public RegisterConflictType addParticipant(@NotNull MaintenanceParticipantEntity participant, String ign) throws SQLException {
        return TransactionManager.callInTransaction(maintenanceParticipantsDao.getConnectionSource(), () -> {
            RegisterConflictType type = _registerPlayer(participant.getParticipantUUID(), ign);
            maintenanceParticipantsDao.create(participant);
            return type;
        });
    }
    
    public void deleteMaintenanceParticipant(@NotNull String uuid) throws SQLException {
        maintenanceParticipantsDao.deleteById(uuid);
    }
    
    // Practice
    
    public PracticeTeam addTeam(PracticeTeam team) throws SQLException {
        Main.logf("adding practice team %s", team.getTeamId());
        practiceTeamsDao.create(team);
        return team;
    }
    
    /**
     * Deletes all participants associated with the team, then deletes the team
     * @param teamId the id of the team to delete
     * @return true if the id was deleted successfully, false if the id doesn't exist
     * @throws SQLException if there's a database error
     */
    public boolean deletePracticeTeam(@NotNull String teamId) throws SQLException {
        return TransactionManager.callInTransaction(practiceTeamsDao.getConnectionSource(), () -> {
            if (!practiceTeamsDao.idExists(teamId)) {
                return false;
            }
            DeleteBuilder<PracticeParticipantEntity, String> deleteBuilder = practiceParticipantsDao.deleteBuilder();
            deleteBuilder.where()
                    .eq("team_id", teamId);
            deleteBuilder.delete();
            practiceTeamsDao.deleteById(teamId);
            Main.logf("deleting practice team %s", teamId);
            return true;
        });
    }
    
    public RegisterConflictType addParticipant(@NotNull PracticeParticipantEntity participant, @NotNull String ign) throws SQLException {
        return TransactionManager.callInTransaction(practiceParticipantsDao.getConnectionSource(), () -> {
            RegisterConflictType type = _registerPlayer(participant.getParticipantUUID(), ign);
            practiceParticipantsDao.create(participant);
            return type;
        });
    }
    
    public void deletePracticeParticipant(@NotNull String uuid) throws SQLException {
        practiceParticipantsDao.deleteById(uuid);
    }
    
    // Event
    
    public EventTeam addTeam(EventTeam team) throws SQLException {
        eventTeamsDao.create(team);
        return team;
    }
    
    /**
     * Deletes all participants associated with the team, then deletes the team
     * @param teamId the id of the team to delete
     * @param eventId the id of the event to delete the team from
     * @return true if the id was deleted successfully, false if the id doesn't exist
     * @throws SQLException if there's a database error
     */
    public boolean deleteEventTeam(@NotNull String teamId, @NotNull String eventId) throws SQLException {
        return TransactionManager.callInTransaction(eventTeamsDao.getConnectionSource(), () -> {
            QueryBuilder<EventTeam, Integer> queryBuilder = eventTeamsDao.queryBuilder();
            queryBuilder.where()
                    .eq("team_id", teamId)
                    .and()
                    .eq("event_id", eventId)
            ;
            List<EventTeam> queryResult = queryBuilder.query();
            if (queryResult.isEmpty()) {
                return false;
            }
            int id = queryResult.getFirst().getId();
            DeleteBuilder<EventParticipantEntity, Integer> participantDeleteBuilder = eventParticipantsDao.deleteBuilder();
            participantDeleteBuilder.where()
                    .eq("team_id", teamId)
                    .and()
                    .eq("event_id", eventId)
            ;
            participantDeleteBuilder.delete();
            eventTeamsDao.deleteById(id);
            return true;
        });
    }
    
    public RegisterConflictType addParticipant(@NotNull EventParticipantEntity participant, @NotNull String ign) throws SQLException {
        return TransactionManager.callInTransaction(activeParticipantsDao.getConnectionSource(), () -> {
            RegisterConflictType type = _registerPlayer(participant.getParticipantUUID(), ign);
            eventParticipantsDao.create(participant);
            return type;
        });
    }
    
    public void deleteEventParticipant(String uuid, String eventId) throws SQLException {
        DeleteBuilder<EventParticipantEntity, Integer> builder = eventParticipantsDao.deleteBuilder();
        builder.where()
                .eq("participant_uuid", uuid)
                .and()
                .eq("event_id", eventId)
        ;
        builder.delete();
    }
    
    private static class InvalidDatabaseStateException extends SQLException {
        public InvalidDatabaseStateException(String ign) {
            super(String.format("Somehow, multiple instances of the ign \"%s\" exist in the all_players database. This shouldn't be possible, since there is a uniqueness constraint on the ign column. Can't continue.", ign));
        }
    }
    
    /**
     * Register the given player in the database. Create their entry in the all_players and player_metadata tables.<br>
     * If a player with the given UUID exists in both, updates the IGN to the given IGN. <br>
     * If a player with the given IGN exists, but isn't associated with the given UUID, then this migrates
     * the UUID to reflect the correct state.
     * @param uuid the UUID of the player to add
     * @param ign the IGN of the player to add
     * @return the type of conflict that was repaired, or {@link RegisterConflictType#NONE} if no conflict needed to be resolved
     * @throws SQLException if there's a database error
     */
    public @NotNull RegisterConflictType registerPlayer(@NotNull String uuid, @NotNull String ign) throws SQLException {
        return TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(),
                () -> _registerPlayer(uuid, ign)
        );
    }
    
    private @NotNull RegisterConflictType _registerPlayer(@NotNull String uuid, @NotNull String ign) throws SQLException {
        // check if the user exists
        AllPlayersEntity existingPlayer = allPlayersDao.queryForId(uuid);
        if (existingPlayer != null) {
            // check if the ign is right
            if (existingPlayer.getIgn().equals(ign)) {
                // everything is correct, we are done
                return RegisterConflictType.NONE;
            }
            // if the uuid exists but the ign is wrong, we need to change the ign
            List<AllPlayersEntity> playersWithIgn = allPlayersDao.queryForEq("ign", ign);
            if (playersWithIgn.isEmpty()) {
                _migrateIgn(uuid, ign);
                return RegisterConflictType.MIGRATE_IGN;
            }
            if (playersWithIgn.size() > 1) {
                // Should never happen with properly configured database
                throw new InvalidDatabaseStateException(ign);
            }
            // there is a player with the ign, so we need to migrate the UUID and keep the ign 
            // instead of creating a new entry in all_players
            _migrateFromUUIDToUUID(playersWithIgn.getFirst().getUuid(), uuid, ign);
            return RegisterConflictType.MIGRATE_UUID;
        }
        // a player with the UUID does not exist in the database
        List<AllPlayersEntity> playersWithIgn = allPlayersDao.queryForEq("ign", ign);
        if (playersWithIgn.isEmpty()) {
            // no players with the ign exist, create a new entry
            AllPlayersEntity newPlayer = AllPlayersEntity.builder()
                    .uuid(uuid)
                    .ign(ign)
                    .firstSeenAt(new Date())
                    .build();
            allPlayersDao.create(newPlayer);
            playerMetadataDao.create(PlayerMetadata.builder()
                    .participantUUID(uuid)
                    .ign(ign)
                    .lifetimeTokens(0)
                    .currentTokens(0)
                    .percentRank(0.0)
                    .discordUsername(null)
                    .build());
            return RegisterConflictType.NONE;
        }
        if (playersWithIgn.size() > 1) {
            // Should never happen with properly configured database
            throw new InvalidDatabaseStateException(ign);
        }
        // a player with the wrong UUID but the right ign exists in the database
        // we must migrate the UUID and keep the correct ign
        AllPlayersEntity playerWithIGN = playersWithIgn.getFirst();
        _migrateFromUUIDToUUID(playerWithIGN.getUuid(), uuid, ign);
        return RegisterConflictType.MIGRATE_UUID;
    }
    
    /**
     * Change the ign to the given value for every row associated with the given UUID in every table
     * private so that you can call in transaction without nested transaction
     * @param uuid the UUID to update the IGN for
     * @param ign the ign to set everything to
     * @throws SQLException if there's a database error
     */
    private void _migrateIgn(String uuid, String ign) throws SQLException {
        // no conflicts, simply change the value
        // all_players
        allPlayersDao.executeRaw("""
                UPDATE all_players
                SET ign = ?
                WHERE uuid = ?
                """, ign, uuid);
        // active_participants
        allPlayersDao.executeRaw("""
                UPDATE active_participants
                SET ign = ?
                WHERE participant_uuid = ?
                """, ign, uuid);
        // player_metadata
        allPlayersDao.executeRaw("""
                UPDATE player_metadata
                SET ign = ?
                WHERE participant_uuid = ?
                """, ign, uuid);
    }
    
    /**
     * Change the ign to the given value for every row associated with the given UUID in every table
     * @param uuid the UUID to update the IGN for
     * @param ign the ign to set everything to
     * @return true if the ign was successfully changed, false if nothing happened.
     * Nothing will happen if a player with the given uuid doesn't exist, if a player with the given IGN already exists,
     * or if the uuid has the correct name already.
     * @throws SQLException if there's a database error
     */
    public boolean migrateIgn(@NotNull String uuid, @NotNull String ign) throws SQLException {
        return TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
            AllPlayersEntity playerWithUUID = allPlayersDao.queryForId(uuid);
            if (playerWithUUID == null) {
                // no player to migrate the ign of
                return false;
            }
            if (playerWithUUID.getIgn().equals(ign)) {
                // the name is correct already
                return false;
            }
            List<AllPlayersEntity> playersWithIgn = allPlayersDao.queryForEq("ign", ign);
            if (playersWithIgn.isEmpty()) {
                // we can safely change the IGN of the player
                _migrateIgn(uuid, ign);
                return true;
            }
            if (playersWithIgn.size() > 1) {
                throw new InvalidDatabaseStateException(ign);
            }
            // a player with the ign exists already, can't continue
            return false;
        });
    }
    
    public void migrateFromUUIDToUUID(String from, String to, String ign) throws SQLException {
        TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
            _migrateFromUUIDToUUID(from, to, ign);
            return null;
        });
    }
    
    /**
     * Make all references to the wrong uuid reference the correct uuid
     * @param from the wrong uuid
     * @param to the correct uuid
     * @throws SQLException if there's a database error
     */
    private void _migrateFromUUIDToUUID(String from, String to, String ign) throws SQLException {
        // create a new row with the correct uuid (or update the existing row to reflect the correct IGN)
        UpdateBuilder<AllPlayersEntity, String> updateBuilder = allPlayersDao.updateBuilder();
        updateBuilder.where()
                .idEq(from);
        updateBuilder.updateColumnValue("ign", "****invalid****//");
        updateBuilder.update();
        allPlayersDao.createOrUpdate(AllPlayersEntity.builder()
                .uuid(to)
                .ign(ign)
                .firstSeenAt(new Date())
                .build());
        
        /*
        The delete statements are so that if the new IGN already exists, we just
        delete the incorrect old IGN and leave the correct new one
         */
        
        // active_admins
        allPlayersDao.executeRaw("""
                DELETE FROM active_admins
                WHERE uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM active_admins
                    WHERE uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE active_admins
                SET uuid = ?
                WHERE uuid = ?
                """, to, from);
        // maintenance_admins
        allPlayersDao.executeRaw("""
                DELETE FROM maintenance_admins
                WHERE uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM maintenance_admins
                    WHERE uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE maintenance_admins
                SET uuid = ?
                WHERE uuid = ?
                """, to, from);
        // practice_admins
        allPlayersDao.executeRaw("""
                DELETE FROM practice_admins
                WHERE uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM practice_admins
                    WHERE uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE practice_admins
                SET uuid = ?
                WHERE uuid = ?
                """, to, from);
        // maintenance_participants
        allPlayersDao.executeRaw("""
                DELETE FROM maintenance_participants
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM maintenance_participants
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE maintenance_participants
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // practice_participants
        allPlayersDao.executeRaw("""
                DELETE FROM practice_participants
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM practice_participants
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE practice_participants
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // active_participants
        allPlayersDao.executeRaw("""
                DELETE FROM active_participants
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM active_participants
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE active_participants
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // Correct the name
        allPlayersDao.executeRaw("""
                UPDATE active_participants
                SET ign = ?
                WHERE participant_uuid = ?
                """, ign, to);
        // in_game_participants
        allPlayersDao.executeRaw("""
                DELETE FROM in_game_participants
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM in_game_participants
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE in_game_participants
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // player_metadata
        allPlayersDao.executeRaw("""
                DELETE FROM player_metadata
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM player_metadata
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE player_metadata
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // Correct the ign
        allPlayersDao.executeRaw("""
                UPDATE player_metadata
                SET ign = ?
                WHERE participant_uuid = ?
                """, ign, to);
        
        // event_admins
        allPlayersDao.executeRaw("""
                DELETE FROM event_admins
                WHERE uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM event_admins
                    WHERE uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE event_admins
                SET uuid = ?
                WHERE uuid = ?
                """, to, from);
        // event_participants
        allPlayersDao.executeRaw("""
                DELETE FROM event_participants
                WHERE participant_uuid = ?
                AND EXISTS (
                    SELECT 1
                    FROM event_participants
                    WHERE participant_uuid = ?
                )
                """, from, to);
        allPlayersDao.executeRaw("""
                UPDATE event_participants
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        // score_events
        // primary key is not participant_uuid, so there can be duplicates, so simply migrate
        allPlayersDao.executeRaw("""
                UPDATE score_events
                SET participant_uuid = ?
                WHERE participant_uuid = ?
                """, to, from);
        
        // now that all references to the old UUID are removed, delete the old entry
        allPlayersDao.deleteById(from);
    }
    
    /**
     * Register all the given players at once. Handled in a transaction so if something goes wrong,
     * nothing is committed.
     * @param uuidsToIGNs all the players
     * @return true if you need to reload the game state, false if no game state changes were made
     * @throws SQLException if there's a database error
     * @deprecated not used anymore
     */
    @Deprecated
    public boolean registerPlayers(@NotNull Map<String, String> uuidsToIGNs) throws SQLException {
        return TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
            boolean shouldReloadGameState = false;
            for (Map.Entry<String, String> entry : uuidsToIGNs.entrySet()) {
                String uuid = entry.getKey();
                String ign = entry.getValue();
                RegisterConflictType type = _registerPlayer(uuid, ign);
                shouldReloadGameState = shouldReloadGameState || !type.equals(RegisterConflictType.NONE);
            }
            return shouldReloadGameState;
        });
    }
    
    public static class MultiplePlayersWithNameException extends Exception {
        public MultiplePlayersWithNameException(String ign) {
            super(String.format("Multiple players with the ign \"%s\" exist", ign));
        }
    }
    
    public @Nullable AllPlayersEntity getPlayer(String uuid) throws SQLException {
        return allPlayersDao.queryForId(uuid);
    }
    
    /**
     * @param ign the ign to look up
     * @return the {@link AllPlayersEntity} with the given ign, or null if
     * no such player exists
     * @throws SQLException if there's a database error
     * @throws MultiplePlayersWithNameException if multiple players exist in the database with the same IGN (should not
     * happen because unique IGNs should be enforced by the database)
     */
    public @Nullable AllPlayersEntity getPlayerByIgn(String ign) throws SQLException, MultiplePlayersWithNameException {
        List<AllPlayersEntity> options = allPlayersDao.queryForEq("ign", ign);
        if (options.isEmpty()) {
            return null;
        }
        if (options.size() != 1) {
            throw new MultiplePlayersWithNameException(ign);
        }
        return options.getFirst();
    }
    
    public void update(@NotNull PlayerMetadata playerMetadata) throws SQLException {
        playerMetadataDao.update(playerMetadata);
    }
    
    public @Nullable PlayerMetadata getPlayerMetadata(@NotNull UUID uuid) throws SQLException {
        return playerMetadataDao.queryForId(uuid.toString());
    }
    
    /**
     * @param uuids the UUIDs of the PlayerMetadata objects to get from the database.
     * @return a matching PlayerMetadata entry for each UUID given, unless an entry can't be found
     * for a UUID in which case nothing is added for it. Thus, the resulting list may or may not be
     * the same size as the given list, depending on the presence of the ids in the database.
     * @throws SQLException if there is a SQL Error
     */
    public @NotNull List<PlayerMetadata> getPlayerMetadatas(Collection<UUID> uuids) throws SQLException {
        List<PlayerMetadata> playerMetadatas = new ArrayList<>(uuids.size());
        for (UUID uuid : uuids) {
            PlayerMetadata playerMetadata = playerMetadataDao.queryForId(uuid.toString());
            if (playerMetadata != null) {
                playerMetadatas.add(playerMetadata);
            }
        }
        return playerMetadatas;
    }
    
    /**
     * @return a list of all player IGNs in the all_players table
     * @throws SQLException if there is a SQL error
     */
    @SuppressWarnings("unused")
    public @NotNull List<String> getPlayerIGNs() throws SQLException {
        String sql = """
                SELECT
                    ap.ign
                FROM all_players ap
                """;
        try (GenericRawResults<String[]> raw =
                     allPlayersDao.queryRaw(sql)) {
            List<String[]> rawResults = raw.getResults();
            List<String> result = new ArrayList<>(rawResults.size());
            for (String[] row : rawResults) {
                String ign = row[0];
                result.add(ign);
            }
            return result;
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting player names", e);
        }
    }
    
    /**
     * @return a list of all player IGNs in the all_players table which start with the given partial string
     * @throws SQLException if there is a SQL error
     */
    public @NotNull List<String> getPlayerIGNsPartialMatch(@NotNull String partial) throws SQLException {
        String sql = """
                SELECT
                    ap.ign
                FROM all_players ap
                WHERE ap.ign LIKE CONCAT(?, '%')
                """;
        try (GenericRawResults<String[]> raw =
                     allPlayersDao.queryRaw(sql, partial)) {
            List<String[]> rawResults = raw.getResults();
            List<String> result = new ArrayList<>(rawResults.size());
            for (String[] row : rawResults) {
                String ign = row[0];
                result.add(ign);
            }
            return result;
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting player names", e);
        }
    }
    
    public void addTeam(@NotNull ActiveTeam team) throws SQLException {
        activeTeamsDao.create(team);
    }
    
    public void addParticipant(@NotNull ActiveParticipant participant) throws SQLException {
        TransactionManager.callInTransaction(activeParticipantsDao.getConnectionSource(), () -> {
            _registerPlayer(participant.getParticipantUUID(), participant.getIgn());
            activeParticipantsDao.create(participant);
            return null;
        });
    }
    
    public List<ActiveTeam> getActiveTeams() throws SQLException {
        return activeTeamsDao.queryForAll();
    }
    
    public List<ActiveParticipant> getActiveParticipants() throws SQLException {
        return activeParticipantsDao.queryForAll();
    }
    
    public void updateActiveParticipant(@NotNull ActiveParticipant activeParticipant) throws SQLException {
        activeParticipantsDao.update(activeParticipant);
    }
    
    public void updateActiveTeam(@NotNull ActiveTeam activeTeam) throws SQLException {
        activeTeamsDao.update(activeTeam);
    }
    
    public void updateActiveParticipants(@NotNull List<ActiveParticipant> activeParticipants) throws Exception {
        if (activeParticipants.isEmpty()) {
            return;
        }
        activeParticipantsDao.callBatchTasks(() -> {
            for (ActiveParticipant activeParticipant : activeParticipants) {
                activeParticipantsDao.update(activeParticipant);
            }
            return null;
        });
    }
    
    public void updateActiveTeams(@NotNull List<ActiveTeam> activeTeams) throws Exception {
        if (activeTeams.isEmpty()) {
            return;
        }
        activeTeamsDao.callBatchTasks(() -> {
            for (ActiveTeam activeTeam : activeTeams) {
                activeTeamsDao.update(activeTeam);
            }
            return null;
        });
    }
    
    /**
     * increment the version of the system_state active_version by 1
     * @throws SQLException if there is an issue communicating with the database
     */
    public void incrementSystemStateVersion() throws SQLException {
        systemStateDao.updateBuilder()
                .updateColumnValue("active_version",
                        new ColumnArg("active_version + 1")
                );
    }
    
    /**
     * Deletes the team and all participants who are on that team
     * @param teamId the teamId of the team to delete
     * @throws SQLException if there is an issue communicating to the database
     */
    public void deleteTeam(@NotNull String teamId) throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            DeleteBuilder<ActiveParticipant, String> deleteBuilder = activeParticipantsDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("team_id", teamId);
            deleteBuilder.delete();
            activeTeamsDao.deleteById(teamId);
            return null;
        });
    }
    
    public void deleteParticipant(@NotNull String participantUUID) throws SQLException {
        activeParticipantsDao.deleteById(participantUUID);
    }
    
    // Admins
    
    public void addAdmin(@NotNull ActiveAdminEntity admin) throws SQLException {
        activeAdminDao.create(admin);
    }
    
    public void addAdmin(@NotNull MaintenanceAdminEntity admin) throws SQLException {
        maintenanceAdminDao.create(admin);
    }
    
    public void addAdmin(@NotNull PracticeAdminEntity admin) throws SQLException {
        practiceAdminDao.create(admin);
    }
    
    public void addAdmin(@NotNull EventAdminEntity admin) throws SQLException {
        eventAdminDao.create(admin);
    }
    
    public void deleteActiveAdmin(@NotNull String uuid) throws SQLException {
        activeAdminDao.deleteById(uuid);
    }
    
    public void deleteMaintenanceAdmin(@NotNull String uuid) throws SQLException {
        maintenanceAdminDao.deleteById(uuid);
    }
    
    public void deletePracticeAdmin(@NotNull String uuid) throws SQLException {
        practiceAdminDao.deleteById(uuid);
    }
    
    public void deleteEventAdmin(@NotNull String uuid, @NotNull String eventId) throws SQLException {
        DeleteBuilder<EventAdminEntity, Integer> builder = eventAdminDao.deleteBuilder();
        builder.where()
                .eq("uuid", uuid)
                .and()
                .eq("event_id", eventId)
        ;
        builder.delete();
    }
    
    public List<ActiveAdminEntity> getActiveAdmins() throws SQLException {
        return activeAdminDao.queryForAll();
    }
    
    /**
     * @return a map from UUID to Admin Names
     * @throws SQLException if there's a SQL error
     */
    public @NotNull Map<UUID, String> getAdminNames() throws SQLException {
        String sql = """
                SELECT
                    ad.uuid,
                    ap.ign
                FROM admins ad
                LEFT JOIN all_players ap
                    ON ad.uuid = ap.uuid;
                """;
        Map<UUID, String> result = new HashMap<>();
        try (GenericRawResults<String[]> raw =
                     activeAdminDao.queryRaw(sql)) {
            for (String[] row : raw.getResults()) {
                UUID uuid = UUID.fromString(row[0]);
                String ign = row[1];
                result.put(uuid, ign);
            }
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting admin names", e);
        }
        return result;
    }
    
    // In Game
    
    public void addOrUpdateTeam(@NotNull InGameTeam team) throws SQLException {
        inGameteamsDao.createOrUpdate(team);
    }
    
    public void addOrUpdateParticipant(@NotNull InGameParticipant participant) throws SQLException {
        inGameParticipantsDao.createOrUpdate(participant);
    }
    
    public void deleteInGameTeam(@NotNull String teamId) throws SQLException {
        // safe if row with id doesn't exist
        inGameteamsDao.deleteById(teamId);
    }
    
    public void deleteInGameParticipant(@NotNull String participantUUID) throws SQLException {
        inGameParticipantsDao.deleteById(participantUUID);
    }
    
    /**
     * Add the given rows to the in-game tables, or update them to match
     * the given teams and participants if they already exist
     * @param teams the teams to add
     * @param participants the participants to add
     * @throws SQLException if there is an issue communicating with the database
     */
    public void addInGameParticipantsAndTeams(@NotNull List<InGameTeam> teams, @NotNull List<InGameParticipant> participants) throws SQLException {
        TransactionManager.callInTransaction(inGameParticipantsDao.getConnectionSource(), () -> {
            try {
                inGameteamsDao.callBatchTasks(() -> {
                    for (InGameTeam team : teams) {
                        inGameteamsDao.createOrUpdate(team);
                    }
                    return null;
                });
                inGameParticipantsDao.callBatchTasks(() -> {
                    for (InGameParticipant participant : participants) {
                        inGameParticipantsDao.createOrUpdate(participant);
                    }
                    return null;
                });
            } catch (Exception e) {
                throw new SQLException("Error occurred creating participants and teams in-game", e);
            }
            return null;
        });
    }
    
    /**
     * Remove the rows from the in-game tables for the given gameSessionId
     * @param gameSessionId the game session ID to delete the entries for
     * @throws SQLException if there's an issue communicating with the database
     */
    public void removeInGameTeamsAndParticipants(int gameSessionId) throws SQLException {
        TransactionManager.callInTransaction(inGameParticipantsDao.getConnectionSource(), () -> {
            DeleteBuilder<InGameTeam, String> teamDeleteBuilder = inGameteamsDao.deleteBuilder();
            teamDeleteBuilder
                    .where()
                    .eq("session_id", gameSessionId);
            teamDeleteBuilder.delete();
            
            DeleteBuilder<InGameParticipant, String> participantDeleteBuilder = inGameParticipantsDao.deleteBuilder();
            participantDeleteBuilder
                    .where()
                    .eq("session_id", gameSessionId);
            participantDeleteBuilder.delete();
            return null;
        });
    }
    
    public void update(@NotNull InGameParticipant participant) throws SQLException {
        inGameParticipantsDao.update(participant);
    }
    
    public void update(@NotNull InGameTeam team) throws SQLException {
        inGameteamsDao.update(team);
    }
    
    public void updateInGameParticipants(@NotNull List<InGameParticipant> participants) throws Exception {
        if (participants.isEmpty()) {
            return;
        }
        activeParticipantsDao.callBatchTasks(() -> {
            for (InGameParticipant participant : participants) {
                inGameParticipantsDao.update(participant);
            }
            return null;
        });
    }
    
    public void updateInGameTeams(@NotNull List<InGameTeam> teams) throws Exception {
        if (teams.isEmpty()) {
            return;
        }
        activeTeamsDao.callBatchTasks(() -> {
            for (InGameTeam team : teams) {
                inGameteamsDao.update(team);
            }
            return null;
        });
    }
    
    public void rebuildPracticeMode() throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            activeTeamsDao.executeRaw("DELETE FROM active_admins");
            
            // rebuild teams
            activeTeamsDao.executeRaw("""
                    INSERT INTO active_teams (team_id, display_name, color, score)
                    SELECT
                        pt.team_id,
                        pt.display_name,
                        pt.color,
                        COALESCE(se.total, 0) AS total
                    FROM practice_teams pt
                    LEFT JOIN (
                        SELECT
                            se.team_id,
                            SUM(
                                CASE
                                    WHEN se.session_id IS NULL THEN se.points_base
                                    WHEN gs.session_undone = FALSE THEN se.points_base * gs.multiplier
                                    ELSE 0
                                END
                            ) AS total
                        FROM score_events se
                        LEFT JOIN game_sessions gs
                            ON gs.id = se.session_id
                        WHERE se.mode = 'practice'
                        GROUP BY se.team_id
                    ) se
                        ON se.team_id = pt.team_id
                    """);
            
            // rebuild participants
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                        SELECT
                            pp.participant_uuid,
                            pp.team_id,
                            ap.ign,
                            COALESCE(se.total, 0) AS total
                        FROM practice_participants pp
                        JOIN all_players ap
                          ON ap.uuid = pp.participant_uuid
                        LEFT JOIN (
                            SELECT se.participant_uuid,
                                   SUM(se.points_base) AS total
                            FROM score_events se
                            LEFT JOIN game_sessions gs
                              ON gs.id = se.session_id
                            WHERE se.mode = 'practice'
                              AND se.participant_uuid IS NOT NULL
                              AND (
                                    se.session_id IS NULL
                                 OR gs.session_undone = FALSE
                              )
                            GROUP BY se.participant_uuid
                        ) se
                          ON se.participant_uuid = pp.participant_uuid
                    """);
            
            // rebuild admins
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_admins (uuid)
                        SELECT
                        	pa.uuid
                        FROM practice_admins pa
                    """);
            
            return null;
        });
    }
    
    /*
    to see what is essentially happening, take a look at the result of this command:

SELECT
      se.id,
      se.session_id,
      se.points_base,
      se.mode,
      se.participant_uuid,
      se.team_id,
      se.points_base,
      mt.display_name,
      mt.color,
      gs.session_undone,
      gs.multiplier
  FROM maintenance_teams mt
  LEFT JOIN score_events se
        ON se.team_id = mt.team_id
        AND se.mode = 'maintenance'
  LEFT JOIN game_sessions gs
      ON gs.id = se.session_id
      AND gs.session_undone = FALSE;
      
      The algorithm takes the resulting select, groups them by team_id, provides defaults for when the multiplier is missing (e.g. the COALESCE statements), applies a multiplier when a session_id is not null, and sums them up for each team_id, and puts the result in the active_teams table.
      
SELECT
        se.participant_uuid,
        se.team_id,
        ap.ign,
        se.mode,
        se.session_id,
        se.points_base,
        se.source_type,
        gs.multiplier,
        gs.session_undone
    FROM score_events se
    LEFT JOIN all_players ap
        ON ap.uuid = se.participant_uuid
    LEFT JOIN maintenance_participants mp
        ON mp.participant_uuid = se.participant_uuid
    LEFT JOIN game_sessions gs
      ON gs.id = se.session_id
      AND gs.session_undone = FALSE
    WHERE se.participant_uuid IS NOT NULL;
     */
    
    public void rebuildMaintenanceMode() throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            activeTeamsDao.executeRaw("DELETE FROM active_admins");
            
            // rebuild teams
            activeTeamsDao.executeRaw("""
                    INSERT INTO active_teams (team_id, display_name, color, score)
                    SELECT
                        mt.team_id,
                        mt.display_name,
                        mt.color,
                        COALESCE(se.total, 0) AS total
                    FROM maintenance_teams mt
                    LEFT JOIN (
                        SELECT
                            se.team_id,
                            SUM(
                                CASE
                                    WHEN se.session_id IS NULL THEN se.points_base
                                    WHEN gs.session_undone = FALSE THEN se.points_base * gs.multiplier
                                    ELSE 0
                                END
                            ) AS total
                        FROM score_events se
                        LEFT JOIN game_sessions gs
                            ON gs.id = se.session_id
                        WHERE se.mode = 'maintenance'
                        GROUP BY se.team_id
                    ) se
                        ON se.team_id = mt.team_id
                    """);
            
            // rebuild participants
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                        SELECT
                            mp.participant_uuid,
                            mp.team_id,
                            ap.ign,
                            COALESCE(se.total, 0) AS total
                        FROM maintenance_participants mp
                        JOIN all_players ap
                          ON ap.uuid = mp.participant_uuid
                        LEFT JOIN (
                            SELECT se.participant_uuid,
                                   SUM(se.points_base) AS total
                            FROM score_events se
                            LEFT JOIN game_sessions gs
                              ON gs.id = se.session_id
                            WHERE se.mode = 'maintenance'
                              AND se.participant_uuid IS NOT NULL
                              AND (
                                    se.session_id IS NULL
                                 OR gs.session_undone = FALSE
                              )
                            GROUP BY se.participant_uuid
                        ) se
                          ON se.participant_uuid = mp.participant_uuid
                    """);
            
            // rebuild admins
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_admins (uuid)
                        SELECT
                        	ma.uuid
                        FROM maintenance_admins ma
                    """);
            
            return null;
        });
    }
    
    public void rebuildEventMode(@NotNull String eventId) throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            activeTeamsDao.executeRaw("DELETE FROM active_admins");
            
            // rebuild teams
            activeTeamsDao.executeRaw("""
                            INSERT INTO active_teams (team_id, display_name, color, score)
                            SELECT
                                et.team_id,
                                et.display_name,
                                et.color,
                                CAST(COALESCE(se.total, 0) AS SIGNED) AS total
                            FROM event_teams et
                            LEFT JOIN (
                                SELECT
                                    se.team_id,
                                    SUM(
                                        CASE
                                            WHEN se.session_id IS NULL THEN se.points_base
                                            WHEN gs.session_undone = FALSE THEN se.points_base * gs.multiplier
                                            ELSE 0
                                        END
                                    ) AS total
                                FROM score_events se
                                LEFT JOIN game_sessions gs
                                    ON gs.id = se.session_id
                                WHERE se.event_id = ?
                                GROUP BY se.team_id
                            ) se
                                ON se.team_id = et.team_id
                            WHERE et.event_id = ?
                            """,
                    eventId,
                    eventId
            );
            
            // rebuild participants
            activeTeamsDao.executeRaw("""
                            INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                            SELECT
                                ep.participant_uuid,
                                ep.team_id,
                                ap.ign,
                                CAST(COALESCE(se.total, 0) AS SIGNED) AS total
                            FROM event_participants ep
                            JOIN all_players ap
                                ON ap.uuid = ep.participant_uuid
                            LEFT JOIN (
                                SELECT
                                    se.participant_uuid,
                                    SUM(
                                        CASE
                                            WHEN se.session_id IS NULL THEN se.points_base
                                            WHEN gs.session_undone = FALSE THEN se.points_base
                                            ELSE 0
                                        END
                                    ) AS total
                                FROM score_events se
                                LEFT JOIN game_sessions gs
                                    ON gs.id = se.session_id
                                WHERE se.event_id = ?
                                GROUP BY se.participant_uuid
                            ) se
                                ON se.participant_uuid = ep.participant_uuid
                            WHERE ep.event_id = ?
                            """,
                    eventId,
                    eventId
            );
            
            // rebuild admins
            activeTeamsDao.executeRaw("""
                            INSERT INTO active_admins (uuid)
                            SELECT
                            	ea.uuid
                            FROM event_admins ea
                            WHERE ea.event_id = ?
                            """,
                    eventId
            );
            
            return null;
        });
    }
}
