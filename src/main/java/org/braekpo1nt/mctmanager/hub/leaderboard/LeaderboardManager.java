package org.braekpo1nt.mctmanager.hub.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages the leaderboard (creating, updating, showing/hiding)
 */
public class LeaderboardManager {
    
    private final Map<UUID, Hologram> holograms = new HashMap<>();
    private static final String PREFIX = "leaderboard_";
    private final GameManager gameManager;
    private @NotNull Location location;
    private int topPlayers;
    
    /**
     * @param location the location that the leaderboard should appear. Must not be null. 
     */
    public LeaderboardManager(@NotNull GameManager gameManager, @NotNull Location location, int topPlayers) {
        this.gameManager = gameManager;
        this.location = location;
        this.topPlayers = topPlayers;
    }
    
    /**
     * Set the location of the leaderboard. Updates any existing holograms. 
     * @param location the location of the leaderboard 
     */
    public void setLocation(@NotNull Location location) {
        this.location = location;
        for (Hologram hologram : holograms.values()) {
            DHAPI.moveHologram(hologram, location);
        }
    }
    
    public void setTopPlayers(int topPlayers) {
        this.topPlayers = topPlayers;
        updateScores();
    }
    
    public void onParticipantJoin(@NotNull Player participant) {
        Hologram hologram = createHologram(PREFIX + participant.getName());
        hologram.setShowPlayer(participant);
        holograms.put(participant.getUniqueId(), hologram);
    }
    
    public void onParticipantQuit(@NotNull Player participant) {
        Hologram hologram = holograms.remove(participant.getUniqueId());
        if (hologram != null) {
            DHAPI.removeHologram(hologram.getName());
        }
    }
    
    /**
     * Creates a new hologram or retrieves the one already created if one with the same name exists
     * @param name the name of the hologram (an ID)
     * @return the created hologram with the given name and location
     */
    private Hologram createHologram(String name) {
        Hologram hologram = DHAPI.getHologram(name);
        if (hologram == null) {
            hologram = DHAPI.createHologram(name, location);
        } else {
            DHAPI.moveHologram(hologram, location);
        }
        DHAPI.setHologramLines(hologram, List.of("Test"));
        hologram.setDefaultVisibleState(false);
        return hologram;
    }
    
    public void updateScores() {
        // and a highlight of their position if they're in the top 10
        List<OfflinePlayer> sortedOfflinePlayers = GameManagerUtils.getSortedOfflinePlayers(gameManager);
        List<Standing> standings = new ArrayList<>(sortedOfflinePlayers.size());
        for (int i = 0; i < Math.min(topPlayers, sortedOfflinePlayers.size()); i++) {
            OfflinePlayer participant = sortedOfflinePlayers.get(i);
            int placement = i+1;
            String displayName = gameManager.getDisplayNameAsString(participant);
            int score = gameManager.getScore(participant.getUniqueId());
            standings.add(new Standing(participant.getUniqueId(), placement, displayName, score));
        }
        List<String> lines = new ArrayList<>(Math.min(topPlayers, standings.size())+1);
        for (int i = 0; i < Math.min(topPlayers, standings.size()); i++) {
            Standing standing = standings.get(i);
            lines.add(standing.toLine());
        }
        lines.add("");
        for (Standing standing : standings) {
            Hologram hologram = holograms.get(standing.getUuid());
            if (hologram != null) {
                DHAPI.setHologramLines(hologram, lines);
                String personalLine = standing.toBoldLine();
                DHAPI.addHologramLine(hologram, personalLine);
            }
        }
    }
    
    @Data
    static class Standing {
        private final @NotNull UUID uuid;
        private final int placement;
        private final @NotNull String displayName;
        private final int score;
        
        public String toBoldLine() {
            return String.format("%s%s%d.%s %s%s - %s%d", ChatColor.BOLD, ChatColor.GOLD, placement, ChatColor.WHITE, displayName, ChatColor.WHITE, ChatColor.GOLD, score);
        }
        public String toLine() {
            return String.format("%s%d.%s %s%s - %s%d", ChatColor.GOLD, placement, ChatColor.WHITE, displayName, ChatColor.WHITE, ChatColor.GOLD, score);
        }
    }
}
