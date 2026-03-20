package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;

import java.util.UUID;

@Data
@Builder
public class MCTPlayerEntity {
    private UUID uniqueId;
    private String name;
    private int score;
    private String teamId;
    
    public MaintenanceParticipantEntity toMaintenance() {
        return MaintenanceParticipantEntity.builder()
                .participantUUID(uniqueId.toString())
                .teamId(teamId)
                .build();
    }
}
