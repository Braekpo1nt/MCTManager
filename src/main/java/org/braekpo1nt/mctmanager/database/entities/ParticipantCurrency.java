package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a participant's currency, which is a collection of points earned
 * during practice mode and event mode. Currency is used to purchase cosmetics.
 */
@DatabaseTable(tableName = "participant_currency")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ParticipantCurrency {
    @DatabaseField(id = true)
    private @NotNull String uuid;
    @DatabaseField(canBeNull = false)
    private @NotNull String ign;
    @DatabaseField(canBeNull = false)
    private int current;
    @DatabaseField(canBeNull = false)
    private int lifetime;
}
