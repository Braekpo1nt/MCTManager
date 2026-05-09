package org.braekpo1nt.mctmanager.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@DatabaseTable(tableName = "event_info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EventInfoDto {
    /**
     * Unique identifier for the event
     */
    @DatabaseField(id = true, columnName = "id")
    private @NotNull String eventId;
    /**
     * The display name to be used in the web and discord formats
     */
    @DatabaseField(canBeNull = false, columnName = "plain_display_name")
    private @NotNull String plainTextName;
    /**
     * The display name to be used in {@link net.kyori.adventure.text.Component}s,
     * internally in Minecraft (a raw text json string)
     */
    @DatabaseField(canBeNull = false, columnName = "component_display_name")
    private @NotNull String componentName;
    /**
     * The date the event is/was scheduled to take place
     */
    @DatabaseField(canBeNull = false, columnName = "event_date")
    private @NotNull Date eventDate;
    /**
     * The date the event was created and added to the database
     */
    @DatabaseField(canBeNull = false, columnName = "created_at")
    private @NotNull Date createdAt;
    /**
     * The date the event was modified (same as the {@link #createdAt} if
     * it has never been modified
     */
    @DatabaseField(canBeNull = false, columnName = "modified_at")
    private @NotNull Date modifiedAt;
    /**
     * The date and time that the event was started (replaced each time the event is started)
     */
    @DatabaseField(columnName = "started_at")
    private @Nullable Date startedAt;
    /**
     * The date and time that the event was ended (replaced each time the event is ended)
     */
    @DatabaseField(columnName = "ended_at")
    private @Nullable Date endedAt;
    
    /**
     * The winner of the event. Null if the event has no winner yet.
     */
    @DatabaseField(columnName = "winner_team_id")
    private @Nullable String winnerTeamId;
    /**
     * Whether this event was canonical and points should be applied
     */
    @DatabaseField(canBeNull = false, columnName = "canonical")
    private boolean canonical;
    /**
     * Tells the website when the standings have changed, reduces polling traffic.
     * Incremented when the scores change during an event
     */
    @DatabaseField(columnName = "standings_version", canBeNull = false)
    private int standingsVersion;
    
    public static EventInfoDto from(EventInfo e) {
        return EventInfoDto.builder()
                .eventId(e.getEventId())
                .plainTextName(e.getPlainTextName())
                .componentName(GsonComponentSerializer.gson().serialize(e.getComponentName()))
                .eventDate(e.getEventDate())
                .createdAt(e.getCreatedAt())
                .modifiedAt(e.getModifiedAt())
                .startedAt(e.getStartedAt())
                .endedAt(e.getEndedAt())
                .winnerTeamId(e.getWinnerTeamId())
                .canonical(e.isCanonical())
                .standingsVersion(e.getStandingsVersion())
                .build();
    }
    
    public @NotNull EventInfo to() {
        return EventInfo.builder()
                .eventId(this.getEventId())
                .plainTextName(this.getPlainTextName())
                .componentName(GsonComponentSerializer.gson().deserialize(this.getComponentName()))
                .eventDate(this.getEventDate())
                .createdAt(this.getCreatedAt())
                .modifiedAt(this.getModifiedAt())
                .startedAt(this.getStartedAt())
                .endedAt(this.getEndedAt())
                .winnerTeamId(this.getWinnerTeamId())
                .canonical(this.isCanonical())
                .standingsVersion(this.getStandingsVersion())
                .build();
    }
}
