package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Preset {
    private List<PresetTeam> teams = new ArrayList<>();
    
    public int getTeamCount() {
        return teams.size();
    }
    
    /**
     * @return the number of unique members (participants) in the Preset
     */
    public int getParticipantCount() {
        int sum = 0;
        for (PresetTeam team : teams) {
            sum += team.getMembers().size();
        }
        return sum;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresetTeam {
        private String teamId;
        private String displayName;
        private String color;
        /**
         * the IGN (in-game-names) of this team's members
         */
        private List<String> members = new ArrayList<>();
    }
}
