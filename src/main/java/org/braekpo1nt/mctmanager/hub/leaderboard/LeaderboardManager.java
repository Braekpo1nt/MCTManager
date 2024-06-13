package org.braekpo1nt.mctmanager.hub.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.Bukkit;
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
    
    private static final String PREFIX = "leaderboard_";
    private static final String LEADERBOARD_ALL = "leaderboard_all";
    /**
     * The holograms for the participants. Each is specific to them because they need to see their personal scores.
     */
    private final Map<UUID, Hologram> holograms = new HashMap<>();
    /**
     * the hologram that others see (admins and non-participants)
     */
    private final Hologram allHologram;
    private final GameManager gameManager;
    private final @NotNull Location location;
    private final int topPlayers;
    
    /**
     * @param location the location that the leaderboard should appear. Must not be null. 
     */
    public LeaderboardManager(@NotNull GameManager gameManager, @NotNull Location location, int topPlayers) {
        this.gameManager = gameManager;
        this.location = location;
        this.topPlayers = topPlayers;
        this.allHologram = createHologram(LEADERBOARD_ALL);
        allHologram.setDefaultVisibleState(true);
    }
    
    /**
     * Removes all the holograms from the world and clears the list of players
     */
    public void tearDown() {
        for (Hologram hologram : holograms.values()) {
            DHAPI.removeHologram(hologram.getName());
        }
        holograms.clear();
        DHAPI.removeHologram(allHologram.getName());
    }
    
    public void onParticipantJoin(@NotNull Player participant) {
        Hologram hologram = createHologram(PREFIX + participant.getName());
        hologram.setShowPlayer(participant);
        holograms.put(participant.getUniqueId(), hologram);
        allHologram.setHidePlayer(participant);
    }
    
    public void onParticipantQuit(@NotNull Player participant) {
        Hologram hologram = holograms.remove(participant.getUniqueId());
        if (hologram != null) {
            DHAPI.removeHologram(hologram.getName());
        }
        allHologram.removeHidePlayer(participant);
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
        hologram.setDefaultVisibleState(false);
        return hologram;
    }
    
    public void updateScores() {
        List<OfflinePlayer> sortedOfflineParticipants = GameManagerUtils.getSortedOfflineParticipants(gameManager);
        List<Standing> standings = new ArrayList<>(sortedOfflineParticipants.size());
        for (int i = 0; i < sortedOfflineParticipants.size(); i++) {
            OfflinePlayer participant = sortedOfflineParticipants.get(i);
            int placement = i+1;
            UUID uuid = participant.getUniqueId();
            String name = participant.getName(); 
            if (name == null) {
                name = gameManager.getOfflineIGN(uuid);
                if (name == null) {
                    name = uuid.toString();
                }
            }
            String teamId = gameManager.getTeamName(uuid);
            ChatColor teamColor = gameManager.getTeamChatColor(teamId);
            int score = gameManager.getScore(uuid);
            standings.add(new Standing(uuid, placement, teamColor, name, score));
        }
        List<String> lines = new ArrayList<>(Math.min(topPlayers, standings.size())+1);
        for (int i = 0; i < Math.min(topPlayers, standings.size()); i++) {
            Standing standing = standings.get(i);
            lines.add(standing.toLine());
        }
        DHAPI.setHologramLines(allHologram, lines);
        lines.add("");
        for (Standing standing : standings) {
            Hologram hologram = holograms.get(standing.getUuid());
            if (hologram != null) {
                DHAPI.setHologramLines(hologram, lines);
                String personalLine = standing.toBoldLine();
                DHAPI.addHologramLine(hologram, personalLine);
                if (standing.getPlacement() - 1 <= topPlayers) {
                    DHAPI.setHologramLine(hologram, standing.getPlacement() - 1, personalLine);
                }
            }
        }
    }
    
    @Data
    static class Standing {
        private final @NotNull UUID uuid;
        private final int placement;
        private final ChatColor teamColor;
        private final @NotNull String ign;
        private final int score;
        
        public String toBoldLine() {
            return "" + ChatColor.GOLD + ChatColor.BOLD + placement + ". "
                    + teamColor + ChatColor.BOLD + ign
                    + ChatColor.WHITE + " - " + ChatColor.GOLD + ChatColor.BOLD + score;
        }
        
        public String toLine() {
            return "" + ChatColor.GOLD + placement + ". "
                    + teamColor + ign
                    + ChatColor.WHITE + " - " + ChatColor.GOLD + score;
        }
    }
}
