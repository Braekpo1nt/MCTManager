package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
class PresetDTO implements Validatable {
    private List<PresetTeamDTO> teams;
    
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
    
    @Data
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
    } 
}
