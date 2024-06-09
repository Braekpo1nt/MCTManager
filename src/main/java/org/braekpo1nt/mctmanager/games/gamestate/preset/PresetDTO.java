package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a preset GameState. Allows users to set up a GameState configuration 
 * with teams and members (by IGN) for use right before an event to get it to the 
 * state they want it in one little command instead of several commands. 
 * This contains all the necessary data to set up the teams and members. 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class PresetDTO implements Validatable {
    private List<PresetTeamDTO> teams = new ArrayList<>();
    
    @Override
    public void validate(@NotNull Validator validator) {
        if (teams != null) {
            validator.validateList(teams, "teams");
            Set<String> uniqueTeamIds = new HashSet<>(teams.size());
            for (int i = 0; i < teams.size(); i++) {
                String teamId = teams.get(i).getTeamId();
                validator.validate(!uniqueTeamIds.contains(teamId), "teams[%d] has a duplicate teamId \"%s\"", i, teamId);
                uniqueTeamIds.add(teamId);
            }
        }
    }
    
    Preset toPreset() {
        return new Preset(PresetTeamDTO.toPresetTeams(teams));
    }
    
    static PresetDTO fromPreset(@NotNull Preset preset) {
        return new PresetDTO(
                PresetTeamDTO.fromPresetTeams(preset.getTeams())
        );
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PresetTeamDTO implements Validatable {
        
        private String teamId;
        private String displayName;
        private String color;
        /**
         * the IGN (in-game-names) of this team's members
         */
        private List<String> members;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(teamId, "teamId");
            validator.notNull(!teamId.equals(GameManager.ADMIN_TEAM), "teamId can't be reserved id \"%s\"", GameManager.ADMIN_TEAM);
            validator.validate(teamId.matches(GameManagerUtils.TEAM_NAME_REGEX), "teamId must match regex \"%s\"", GameManagerUtils.TEAM_NAME_REGEX);
            validator.notNull(displayName, "displayName");
            validator.validate(!displayName.isEmpty(), "displayName can't be blank");
            validator.notNull(color, "color");
            validator.validate(ColorMap.hasNamedTextColor(color), "color is not a recognized color. It should be one of %s", ColorMap.getNamedTextColors());
            validator.notNull(members, "members");
            validator.validate(!members.contains(null), "members can't contain null entries");
            Set<String> uniqueMembers = new HashSet<>(members.size());
            for (int i = 0; i < members.size(); i++) {
                String member = members.get(i);
                validator.validate(!member.isEmpty(), "members[%d] can't be blank");
                validator.validate(!uniqueMembers.contains(member), "members[%d] is a duplicate of \"%s\"", i, member);
                uniqueMembers.add(member);
            }
            
        }
        
        Preset.PresetTeam toPresetTeam() {
            return new Preset.PresetTeam(
                    teamId, 
                    displayName, 
                    color, 
                    members
            );
        }
        
        static List<Preset.PresetTeam> toPresetTeams(List<PresetTeamDTO> presetTeamDTOS) {
            return presetTeamDTOS.stream().map(PresetTeamDTO::toPresetTeam).collect(Collectors.toCollection(ArrayList::new));
        }
        
        static PresetTeamDTO fromPresetTeam(Preset.PresetTeam presetTeam) {
            return new PresetTeamDTO(
                    presetTeam.getTeamId(), 
                    presetTeam.getDisplayName(),
                    presetTeam.getColor(),
                    presetTeam.getMembers()
            );
        }
        
        static List<PresetTeamDTO> fromPresetTeams(List<Preset.PresetTeam> presetTeams) {
            return presetTeams.stream().map(PresetTeamDTO::fromPresetTeam).toList();
        }
    } 
}
