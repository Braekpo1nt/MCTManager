package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
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
    private final @NotNull Dao<EventAdminEntity, String> eventAdminDao;
    
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
    
    public void addMaintenanceTeams(Collection<MaintenanceTeam> teams) throws SQLException {
        maintenanceTeamsDao.create(teams);
    }
    
    public List<MaintenanceTeam> getAllMaintenanceTeams() throws SQLException {
        return maintenanceTeamsDao.queryForAll();
    }
    
    public void addParticipant(@NotNull MaintenanceParticipantEntity participant, String ign) throws SQLException {
        TransactionManager.callInTransaction(maintenanceParticipantsDao.getConnectionSource(), () -> {
            registerParticipantIfNot(participant.getParticipantUUID(), ign);
            maintenanceParticipantsDao.create(participant);
            return null;
        });
    }
    
    public void deleteMaintenanceParticipant(@NotNull String uuid) throws SQLException {
        maintenanceParticipantsDao.deleteById(uuid);
    }
    
    // Practice
    
    public PracticeTeam addTeam(PracticeTeam team) throws SQLException {
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
            return true;
        });
    }
    
    public Collection<PracticeTeam> addPracticeTeams(Collection<PracticeTeam> teams) throws SQLException {
        practiceTeamsDao.create(teams);
        return teams;
    }
    
    public List<PracticeTeam> getAllPracticeTeams() throws SQLException {
        return practiceTeamsDao.queryForAll();
    }
    
    public void addParticipant(@NotNull PracticeParticipantEntity participant, @NotNull String ign) throws SQLException {
        TransactionManager.callInTransaction(practiceParticipantsDao.getConnectionSource(), () -> {
            registerParticipantIfNot(participant.getParticipantUUID(), ign);
            practiceParticipantsDao.create(participant);
            return null;
        });
    }
    
    public void deletePracticeParticipant(@NotNull String uuid) throws SQLException {
        practiceParticipantsDao.deleteById(uuid);
    }
    
    public List<PracticeParticipantEntity> getAllPracticeParticipants() throws SQLException {
        return practiceParticipantsDao.queryForAll();
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
    
    /**
     * @param eventId the eventId to get all the teams for.
     * @return the {@link EventTeam}s associated with the given ID.
     * If the eventId doesn't exist, list will be empty.
     * @throws SQLException if there is a problem communicating with the database.
     */
    public List<EventTeam> getAllEventTeams(@NotNull String eventId) throws SQLException {
        return eventTeamsDao.queryForEq("event_id", eventId);
    }
    
    public void addParticipant(@NotNull EventParticipantEntity participant, @NotNull String ign) throws SQLException {
        TransactionManager.callInTransaction(activeParticipantsDao.getConnectionSource(), () -> {
            registerParticipantIfNot(participant.getParticipantUUID(), ign);
            eventParticipantsDao.create(participant);
            return null;
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
    
    /**
     * @param eventId the eventId to get all the participants for
     * @return a list of all {@link EventParticipantEntity}s which share the given eventId,
     * or empty list of none exist
     * @throws SQLException if there's a problem communicating with the database.
     */
    public List<EventParticipantEntity> getAllEventParticipants(@NotNull String eventId) throws SQLException {
        return eventParticipantsDao.queryForEq("event_id", eventId);
    }
    
    /**
     * Using the given info, register a new participant with the all_players and player_metadata databases
     * Not implemented in a transaction for internal careful use only.
     * @param uuid the uuid of the player
     * @param ign the ign of the player
     * @throws SQLException if there is an exception
     */
    private void registerParticipantIfNot(@NotNull String uuid, @NotNull String ign) throws SQLException {
        allPlayersDao.createOrUpdate(AllPlayersEntity.builder()
                .uuid(uuid)
                .ign(ign)
                .firstSeenAt(new Date())
                .build());
        playerMetadataDao.createIfNotExists(PlayerMetadata.builder()
                .participantUUID(uuid)
                .ign(ign)
                .discordUsername(null)
                .currentTokens(0)
                .lifetimeTokens(0)
                .percentRank(0.0)
                .build());
    }
    
    public void registerParticipantIfNotRegistered(@NotNull AllPlayersEntity allPlayersEntity, @NotNull PlayerMetadata playerMetadata) throws SQLException {
        TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
            allPlayersDao.createOrUpdate(allPlayersEntity);
            playerMetadataDao.createIfNotExists(playerMetadata);
            return null;
        });
    }
    
    /**
     * Register all the given players at once. Handled in a transaction so if something goes wrong,
     * nothing is committed.
     * @param allPlayersEntities all the players
     * @param playerMetadatas all the metadatas
     * @throws SQLException if there's a database error
     */
    public void registerParticipantsIfNotRegistered(@NotNull List<AllPlayersEntity> allPlayersEntities, @NotNull List<PlayerMetadata> playerMetadatas) throws SQLException {
        TransactionManager.callInTransaction(allPlayersDao.getConnectionSource(), () -> {
            for (AllPlayersEntity allPlayersEntity : allPlayersEntities) {
                allPlayersDao.createOrUpdate(allPlayersEntity);
            }
            for (PlayerMetadata playerMetadata : playerMetadatas) {
                playerMetadataDao.createIfNotExists(playerMetadata);
            }
            return null;
        });
    }
    
    public static class MultiplePlayersWithNameException extends Exception {
        public MultiplePlayersWithNameException(String ign) {
            super(String.format("Multiple players with the ign \"%s\" exist", ign));
        }
    }
    
    public @Nullable AllPlayersEntity getPlayer(String ign) throws SQLException, MultiplePlayersWithNameException {
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
     * @param ign the in-game-name of the player to find the metadata of
     * @return the first-found player with that ign, or null if there is no such player
     * @throws SQLException if there is an error
     */
    public @Nullable PlayerMetadata getPlayerMetadata(@NotNull String ign) throws SQLException {
        return playerMetadataDao.queryForFieldValuesArgs(
                        Map.of("ign", ign)
                ).stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * @return a list of all player IGNs in the all_players table
     * @throws SQLException if there is a SQL error
     */
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
            registerParticipantIfNot(participant.getParticipantUUID(), participant.getIgn());
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
    
    public void deleteEventAdmin(@NotNull String uuid) throws SQLException {
        eventAdminDao.deleteById(uuid);
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
    
    public void addIfNotExists(@NotNull InGameTeam team) throws SQLException {
        inGameteamsDao.createIfNotExists(team);
    }
    
    public void addIfNotExists(@NotNull InGameParticipant participant) throws SQLException {
        inGameParticipantsDao.createIfNotExists(participant);
    }
    
    public void createOrUpdate(@NotNull InGameTeam team) throws SQLException {
        inGameteamsDao.createOrUpdate(team);
    }
    
    public void createOrUpdate(@NotNull InGameParticipant participant) throws SQLException {
        inGameParticipantsDao.createOrUpdate(participant);
    }
    
    public void deleteTeamInGame(@NotNull String teamId) throws SQLException {
        // safe if row with id doesn't exist
        inGameteamsDao.deleteById(teamId);
    }
    
    public void deleteParticipantInGame(@NotNull String participantUUID) throws SQLException {
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
