package org.braekpo1nt.mctmanager.games.gamestate.preset.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetDTO;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a preset GameState. Allows users to set up a GameState configuration
 * with teams and members (by IGN) for use right before an event to get it to the
 * state they want it in one little command instead of several commands.
 * This contains all the necessary data to set up the teams and members.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegacyPresetDTO implements Validatable {
    private List<LegacyPresetTeamDTO> teams = new ArrayList<>();
    
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
    @AllArgsConstructor
    @NoArgsConstructor
    static class LegacyPresetTeamDTO implements Validatable {
        
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
            validator.validate(GameManagerUtils.validTeamId(teamId), "teamId must match regex \"%s\"", GameManagerUtils.TEAM_NAME_REGEX);
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
        
        public PresetDTO.PresetTeamDTO convert(@NotNull Server server) throws ConfigException {
            List<PresetDTO.PresetParticipantDTO> memberDtos = new ArrayList<>(members.size());
            List<String> unFindableIGNs = new ArrayList<>();
            for (String ign : members) {
                UUID uuid = server.getPlayerUniqueId(ign);
                if (uuid == null) {
                    // add to list and fail out later for a full list of IGNs that couldn't be resolved
                    unFindableIGNs.add(ign);
                }
                memberDtos.add(PresetDTO.PresetParticipantDTO.builder()
                        .ign(ign)
                        .uuid(uuid)
                        .build());
            }
            if (!unFindableIGNs.isEmpty()) {
                throw new ConfigException(String.format("Could not find a UUID for the following IGNs: %s", unFindableIGNs));
            }
            memberDtos = members.stream()
                    .map(ign -> {
                        UUID uuid = server.getPlayerUniqueId(ign);
                        if (uuid == null) {
                            throw new ConfigException(String.format("Could not find a UUID for the IGN \"%s\"", ign));
                        }
                        return PresetDTO.PresetParticipantDTO.builder()
                                .ign(ign)
                                .uuid(uuid)
                                .build();
                    })
                    .toList();
            return PresetDTO.PresetTeamDTO.builder()
                    .teamId(teamId)
                    .displayName(displayName)
                    .color(color)
                    .members(memberDtos)
                    .build();
        }
        
    }
    
    public PresetDTO convert(@NotNull Server server) throws ConfigException {
        List<PresetDTO.PresetTeamDTO> teams = this.teams.stream()
                .map(dto -> dto.convert(server))
                .toList();
        return new PresetDTO(teams);
    }
}
