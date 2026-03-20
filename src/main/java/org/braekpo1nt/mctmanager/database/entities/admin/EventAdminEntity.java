package org.braekpo1nt.mctmanager.database.entities.admin;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "event_admins")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EventAdminEntity {
    /**
     * Generated unique id, for uniqueness only
     */
    @DatabaseField(generatedId = true)
    private int id;
    /**
     * the eventId of the event this participant is associated with.
     * Unique in combination with {@link #uuid}
     */
    @DatabaseField(canBeNull = false, columnName = "event_id")
    private @NotNull String eventId;
    /**
     * The UUID of the admin
     */
    @DatabaseField(canBeNull = false, columnName = "uuid")
    private @NotNull String uuid;
}
