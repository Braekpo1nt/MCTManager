package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public class ParkourPathwayEditor implements Configurable, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ParkourPathwayStorageUtil storageUtil;
    
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Display> displays;
    
    public ParkourPathwayEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        storageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        displays = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    public void initializeParticipant(Player participant) {
        participants.add(participant);
        participant.teleport(storageUtil.getStartingLocation());
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    private void resetParticipant(Player participant) {
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
}
