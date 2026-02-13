package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.PracticeParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class GameStateService {
    private final @NotNull String mode;
    private final @NotNull Dao<ActiveTeam, Integer> activeTeamsDao;
    private final @NotNull Dao<ActiveParticipant, Integer> activeParticipantsDao;
    
    private final @NotNull Dao<MaintenanceTeam, String> maintenanceTeamsDao;
    private final @NotNull Dao<PracticeTeam, String> practiceTeamsDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<MaintenanceParticipantEntity, String> maintenanceParticipantsDao;
    private final @NotNull Dao<PracticeParticipantEntity, String> practiceParticipantsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    
    public GameStateService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        
        this.activeTeamsDao = database.getActiveTeamsDao();
        this.activeParticipantsDao = database.getActiveParticipantsDao();
        
        this.maintenanceTeamsDao = database.getMaintenanceTeamsDao();
        this.practiceTeamsDao = database.getPracticeTeamsDao();
        this.eventTeamsDao = database.getEventTeamsDao();
        this.maintenanceParticipantsDao = database.getMaintenanceParticipantsDao();
        this.practiceParticipantsDao = database.getPracticeParticipantsDao();
        this.eventParticipantsDao = database.getEventParticipantsDao();
    }
    
    // Maintenance
    
    public void addTeam(MaintenanceTeam team) throws SQLException {
        maintenanceTeamsDao.create(team);
    }
    
    public void addMaintenanceTeams(Collection<MaintenanceTeam> teams) throws SQLException {
        maintenanceTeamsDao.create(teams);
    }
    
    public List<MaintenanceTeam> getAllMaintenanceTeams() throws SQLException {
        return maintenanceTeamsDao.queryForAll();
    }
    
    public void addParticipant(@NotNull MaintenanceParticipantEntity participant) throws SQLException {
        maintenanceParticipantsDao.create(participant);
    }
    
    public void addMaintenanceParticipants(@NotNull Collection<MaintenanceParticipantEntity> participants) throws SQLException {
        maintenanceParticipantsDao.create(participants);
    }
    
    public List<MaintenanceParticipantEntity> getAllMaintenanceParticipants() throws SQLException {
        return maintenanceParticipantsDao.queryForAll();
    }
    
    // Practice
    
    public PracticeTeam addTeam(PracticeTeam team) throws SQLException {
        practiceTeamsDao.create(team);
        return team;
    }
    
    public Collection<PracticeTeam> addPracticeTeams(Collection<PracticeTeam> teams) throws SQLException {
        practiceTeamsDao.create(teams);
        return teams;
    }
    
    public List<PracticeTeam> getAllPracticeTeams() throws SQLException {
        return practiceTeamsDao.queryForAll();
    }
    
    public void addParticipant(@NotNull PracticeParticipantEntity participant) throws SQLException {
        practiceParticipantsDao.create(participant);
    }
    
    public void addPracticeParticipants(@NotNull Collection<PracticeParticipantEntity> participants) throws SQLException {
        practiceParticipantsDao.create(participants);
    }
    
    public List<PracticeParticipantEntity> getAllPracticeParticipants() throws SQLException {
        return practiceParticipantsDao.queryForAll();
    }
    
    // Event
    
    public EventTeam addTeam(EventTeam team) throws SQLException {
        eventTeamsDao.create(team);
        return team;
    }
    
    public Collection<EventTeam> addEventTeams(Collection<EventTeam> teams) throws SQLException {
        eventTeamsDao.create(teams);
        return teams;
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
    
    public EventParticipantEntity addParticipant(@NotNull EventParticipantEntity participant) throws SQLException {
        eventParticipantsDao.create(participant);
        return participant;
    }
    
    public <T extends Collection<EventParticipantEntity>> T addEventParticipants(@NotNull T participants) throws SQLException {
        eventParticipantsDao.create(participants);
        return participants;
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
    
    public void rebuildPracticeMode() throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            
            // rebuild teams
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_teams (team_id, display_name, color, score)
                        SELECT
                            pt.team_id,
                            pt.display_name,
                            pt.color,
                            COALESCE(SUM(se.points_base * se.multiplier), 0)
                        FROM practice_teams pt
                        LEFT JOIN score_events se
                          ON se.team_id = pt.team_id
                         AND se.mode = 'practice'
                        GROUP BY pt.team_id, pt.display_name, pt.color
                    """);
            
            // rebuild participants
            activeTeamsDao.executeRaw("""
                        INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                        SELECT
                            pp.participant_uuid,
                            pp.team_id,
                            ap.ign,
                            COALESCE(SUM(se.points_base), 0)
                        FROM practice_participants pp
                        JOIN all_players ap
                          ON ap.uuid = pp.participant_uuid
                        LEFT JOIN score_events se
                          ON se.participant_uuid = pp.participant_uuid
                         AND se.mode = 'practice'
                        GROUP BY pp.participant_uuid, pp.team_id, ap.ign
                    """);
            
            return null;
        });
    }
    
    public void rebuildMaintenanceMode() throws SQLException {
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
                        FROM maintenance_teams mt
                        LEFT JOIN score_events se
                          ON se.team_id = mt.team_id
                         AND se.mode = 'maintenance'
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
                        FROM maintenance_participants mp
                        JOIN all_players ap
                          ON ap.uuid = mp.participant_uuid
                        LEFT JOIN score_events se
                          ON se.participant_uuid = mp.participant_uuid
                         AND se.mode = 'maintenance'
                        GROUP BY mp.participant_uuid, mp.team_id, ap.ign
                    """);
            
            return null;
        });
    }
    
    public void rebuildEventMode(@NotNull String eventId) throws SQLException {
        TransactionManager.callInTransaction(activeTeamsDao.getConnectionSource(), () -> {
            // clear
            activeTeamsDao.executeRaw("DELETE FROM active_participants");
            activeTeamsDao.executeRaw("DELETE FROM active_teams");
            
            // rebuild teams
            activeTeamsDao.executeRaw(
                    """
                                INSERT INTO active_teams (team_id, display_name, color, score)
                                SELECT
                                    et.team_id,
                                    et.display_name,
                                    et.color,
                                    COALESCE(SUM(se.points_base * se.multiplier), 0)
                                FROM event_teams et
                                LEFT JOIN score_events se
                                  ON se.team_id = et.team_id
                                 AND se.event_id = ?
                                WHERE et.event_id = ?
                                GROUP BY et.team_id, et.display_name, et.color
                            """,
                    eventId,
                    eventId
            );
            
            // rebuild participants
            activeTeamsDao.executeRaw(
                    """
                                INSERT INTO active_participants (participant_uuid, team_id, ign, score)
                                SELECT
                                    ep.participant_uuid,
                                    ep.team_id,
                                    ap.ign,
                                    COALESCE(SUM(se.points_base), 0)
                                FROM event_participants ep
                                JOIN all_players ap
                                  ON ap.uuid = ep.participant_uuid
                                LEFT JOIN score_events se
                                  ON se.participant_uuid = ep.participant_uuid
                                 AND se.event_id = ?
                                WHERE ep.event_id = ?
                                GROUP BY ep.participant_uuid, ep.team_id, ap.ign
                            """,
                    eventId, eventId);
            
            return null;
        });
    }
}
