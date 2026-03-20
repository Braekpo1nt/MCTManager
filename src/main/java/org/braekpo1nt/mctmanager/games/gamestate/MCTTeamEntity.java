package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.database.entities.teams.PracticeTeam;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class MCTTeamEntity {
    private String name;
    private String displayName;
    private int score;
    private String color;
    
    public MaintenanceTeam toMaintenance() {
        return MaintenanceTeam.builder()
                .teamId(name)
                .displayName(displayName)
                .color(color)
                .modifiedAt(new Date())
                .build();
    }
    
    public PracticeTeam toPractice() {
        return PracticeTeam.builder()
                .teamId(name)
                .displayName(displayName)
                .color(color)
                .modifiedAt(new Date())
                .build();
    }
}
