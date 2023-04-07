package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.structure.Structure;

import java.util.*;

public class SpleefGame implements MCTGame, Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private List<Player> participants;
    private final World spleefWorld;
    private Map<UUID, Boolean> participantsAlive;
    private boolean gameActive = false;
    private Location spleefStartAnchor;

    public SpleefGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.spleefWorld = worldManager.getMVWorld("FT").getCBWorld();
    }

    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>();
        participantsAlive = new HashMap<>();
        placeLayers();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        gameActive = true;
    }

    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        participantsAlive.put(participantUniqueId, true);
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.spleefStartAnchor = anchorManager.getAnchorLocation("spleef");
    }

    @Override
    public void stop() {

    }

    @Override
    public void onParticipantJoin(Player participant) {

    }

    @Override
    public void onParticipantQuit(Player participant) {

    }
    
    private void placeLayers() {
        Structure layer1 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer1"));
        Structure layer2 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer2"));
        Structure layer3 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer3"));
        Structure layer4 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer4"));

        layer1.place(new Location(spleefWorld, -15, 21, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer2.place(new Location(spleefWorld, -15, 16, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer3.place(new Location(spleefWorld, -15, 11, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer4.place(new Location(spleefWorld, -15, 6, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
}
