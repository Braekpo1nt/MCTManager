package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated in favor of {@link PlayerMetadata}
 */
@DatabaseTable(tableName = "participant_data")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@Deprecated
public class ParticipantData {
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
     *
     */
    @DatabaseField(canBeNull = false)
    private double percentRank;
    /**
     * The average final personal score across all played events
     */
    @DatabaseField(canBeNull = false)
    private double averageScore;
    /**
     * The total number of events the participant has played in
     */
    @DatabaseField(canBeNull = false)
    private int totalEvents;
    /**
     * The current number of challenger tokens
     */
    @DatabaseField(canBeNull = false)
    private int currentTokens;
    /**
     * The number of challenger tokens earned by the player in their lifetime
     * (regardless of how many were spent by the player)
     */
    @DatabaseField(canBeNull = false)
    private int lifetimeTokens;
}
