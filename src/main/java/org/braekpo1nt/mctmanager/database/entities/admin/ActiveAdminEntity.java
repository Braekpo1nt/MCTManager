package org.braekpo1nt.mctmanager.database.entities.admin;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "active_admins")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActiveAdminEntity {
    /**
     * The UUID of the admin
     */
    @DatabaseField(id = true, columnName = "uuid")
    private @NotNull String uuid;
}
