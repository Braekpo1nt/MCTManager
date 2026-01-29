package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "participant_data")
@NoArgsConstructor
@Data
public class ParticipantData {
    @DatabaseField(id = true)
    private @NotNull String uuid;
    @DatabaseField(canBeNull = false)
    private @NotNull String ign;
    @DatabaseField(canBeNull = false)
    private double percentRank;
    @DatabaseField(canBeNull = false)
    private double averageScore;
    @DatabaseField(canBeNull = false)
    private int totalEvents;
    @DatabaseField(canBeNull = false)
    private int current_tokens;
    @DatabaseField(canBeNull = false)
    private int lifetime_tokens;
}
