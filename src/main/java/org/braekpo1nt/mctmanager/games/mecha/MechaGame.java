package org.braekpo1nt.mctmanager.games.mecha;

import com.onarandombox.MultiverseCore.utils.AnchorManager;
import fr.mrmicky.fastboard.FastBoard;
import it.unimi.dsi.fastutil.Function;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MechaGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private List<Player> participants;
    private Map<UUID, FastBoard> boards;
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }


    @Override
    public void start(List<Player> participants) {
        this.participants = participants;
        initializeFastboards();
        Bukkit.getLogger().info("Started mecha");
    }

    @Override
    public void stop() {
        hideFastBoards();
        Bukkit.getLogger().info("Stopped mecha");
    }
    
    private void initializeFastboards() {
        for (Player participant : participants) {
            FastBoard board = new FastBoard(participant);
            board.updateTitle(ChatColor.BLUE+"MECHA");
            board.updateLines(
                    ChatColor.RED+"Kills: 0",
                    "",
                    ChatColor.DARK_PURPLE+"Boarder: 00:00",
                    ""
            );
            boards.put(participant.getUniqueId(), board);
        }
    }
    
    private void hideFastBoards() {
        for (FastBoard board : boards.values()) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
    }
    
    private void teleportPlayersToStartingPositions() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        Map<String, Location> teamLocations = new HashMap<>();
        teamLocations.put("orange", anchorManager.getAnchorLocation("mecha-orange"));
        teamLocations.put("yellow", anchorManager.getAnchorLocation("mecha-yellow"));
        teamLocations.put("green", anchorManager.getAnchorLocation("mecha-green"));
        teamLocations.put("dark-green", anchorManager.getAnchorLocation("mecha-dark-green"));
        teamLocations.put("cyan", anchorManager.getAnchorLocation("mecha-cyan"));
        teamLocations.put("blue", anchorManager.getAnchorLocation("mecha-blue"));
        teamLocations.put("purple", anchorManager.getAnchorLocation("mecha-purple"));
        teamLocations.put("red", anchorManager.getAnchorLocation("mecha-red"));
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location teamLocation = teamLocations.get(team);
            participant.teleport(teamLocation);
        }
    }
}
