package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.UpdateBuilder;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.EventInfoDto;
import org.braekpo1nt.mctmanager.database.entities.SystemState;
import org.braekpo1nt.mctmanager.database.entities.admin.EventAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.exceptions.EventStillInUseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class EventService {
    
    private final @NotNull String mode;
    private final @NotNull Dao<EventInfoDto, String> eventInfoDao;
    private final @NotNull Dao<EventAdminEntity, Integer> eventAdminDao;
    private final @NotNull Dao<EventTeam, Integer> eventTeamsDao;
    private final @NotNull Dao<EventParticipantEntity, Integer> eventParticipantsDao;
    
    private final @NotNull Dao<SystemState, Integer> systemStateDao;
    
    public EventService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.eventInfoDao = database.getEventInfoDao();
        this.eventAdminDao = database.getEventAdminDao();
        this.eventTeamsDao = database.getEventTeamsDao();
        this.eventParticipantsDao = database.getEventParticipantsDao();
        this.systemStateDao = database.getSystemStateDao();
    }
    
    /**
     * Deletes all entries in the EventService databases
     * @throws SQLException if there is an error clearing the databases
     */
    public boolean clearDatabase() throws SQLException {
        if (!mode.equals("test")) {
            return false;
        }
        eventInfoDao.deleteBuilder().delete();
        return true;
    }
    
    /**
     * @param eventInfo the eventInfo to create
     * @return true if the EventInfo object was created, false if an EventInfo with that eventId already exists in the database
     * @throws SQLException if there are any issues persisting to the database
     */
    public boolean addEventInfo(@NotNull EventInfo eventInfo) throws SQLException {
        return addEventInfo(EventInfoDto.from(eventInfo));
    }
    
    /**
     * @param eventInfo the eventInfo to create
     * @return true if the EventInfo object was created, false if an EventInfo with that eventId already exists in the database
     * @throws SQLException if there are any issues persisting to the database
     */
    public boolean addEventInfo(@NotNull EventInfoDto eventInfo) throws SQLException {
        return TransactionManager.callInTransaction(eventInfoDao.getConnectionSource(), () -> {
            if (eventInfoDao.idExists(eventInfo.getEventId())) {
                return false;
            }
            eventInfoDao.create(eventInfo);
            return true;
        });
    }
    
    /**
     * Delete the given {@link EventInfoDto} from the database
     * @param eventId the eventId of the {@link EventInfoDto} to delete
     * @return true if the deletion was successful, false if there was no {@link EventInfoDto}
     * found with the given ID
     */
    public boolean deleteEvent(String eventId) throws SQLException, EventStillInUseException {
        try {
            return TransactionManager.callInTransaction(eventInfoDao.getConnectionSource(), () -> {
                if (!eventInfoDao.idExists(eventId)) {
                    return false;
                }
                eventInfoDao.deleteById(eventId);
                return true;
            });
        } catch (SQLException e) {
            if (Database.containsForeignKeyViolation(e)) {
                throw new EventStillInUseException(eventId, e);
            }
            throw e;
        }
    }
    
    /**
     * @param eventId the eventId of the event to retrieve
     * @return the {@link EventInfoDto} with the given eventId, or null if no such event exists
     * @throws SQLException if there is an issue connecting to the database
     */
    public @Nullable EventInfoDto getEventInfoDto(@NotNull String eventId) throws SQLException {
        return eventInfoDao.queryForId(eventId);
    }
    
    /**
     * @param eventId the eventId of the event to retrieve
     * @return the {@link EventInfo} with the given eventId, or null if no such event exists
     * @throws SQLException if there is an issue connecting to the database
     */
    public @Nullable EventInfo getEventInfo(@NotNull String eventId) throws SQLException {
        EventInfoDto eventInfoDto = eventInfoDao.queryForId(eventId);
        if (eventInfoDto == null) {
            return null;
        }
        return eventInfoDto.to();
    }
    
    /**
     * @return a list of all eventIds in the database (empty list if there are none)
     * @throws SQLException if there are any issues communicating with the database
     */
    public @NotNull List<String> getEventIds() throws SQLException {
        try (GenericRawResults<String[]> raw =
                     eventInfoDao.queryRaw("SELECT id FROM event_info ORDER BY event_date DESC")) {
            return raw.getResults().stream()
                    .map(r -> r[0])
                    .toList();
        } catch (Exception e) {
            throw new SQLException("Exception thrown while getting eventIds from table");
        }
    }
    
    public void update(@NotNull EventInfo eventInfo) throws SQLException {
        eventInfo.setModifiedAt(new Date());
        eventInfoDao.update(EventInfoDto.from(eventInfo));
    }
    
    public @NotNull List<EventTeam> getTeams(@NotNull String eventId) throws SQLException {
        return eventTeamsDao.queryForEq("event_id", eventId);
    }
    
    public @NotNull List<EventParticipantEntity> getParticipants(@NotNull String eventId) throws SQLException {
        return eventParticipantsDao.queryForEq("event_id", eventId);
    }
    
    /**
     * Delete all teams and participants associated with the given eventId, and add all the given teams and
     * participants.
     * @param teams the teams to add
     * @param participants the participants to add
     * @param eventId the eventId
     * @throws SQLException if there is a database error
     * @throws IllegalArgumentException if not all the teams and participants share the same eventId as the input
     * eventId, or if the participants have a teamId that doesn't exist in the keys for teams
     */
    public void replaceEventTeamsAndParticipants(List<EventTeam> teams, List<EventParticipantEntity> participants, @NotNull String eventId) throws SQLException {
        Set<String> teamIds = teams.stream()
                .map(EventTeam::getTeamId)
                .collect(Collectors.toSet());
        for (EventTeam team : teams) {
            if (!Objects.equals(eventId, team.getEventId())) {
                throw new IllegalArgumentException(String.format("Team \"%s\" eventId \"%s\" doesn't match the input one (\"%s\")", team.getTeamId(), team.getEventId(), eventId));
            }
        }
        for (EventParticipantEntity participant : participants) {
            if (!teamIds.contains(participant.getTeamId())) {
                throw new IllegalArgumentException(String.format("Participant teamId \"%s\" doesn't exist in the given team ids", participant.getTeamId()));
            }
            if (!Objects.equals(eventId, participant.getEventId())) {
                throw new IllegalArgumentException(String.format("Participant \"%s\" eventId \"%s\" doesn't match the input one (\"%s\")", participant.getParticipantUUID(), participant.getEventId(), eventId));
            }
        }
        TransactionManager.callInTransaction(eventTeamsDao.getConnectionSource(), () -> {
            // order matters because of foreign keys
            // delete all the old participants and teams
            eventParticipantsDao.executeRaw("""
                            DELETE FROM event_participants
                            WHERE event_id = ?
                            """,
                    eventId
            );
            eventTeamsDao.executeRaw("""
                            DELETE FROM event_teams
                            WHERE event_id = ?
                            """,
                    eventId
            );
            // create the new teams
            eventTeamsDao.create(teams);
            // create the new participants
            eventParticipantsDao.create(participants);
            // remove admins who are now participants
            eventAdminDao.executeRaw("""
                            DELETE ea
                            FROM event_admins ea
                            JOIN event_participants ep
                              ON ea.uuid = ep.participant_uuid
                            WHERE ea.event_id = ?
                              AND ep.event_id = ?
                            """,
                    eventId,
                    eventId
            );
            return null;
        });
    }
    
    public void updateActiveEvent(@Nullable String eventId, int currentGameNumber, int maxGames) throws SQLException {
        UpdateBuilder<SystemState, Integer> updateBuilder = systemStateDao.updateBuilder();
        updateBuilder.where()
                .idEq(SystemState.ONLY_ID);
        updateBuilder
                .updateColumnValue("active_event_id", eventId)
                .updateColumnValue("current_game_number", currentGameNumber)
                .updateColumnValue("max_games", maxGames)
        ;
        updateBuilder.update();
    }
    
    public void setEventStartTime(@NotNull String eventId, @Nullable Date startedAt) throws SQLException {
        UpdateBuilder<EventInfoDto, String> updateBuilder = eventInfoDao.updateBuilder();
        updateBuilder.where()
                .idEq(eventId);
        updateBuilder
                .updateColumnValue("started_at", startedAt);
        updateBuilder.update();
    }
    
    public void setEventEndTime(@NotNull String eventId, @Nullable Date endedAt) throws SQLException {
        UpdateBuilder<EventInfoDto, String> updateBuilder = eventInfoDao.updateBuilder();
        updateBuilder.where()
                .idEq(eventId);
        updateBuilder
                .updateColumnValue("ended_at", endedAt);
        updateBuilder.update();
    }
    
    public void setEventWinner(@NotNull String eventId, @Nullable String winnerTeamId) throws SQLException {
        UpdateBuilder<EventInfoDto, String> updateBuilder = eventInfoDao.updateBuilder();
        updateBuilder.where()
                .idEq(eventId);
        updateBuilder
                .updateColumnValue("winner_team_id", winnerTeamId);
        updateBuilder.update();
    }
    
}
