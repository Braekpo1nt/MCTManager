package org.braekpo1nt.mctmanager.hub.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
        /**
         * The instance IDs of the allowed games to play during practice mode
         * (same as those of {@link #allowedGames}, but for fast and convenient reference
         */
        private Set<GameInstanceId> allowedGameIds;
        /**
         * The allowed games to play during practice  mode
         */
        private List<GameInfo> allowedGames;
        /**
         * If null, no preset will be used.
         */
        private @Nullable PresetConfig preset;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GameInfo {
        private @NotNull GameInstanceId id;
        private @NotNull Material itemType;
        private @NotNull Component itemName;
        private @NotNull List<Component> itemLore;
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
