package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * Used to tell the website which eventId to use as the active event
 */
@DatabaseTable(tableName = "system_state")
@NoArgsConstructor
@Data
public class SystemState {
    /**
     * The entity ID. Should be 1, since this has only one entry.
     */
    @DatabaseField(id = true)
    private int id;
    /**
     * The active eventId, or null if there is no active event
     */
    @DatabaseField(columnName = "active_event_id")
    private @Nullable String activeEventId;
}
