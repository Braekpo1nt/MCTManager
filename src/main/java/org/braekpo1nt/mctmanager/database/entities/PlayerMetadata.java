package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds Metadata about all players, regardless of membership or participant status
 * Including wallet and balance, discord username, etc.
 */
@DatabaseTable(tableName = "player_metadata")
@NoArgsConstructor
@Data
public class PlayerMetadata {
    /**
     * Primary key, player unique id, must also be contained in all_players database
     */
    @DatabaseField(id = true, columnName = "participant_uuid")
    private @NotNull String participantUUID;
    /**
     * The username associated with the participant
     */
    @DatabaseField(columnName = "discord_username")
    private @Nullable String discordUsername;
    /**
     * The current number of challenger tokens
     */
    @DatabaseField(canBeNull = false, columnName = "current_tokens")
    private int currentTokens;
    /**
     * The number of challenger tokens earned by the player in their lifetime
     * (regardless of how many were spent by the player)
     */
    @DatabaseField(canBeNull = false, columnName = "lifetime_tokens")
    private int lifetimeTokens;
    /**
     *
     */
    @DatabaseField(canBeNull = false, columnName = "percent_rank")
    private double percentRank;
}
