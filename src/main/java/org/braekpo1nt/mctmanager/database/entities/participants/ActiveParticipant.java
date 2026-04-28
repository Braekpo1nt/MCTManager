package org.braekpo1nt.mctmanager.database.entities.participants;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@DatabaseTable(tableName = "active_participants")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ActiveParticipant {
    /**
     * The UUID of the participant
     */
    @DatabaseField(id = true, columnName = "participant_uuid")
    private @NotNull String participantUUID;
    /**
     * The teamId this participant is a member of
     */
    @DatabaseField(canBeNull = false, columnName = "team_id")
    private @NotNull String teamId;
    /**
     * The Minecraft IGN
     */
    @DatabaseField(canBeNull = false, columnName = "ign")
    private @NotNull String ign;
    /**
     * The participant's score
     */
    @DatabaseField(canBeNull = false, columnName = "score")
    private int score;
    
    public static Map<UUID, MCTPlayerEntity> toPlayers(List<ActiveParticipant> activeParticipants) {
        return activeParticipants.stream()
                .map(ActiveParticipant::toPlayer)
                .collect(Collectors.toMap(MCTPlayerEntity::getUniqueId, Function.identity()));
    }
    
    public static MCTPlayerEntity toPlayer(ActiveParticipant activeParticipant) {
        return MCTPlayerEntity.builder()
                .uniqueId(UUID.fromString(activeParticipant.getParticipantUUID()))
                .name(activeParticipant.getIgn())
                .score(activeParticipant.getScore())
                .teamId(activeParticipant.getTeamId())
                .build();
    }
    
    public static List<ActiveParticipant> fromPlayers(Collection<MCTPlayerEntity> entities) {
        return entities.stream()
                .map(ActiveParticipant::fromPlayer)
                .toList();
    }
    
    @Contract("null -> null")
    public static ActiveParticipant fromPlayer(MCTPlayerEntity player) {
        if (player == null) {
            return null;
        }
        return builder()
                .participantUUID(player.getUniqueId().toString())
                .teamId(player.getTeamId())
                .ign(player.getName())
                .score(player.getScore())
                .build();
    }
}
