package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
}
