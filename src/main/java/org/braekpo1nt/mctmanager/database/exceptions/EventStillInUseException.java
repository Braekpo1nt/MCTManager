package org.braekpo1nt.mctmanager.database.exceptions;

public class EventStillInUseException extends Exception {
    public EventStillInUseException(String eventId, Throwable cause) {
        super(String.format("The event with id \"%s\" is still in use.", eventId), cause);
    }
}
