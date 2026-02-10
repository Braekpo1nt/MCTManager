package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@DatabaseTable(tableName = "all_players")
@NoArgsConstructor
@Data
public class AllPlayersEntity {
    @DatabaseField(id = true)
    private @NotNull String uuid;
    @DatabaseField(canBeNull = false)
    private @NotNull String ign;
    @DatabaseField(columnName = "discord_username")
    private @Nullable String discordUsername;
    @DatabaseField(columnName = "first_seen_at")
    private @NotNull Date firstSeenAt;
}
