package org.braekpo1nt.mctmanager.hub.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HubConfig {
    
    private World world;
    private Location spawn;
    private Location podium;
    private Location podiumObservation;
    private List<Leaderboard> leaderboards;
    private double yLimit;
    private int tpToHubDuration;
    private List<Material> preventInteractions;
    private @Nullable String initialState;
    private PracticeConfig practice;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PracticeConfig {
        private List<GameType> allowedGames;
        private Map<GameType, String> gameConfigs;
        /**
         * If null, no preset will be used. 
         */
        private @Nullable Preset preset;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Preset {
        private String file;
        private boolean override;
        private boolean resetScores;
        private boolean whitelist;
        /**
         * if true, players who are not whitelisted when transitioning to the practice mode
         * will be kicked (this kicking happens after the application of the preset)
         */
        private boolean kickUnWhitelisted;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Leaderboard {
        /**
         * the title of the leaderboard. Null will make the leaderboard have no title. 
         */
        private @Nullable String title;
        /**
         * the location the leaderboard should appear
         */
        private Location location;
        /**
         * how many players should be shown (i.e. top 10, top 20, top 5, etc.)
         */
        private int topPlayers = 10;
    }
    
}
