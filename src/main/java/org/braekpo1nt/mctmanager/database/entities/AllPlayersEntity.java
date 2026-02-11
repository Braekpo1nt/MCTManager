package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Source of truth about players who logged into the server, regardless of
 * membership or participant status
 */
@DatabaseTable(tableName = "all_players")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AllPlayersEntity {
    /**
     * Primary key, player unique id
     */
    @DatabaseField(id = true)
    private @NotNull String uuid;
    /**
     * The Minecraft IGN
     */
    @DatabaseField(canBeNull = false)
    private @NotNull String ign;
    /**
     * The first time this player logged in
     */
    @DatabaseField(columnName = "first_seen_at")
    private @NotNull Date firstSeenAt;
}
