package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@SuppressWarnings("UnusedReturnValue")
public class GameStateService {
    private final @NotNull String mode;
    private final @NotNull Dao<ActiveTeam, Integer> activeTeamsDao;
    private final @NotNull Dao<ActiveParticipant, Integer> activeParticipantsDao;
    
    public GameStateService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        
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
                                GROUP BY et.team_id, et.display_name, et.color
                                WHERE et.event_id = ?;
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
                                GROUP BY ep.participant_uuid, ep.team_id, ap.ign
                                WHERE ep.event_id = ?;
                            """,
                    eventId, eventId);
            
            return null;
        });
    }
}
