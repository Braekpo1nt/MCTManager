package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@DatabaseTable(tableName = "in_game_participants")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class InGameParticipant {
    /**
     * The UUID of the participant
     */
    @DatabaseField(id = true, columnName = "participant_uuid")
    private @NotNull String participantUUID;
    /**
     * the id of the game session this participant is participating in
     */
    @DatabaseField(columnName = "session_id")
    private @Nullable Integer gameSessionId;
    /**
     * The participant's score in the game
     */
    @DatabaseField(canBeNull = false, columnName = "game_score")
    private int gameScore;
    
    public static <P extends Participant> @NotNull InGameParticipant from(@NotNull P participant, int gameSessionId) {
        return InGameParticipant.builder()
                .participantUUID(participant.getUniqueId().toString())
                .gameSessionId(gameSessionId)
                .gameScore(participant.getScore())
                .build();
    }
    
    public static <P extends Participant> @NotNull List<InGameParticipant> from(@NotNull Collection<P> participants, int gameSessionId) {
        return participants.stream()
                .map(participant -> from(participant, gameSessionId))
                .toList();
    }
}
