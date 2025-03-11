package org.braekpo1nt.mctmanager.hub.leaderboard;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages the leaderboard (creating, updating, showing/hiding)
 */
public class LeaderboardManager {
    
    /**
     * the prefix of the unique identifier of each participant's personal leaderboard
     * this is because hologram names are global in {@link DHAPI}, so they must have unique names. 
     */
    private final String PREFIX;
    /**
     * The holograms for the participants. Each is specific to them because they need to see their personal scores.
     */
    private final Map<UUID, Hologram> holograms = new HashMap<>();
    /**
     * the title of the leaderboard. If null, there will be no title.
     */
    private final @Nullable String title;
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
    public LeaderboardManager(@NotNull GameManager gameManager, @Nullable String title, @NotNull Location location, int topPlayers) {
        this.gameManager = gameManager;
        this.title = title;
        this.location = location;
        this.topPlayers = topPlayers;
        this.PREFIX = UUID.randomUUID() + "_leaderboard_";
        String allLeaderboardName = UUID.randomUUID() + "_leaderboard_all";
        this.allHologram = createHologram(allLeaderboardName);
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
        @NotNull List<OfflineParticipant> sortedOfflineParticipants = GameManagerUtils.getSortedOfflineParticipants(gameManager);
        List<Standing> standings = new ArrayList<>(sortedOfflineParticipants.size());
        for (int i = 0; i < sortedOfflineParticipants.size(); i++) {
            OfflineParticipant participant = sortedOfflineParticipants.get(i);
            int placement = i+1;
            standings.add(new Standing(participant.getUniqueId(), placement, participant.displayName(), participant.getScore()));
        }
        List<String> lines = new ArrayList<>(Math.min(topPlayers, standings.size())+(title != null ? 1 : 2));
        if (title != null) {
            lines.add(title);
        }
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
                if (standing.getPlacement() - (title != null ? 0 : 1) <= topPlayers) {
                    DHAPI.setHologramLine(hologram, standing.getPlacement() - (title != null ? 0 : 1), personalLine);
                }
            }
        }
    }
    
    @Data
    static class Standing {
        private final @NotNull UUID uuid;
        private final int placement;
        private final @NotNull Component displayName;
        private final int score;
        
        public String toBoldLine() {
            return LegacyComponentSerializer.legacyAmpersand().serialize(Component.empty()
                    .append(Component.text(placement))
                    .append(Component.text(". "))
                    .append(displayName)
                    .append(Component.text(" - "))
                    .append(Component.text(score))
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
            );
        }
        
        public String toLine() {
            return LegacyComponentSerializer.legacyAmpersand().serialize(Component.empty()
                    .append(Component.text(placement))
                    .append(Component.text(". "))
                    .append(displayName)
                    .append(Component.text(" - "))
                    .append(Component.text(score))
                    .color(NamedTextColor.GOLD)
            );
        }
    }
}
