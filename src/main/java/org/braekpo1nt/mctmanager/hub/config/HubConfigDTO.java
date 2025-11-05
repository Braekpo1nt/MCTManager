package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetDTO;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
class HubConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private LocationDTO spawn;
    private LocationDTO podium;
    private LocationDTO podiumObservation;
    private PracticeDTO practice;
    private List<LeaderboardDTO> leaderboards;
    private List<Material> preventInteractions;
    private double yLimit;
    private Durations durations;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(this.world, "world");
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
        validator.notNull(this.spawn, "spawn");
        validator.notNull(this.podium, "podium");
        validator.notNull(practice, "practice");
        practice.validate(validator.path("practice"));
        if (leaderboards != null) {
            validator.validateList(leaderboards, "leaderboards");
        }
        validator.validate(this.yLimit < this.spawn.getY(), "yLimit (%s) must be less than spawn.y (%s)", this.yLimit, this.spawn.getY());
        validator.validate(this.yLimit < this.podium.getY(), "yLimit (%s) must be less than podium.y (%s)", this.yLimit, this.podium.getY());
        validator.validate(this.yLimit < this.podiumObservation.getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", this.yLimit, this.podiumObservation.getY());
        validator.validate(this.yLimit < this.podiumObservation.getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", this.yLimit, this.podiumObservation.getY());
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.tpToHub() > 0, "durations.tpToHub must be greater than 0");
        
    }
    
    HubConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        return HubConfig.builder()
                .world(newWorld)
                .spawn(this.spawn.toLocation(newWorld))
                .podium(this.podium.toLocation(newWorld))
                .podiumObservation(this.podiumObservation.toLocation(newWorld))
                .leaderboards(LeaderboardDTO.toLeaderboards(this.leaderboards, newWorld))
                .yLimit(this.yLimit)
                .tpToHubDuration(this.durations.tpToHub)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .practice(this.practice.toPractice())
                .build();
    }
    
    record Durations(int tpToHub) {
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class PracticeDTO implements Validatable {
        
        /**
         * If true, participants can't join an active game
         * unless their team already has members in it.
         * Defaults to false.
         */
        private Boolean restrictGameJoining;
        private List<GameInfoDTO> allowedGames;
        private @Nullable PresetDTO.PresetConfigDTO preset;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(allowedGames, "allowedGames");
            validator.validateList(allowedGames, "allowedGames");
            Set<GameInstanceId> usedIds = new HashSet<>(allowedGames.size());
            for (GameInfoDTO gameInfoDTO : allowedGames) {
                GameInstanceId id = new GameInstanceId(gameInfoDTO.getGameType(), gameInfoDTO.getConfigFile());
                validator.validate(!usedIds.contains(id), "allowedGames has duplicate game/config combo: \"%s\" and \"%s\"", id.getGameType(), id.getConfigFile());
                usedIds.add(id);
            }
            if (preset != null) {
                preset.validate(validator.path("preset"));
            }
        }
        
        public HubConfig.PracticeConfig toPractice() {
            List<HubConfig.GameInfo> games = toAllowedGames();
            return HubConfig.PracticeConfig.builder()
                    .restrictGameJoining(restrictGameJoining != null ? restrictGameJoining : false)
                    .allowedGameIds(games.stream()
                            .map(HubConfig.GameInfo::getId)
                            .collect(Collectors.toSet()))
                    .allowedGames(games)
                    .preset(preset != null ? preset.toPreset() : null)
                    .build();
        }
        
        private List<HubConfig.GameInfo> toAllowedGames() {
            List<HubConfig.GameInfo> result = new ArrayList<>(allowedGames.size());
            for (GameInfoDTO gameInfoDTO : allowedGames) {
                HubConfig.GameInfo gameInfo = gameInfoDTO.toGameInfo();
                result.add(gameInfo);
            }
            return result;
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class GameInfoDTO implements Validatable {
        private GameType gameType;
        private String configFile;
        private Material itemType;
        private Component itemName;
        private List<Component> itemLore;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(gameType, "gameType");
            validator.notNull(configFile, "configFile");
            validator.notNull(itemType, "itemType");
            validator.notNull(itemName, "itemName");
            validator.notNull(itemLore, "itemLore");
            validator.validate(!itemLore.contains(null), "itemLore can't have null entries");
        }
        
        public HubConfig.GameInfo toGameInfo() {
            return HubConfig.GameInfo.builder()
                    .id(new GameInstanceId(gameType, configFile))
                    .itemType(itemType)
                    .itemName(itemName)
                    .itemLore(itemLore)
                    .build();
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class LeaderboardDTO implements Validatable {
        /**
         * the title of the leaderboard. Null will make the leaderboard have no title.
         */
        private @Nullable String title;
        /**
         * the location the leaderboard should appear
         */
        private LocationDTO location;
        /**
         * how many players should be shown (i.e. top 10, top 20, top 5, etc.)
         */
        private int topPlayers = 10;
        
        @Override
        public void validate(@NotNull Validator validator) {
            // location can be null if the external HubConfigDTO#leaderBoard Vector is not null, so that validation is left up to external validation
            validator.validate(topPlayers >= 0, "topNumber can't be negative");
        }
        
        HubConfig.Leaderboard toLeaderboard(@NotNull World world) {
            return new HubConfig.Leaderboard(
                    title,
                    location.toLocation(world),
                    topPlayers
            );
        }
        
        static List<HubConfig.Leaderboard> toLeaderboards(@Nullable List<LeaderboardDTO> leaderboardDTOS, @NotNull World world) {
            if (leaderboardDTOS == null) {
                return Collections.emptyList();
            }
            return leaderboardDTOS.stream().map(l -> l.toLeaderboard(world)).toList();
        }
        
    }
}
