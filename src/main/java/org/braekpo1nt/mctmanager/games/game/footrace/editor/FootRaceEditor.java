package org.braekpo1nt.mctmanager.games.game.footrace.editor;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class FootRaceEditor implements GameEditor, Configurable, Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private final FootRaceConfigController controller;
    private FootRaceConfig config;
    private Sidebar sidebar;
    private List<Player> participants;
    private Map<UUID, Display> displays;
    // wands
    // end wands
    /**
     * the checkpoint that the participant is editing
     */
    private Map<UUID, Integer> currentCheckpoints;
    private boolean editorStarted = false;
    
    public FootRaceEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.controller = new FootRaceConfigController(plugin.getDataFolder());
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        currentCheckpoints = new HashMap<>(newParticipants.size());
        displays = new HashMap<>(newParticipants.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player newParticipant : newParticipants) {
            initializeParticipant(newParticipant);
        }
        initializeSidebar();
        for (Player newParticipant : newParticipants) {
            selectCheckpoint(newParticipant, 0, false);
        }
        editorStarted = true;
        Bukkit.getLogger().info("Starting Foot Race editor");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        currentCheckpoints.put(participant.getUniqueId(), 0);
        displays.put(participant.getUniqueId(), new Display(plugin));
        sidebar.addPlayer(participant);
        participant.getInventory().clear();
        participant.teleport(config.getStartingLocation());
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        giveWands(participant);
    }
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public GameType getType() {
        return null;
    }
    
    @Override
    public boolean configIsValid() {
        return false;
    }
    
    @Override
    public void saveConfig() throws ConfigIOException, ConfigInvalidException {
        
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = controller.getConfig();
        if (!editorStarted) {
            return;
        }
        for (Player participant : participants) {
            selectCheckpoint(
                    participant,
                    currentCheckpoints.get(participant.getUniqueId()),
                    false
            );
        }
    }
}
