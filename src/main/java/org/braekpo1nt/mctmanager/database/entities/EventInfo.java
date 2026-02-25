package org.braekpo1nt.mctmanager.database.entities;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Data
@Builder
public class EventInfo {
    /**
     * Unique identifier for the event
     */
    private @NotNull String eventId;
    /**
     * The event display name for the web and discord
     */
    private @NotNull String plainTextName;
    /**
     * The event display name for sidebars and chat
     */
    private @NotNull Component componentName;
    /**
     * The date the event is/was scheduled to take place
     */
    private @NotNull Date eventDate;
    /**
     * The date the event was created and added to the database
     */
    private @NotNull Date createdAt;
    /**
     * The date the event was modified (same as the {@link #createdAt} if
     * it has never been modified
     */
    private @NotNull Date modifiedAt;
    /**
     * The date and time that the event was started (replaced each time the event is started)
     */
    private @Nullable Date startedAt;
    /**
     * The date and time that the event was ended (replaced each time the event is ended)
     */
    private @Nullable Date endedAt;
    /**
     * The winner of the event. Null if the event has no winner yet.
     */
    private @Nullable String winnerTeamId;
    /**
     * Tells the website when the standings have changed, reduces polling traffic.
     * Incremented when the scores change during an event
     */
    private boolean canonical;
    /**
     * Tells the website when the standings have changed, reduces polling traffic.
     * Incremented when the scores change during an event
     */
    private int standingsVersion;
}
