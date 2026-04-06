package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
public class PresetDTO implements Validatable {
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
    
    Preset toPreset(@NotNull String fileName) {
        return new Preset(fileName, PresetTeamDTO.toPresetTeams(teams));
    }
    
    static PresetDTO fromPreset(@NotNull Preset preset) {
        return new PresetDTO(
                PresetTeamDTO.fromPresetTeams(preset.getTeams())
        );
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PresetParticipantDTO implements Validatable {
        /**
         * The in-game-name (ign) of this participant
         */
        private String ign;
        /**
         * The UUID of this participant
         */
        private UUID uuid;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(ign, "ign");
            validator.validate(!ign.isEmpty(), "ign can't be empty");
            validator.notNull(uuid, "uuid for %s", ign);
        }
        
        Preset.PresetParticipant toPresetParticipant() {
            return new Preset.PresetParticipant(ign, uuid);
        }
        
        static PresetParticipantDTO fromPresetParticipant(Preset.PresetParticipant presetParticipant) {
            return new PresetParticipantDTO(
                    presetParticipant.getIgn(),
                    presetParticipant.getUuid()
            );
        }
        
        static List<Preset.PresetParticipant> toPresetParticipants(List<PresetParticipantDTO> dtos) {
            return dtos.stream()
                    .map(PresetParticipantDTO::toPresetParticipant)
                    .toList();
        }
        
        static List<PresetParticipantDTO> fromPresetParticipants(List<Preset.PresetParticipant> entities) {
            return entities.stream()
                    .map(PresetParticipantDTO::fromPresetParticipant)
                    .toList();
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PresetTeamDTO implements Validatable {
        
        private String teamId;
        private String displayName;
        private String color;
        /**
         * this team's members
         */
        private List<PresetParticipantDTO> members;
        
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
            Set<String> uniqueIGNs = new HashSet<>(members.size());
            Set<UUID> uniqueUUIDs = new HashSet<>(members.size());
            for (int i = 0; i < members.size(); i++) {
                PresetParticipantDTO member = members.get(i);
                validator.notNull(member, "members[%d]");
                member.validate(validator.path("members[%s]"));
                validator.validate(!uniqueIGNs.contains(member.getIgn()),
                        "members[%d].ign is a duplicate: \"%s\"", i, member.getIgn());
                validator.validate(!uniqueUUIDs.contains(member.getUuid()),
                        "members[%d].uuid is a duplicate: \"%s\"", i, member.getUuid());
                uniqueIGNs.add(member.getIgn());
                uniqueUUIDs.add(member.getUuid());
            }
            
        }
        
        Preset.PresetTeam toPresetTeam() {
            return new Preset.PresetTeam(
                    teamId,
                    displayName,
                    color,
                    PresetParticipantDTO.toPresetParticipants(members)
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
                    PresetParticipantDTO.fromPresetParticipants(presetTeam.getMembers())
            );
        }
        
        static List<PresetTeamDTO> fromPresetTeams(List<Preset.PresetTeam> presetTeams) {
            return presetTeams.stream().map(PresetTeamDTO::fromPresetTeam).toList();
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresetConfigDTO implements Validatable {
        
        /**
         * The name of the preset file (located in MCTManager/presets/) to use
         */
        private String file;
        private Boolean override;
        private Boolean resetScores;
        /**
         * Add participants who are in the preset to the whitelist when applied
         */
        private Boolean whitelist;
        /**
         * Remove current participants from the whitelist before the whitelist
         * is applied
         */
        private Boolean unWhitelist;
        /**
         * if true, players who are not whitelisted when transitioning to the practice mode
         * will be kicked (this kicking happens after the application of the preset)
         */
        private Boolean kickUnWhitelisted;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(file, "file");
            validator.notNull(override, "override");
            validator.notNull(resetScores, "resetScores");
            validator.notNull(whitelist, "whitelist");
            validator.notNull(kickUnWhitelisted, "kickUnWhitelisted");
            validator.notNull(unWhitelist, "unWhitelist");
        }
        
        public PresetConfig toPreset() {
            return new PresetConfig(
                    file,
                    override,
                    resetScores,
                    whitelist,
                    unWhitelist,
                    kickUnWhitelisted
            );
        }
    }
}
