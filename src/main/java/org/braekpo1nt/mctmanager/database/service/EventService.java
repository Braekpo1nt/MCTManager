package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.EventInfoDto;
import org.braekpo1nt.mctmanager.database.exceptions.EventStillInUseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class EventService {
    
    private final @NotNull String mode;
    private final @NotNull Dao<EventInfoDto, String> eventInfoDao;
    
    public EventService(@NotNull String mode, @NotNull Database database) {
        this.mode = mode;
        this.eventInfoDao = database.getEventInfoDao();
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
}
