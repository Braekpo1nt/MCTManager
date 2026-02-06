package org.braekpo1nt.mctmanager.database.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class EventService {
    
    private final @NotNull String mode;
    private final @NotNull Dao<EventInfo, String> eventInfoDao;
    
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
        return TransactionManager.callInTransaction(eventInfoDao.getConnectionSource(), () -> {
            if (eventInfoDao.idExists(eventInfo.getEventId())) {
                return false;
            }
            eventInfoDao.create(eventInfo);
            return true;
        });
    }
    
    /**
     * Delete the given {@link EventInfo} from the database
     * @param eventId the eventId of the {@link EventInfo} to delete
     * @return true if the deletion was successful, false if there was no {@link EventInfo}
     * found with the given ID
     */
    public boolean deleteEvent(String eventId) throws SQLException {
        return TransactionManager.callInTransaction(eventInfoDao.getConnectionSource(), () -> {
            if (!eventInfoDao.idExists(eventId)) {
                return false;
            }
            eventInfoDao.deleteById(eventId);
            return true;
        });
    }
}
