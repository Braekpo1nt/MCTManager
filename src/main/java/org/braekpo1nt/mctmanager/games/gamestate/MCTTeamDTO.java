package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
class MCTTeamDTO implements Validatable {
    private String name;
    private String displayName;
    private int score;
    private String color;
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(name, "name");
        validator.validate(GameManagerUtils.validTeamName(name), "name must be a valid teamName matching the regex \"%s\"", GameManagerUtils.TEAM_NAME_REGEX);
        validator.validate(name.equals(GameManager.ADMIN_TEAM), "name can't be \"%s\"", GameManager.ADMIN_TEAM);
        validator.notNull(displayName, "displayName");
        validator.validate(!displayName.isEmpty(), "displayName can't be blank");
        validator.validate(score >= 0, "score can't be negative");
        validator.notNull(color, "color");
        validator.validate(ColorMap.hasNamedTextColor(color), "color is not a recognized color. It should be one of %s", ColorMap.getNamedTextColors());
    }
    
    MCTTeam toMCTTeam() {
        return MCTTeam.builder()
                .name(this.name)
                .displayName(this.displayName)
                .score(this.score)
                .color(this.color)
                .build();
    }
    
    static Map<String, MCTTeam> toMCTTeams(Map<String, MCTTeamDTO> teams) {
        Map<String, MCTTeam> mctTeams = new HashMap<>(teams.size());
        for (Map.Entry<String, MCTTeamDTO> entry : teams.entrySet()) {
            mctTeams.put(entry.getKey(), entry.getValue().toMCTTeam());
        }
        return mctTeams;
    }
    
    static MCTTeamDTO fromMCTTeam(MCTTeam mctTeam) {
        return MCTTeamDTO.builder()
                .name(mctTeam.getName())
                .displayName(mctTeam.getDisplayName())
                .score(mctTeam.getScore())
                .color(mctTeam.getColor())
                .build();
    }
    
    static Map<String, MCTTeamDTO> fromMCTTeams(Map<String, MCTTeam> teams) {
        Map<String, MCTTeamDTO> mctTeamDTOs = new HashMap<>(teams.size());
        for (Map.Entry<String, MCTTeam> entry : teams.entrySet()) {
            mctTeamDTOs.put(entry.getKey(), MCTTeamDTO.fromMCTTeam(entry.getValue()));
        }
        return mctTeamDTOs;
    }
    
}
