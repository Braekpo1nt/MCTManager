package org.braekpo1nt.mctmanager.hub.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetConfig;
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
    private PracticeConfig practice;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PracticeConfig {
        /**
         * If true, participants can't join an active game
         * unless their team already has members in it
         */
        private boolean restrictGameJoining;
        private List<GameType> allowedGames;
        private Map<GameType, String> gameConfigs;
        /**
         * If null, no preset will be used. 
         */
        private @Nullable PresetConfig preset;
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
