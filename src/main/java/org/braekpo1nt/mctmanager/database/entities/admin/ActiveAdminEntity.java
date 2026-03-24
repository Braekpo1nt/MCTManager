package org.braekpo1nt.mctmanager.database.entities.admin;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    public static List<UUID> toAdmins(List<ActiveAdminEntity> adminEntities) {
        return adminEntities.stream()
                .map(admin -> UUID.fromString(admin.getUuid()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
