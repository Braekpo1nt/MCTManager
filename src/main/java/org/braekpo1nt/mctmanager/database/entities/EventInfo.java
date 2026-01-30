package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@DatabaseTable(tableName = "event_info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EventInfo {
    /**
     * Unique identifier for the event
     */
    @DatabaseField(id = true)
    private @NotNull String eventId;
    /**
     * The display name to be used in the web and discord formats
     */
    @DatabaseField(canBeNull = false)
    private @NotNull String plainTextName;
    /**
     * The display name to be used in {@link net.kyori.adventure.text.Component}s,
     * internally in Minecraft (a raw text json string)
     */
    @DatabaseField(canBeNull = false)
    private @NotNull String componentName;
    /**
     * The date the event was created and added to the database
     */
    @DatabaseField(canBeNull = false)
    private @NotNull Date createdDate;
    /**
     * The date the event was modified (same as the {@link #createdDate} if
     * it has never been modified
     */
    @DatabaseField(canBeNull = false)
    private @NotNull Date modifiedDate;
    /**
     * The date the event is/was scheduled to take place
     */
    @DatabaseField(canBeNull = false)
    private @NotNull Date eventDate;
    /**
     * The date and time that the event was started (replaced each time the event is started)
     */
    @DatabaseField
    private @Nullable Date startTime;
    /**
     * The date and time that the event was ended (replaced each time the event is ended)
     */
    @DatabaseField
    private @Nullable Date endTime;
}
