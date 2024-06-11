package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    /**
     * @param teamId the teamId to check
     * @return true if a team with the given teamId exists in this preset
     */
    public boolean hasTeamId(@NotNull String teamId) {
        return teams.stream().anyMatch(t -> t.getTeamId().equals(teamId));
    }
    
    public void addTeam(@NotNull String teamId, @NotNull String displayName, @NotNull String color) {
        teams.add(new PresetTeam(teamId, displayName, color));
    }
    
    /**
     * removes the team with the given teamId from the teams list. This also leaves all the participants from that team. 
     * @param teamId the teamId to remove
     */
    public void removeTeam(@NotNull String teamId) {
        teams = teams.stream().filter(t -> !t.getTeamId().equals(teamId)).collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * @param ign the in-game-name to search for
     * @return true if the given ign is contained in any of the team members
     */
    public boolean hasMember(@NotNull String ign) {
        return teams.stream().anyMatch(t -> t.getMembers().contains(ign));
    }
    
    /**
     * @param ign the in-game-name to search for the team of
     * @return The teamId that the given ign is a member of. null if the ign isn't on any team
     */
    public @Nullable String getMemberTeamId(@NotNull String ign) {
        return teams.stream().filter(t -> t.getMembers().contains(ign)).findFirst().orElse(new PresetTeam()).getTeamId();
    }
    
    /**
     * leave the player from their team (if they are on one)
     * @param ign the in-game-name of the member to remove
     */
    public void leaveMember(@NotNull String ign) {
        teams.stream().filter(t -> t.getMembers().contains(ign)).findFirst().ifPresent(presetTeam -> presetTeam.getMembers().remove(ign));
    }
    
    /**
     * @param ign the in-game-name of the member to join
     * @param teamId the teamId of the team to join the member to
     */
    public void joinMember(@NotNull String ign, @NotNull String teamId) {
        teams.stream().filter(t -> t.getTeamId().equals(teamId)).findFirst().ifPresent(presetTeam -> presetTeam.getMembers().add(ign));
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresetTeam {
        
        public PresetTeam(@NotNull String teamId, @NotNull String displayName, @NotNull String color) {
            this.teamId = teamId;
            this.displayName = displayName;
            this.color = color;
        }
        
        private String teamId;
        private String displayName;
        private String color;
        /**
         * the IGN (in-game-names) of this team's members
         */
        private List<String> members = new ArrayList<>();
    }
}
