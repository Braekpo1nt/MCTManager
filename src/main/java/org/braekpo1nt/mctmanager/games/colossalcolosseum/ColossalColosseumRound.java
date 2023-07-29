package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class ColossalColosseumRound {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final World colossalColosseumWorld;
    private final Location firstPlaceSpawn;
    private final Location secondPlaceSpawn;
    private Map<UUID, Boolean> participantsAlive = new HashMap<>();
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    
    public ColossalColosseumRound(Main plugin, GameManager gameManager, ColossalColosseumGame colossalColosseumGame) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.colossalColosseumWorld = worldManager.getMVWorld("FT").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.firstPlaceSpawn = anchorManager.getAnchorLocation("cc-first-place-spawn");
        this.secondPlaceSpawn = anchorManager.getAnchorLocation("cc-second-place-spawn");
    }
    
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators) {
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        participantsAlive = new HashMap<>();
        spectators = new ArrayList<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        closeGates();
        for (Player first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Player second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Player spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        setupTeamOptions();
        startRoundStartingCountDown();
        Bukkit.getLogger().info("Starting Colossal Colosseum round");
    }
    
}
