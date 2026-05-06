package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Preset {
    /**
     * Purely for debug output and command output convenience, not a reliable
     * source of usable file information
     */
    private final @NotNull String fileName;
    
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
        return teams.stream().anyMatch(t -> t.hasMember(ign));
    }
    
    /**
     * @param ign the in-game-name to search for the team of
     * @return The teamId that the given ign is a member of. null if the ign isn't on any team
     */
    public @Nullable String getMemberTeamId(@NotNull String ign) {
        PresetTeam presetTeam = teams.stream().filter(team -> team.hasMember(ign)).findFirst().orElse(null);
        if (presetTeam == null) {
            return null;
        }
        return presetTeam.getTeamId();
    }
    
    /**
     * leave the player from their team (if they are on one)
     * @param ign the in-game-name of the member to remove
     */
    public void leaveMember(@NotNull String ign) {
        teams.stream()
                .filter(t -> t.getMembers().stream()
                        .anyMatch(member -> member.getIgn().equals(ign))
                )
                .findFirst()
                .ifPresent(presetTeam -> presetTeam.removeMember(ign));
    }
    
    /**
     * @param ign the in-game-name of the member to join
     * @param teamId the teamId of the team to join the member to
     */
    public void joinMember(@NotNull String ign, @NotNull UUID uuid, @NotNull String teamId) {
        teams.stream()
                .filter(t -> t.getTeamId().equals(teamId))
                .findFirst()
                .ifPresent(
                        presetTeam -> presetTeam.getMembers()
                                .add(new PresetParticipant(ign, uuid))
                );
    }
    
    /**
     * @return the teamIds of the teams in this preset (in alphabetical order). Note this list is unmodifiable.
     */
    public @NotNull List<String> getTeamIds() {
        return teams.stream().map(PresetTeam::getTeamId).sorted().toList();
    }
    
    /**
     * @return a list of all the members in-game-names that are in the preset
     */
    public @NotNull List<PresetParticipant> getMembers() {
        return teams.stream().flatMap(t -> t.getMembers().stream()).toList();
    }
    
    @Data
    @AllArgsConstructor
    public static class PresetParticipant {
        private @NotNull String ign;
        private @NotNull UUID uuid;
    }
    
    @Data
    public static class PresetTeam {
        
        public PresetTeam(@NotNull String teamId, @NotNull String displayName, @NotNull String color) {
            this(teamId, displayName, color, new ArrayList<>());
        }
        
        public PresetTeam(@NotNull String teamId, @NotNull String displayName, @NotNull String color, @NotNull Collection<PresetParticipant> members) {
            this.teamId = teamId;
            this.displayName = displayName;
            this.color = color;
            this.members = new ArrayList<>(members);
        }
        
        private @NotNull String teamId;
        private @NotNull String displayName;
        private @NotNull String color;
        private @NotNull List<PresetParticipant> members;
        
        public boolean hasMember(@NotNull String ign) {
            return members.stream()
                    .anyMatch(member -> member.getIgn().equals(ign));
        }
        
        public boolean hasMember(@NotNull UUID uuid) {
            return members.stream()
                    .anyMatch(member -> member.getUuid().equals(uuid));
        }
        
        public void removeMember(@NotNull String ign) {
            members = members.stream()
                    // keep members whose ign is not the given ign
                    .filter(member -> !member.getIgn().equals(ign))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
