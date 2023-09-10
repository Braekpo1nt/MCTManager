package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ClockworkRound implements Listener {
    
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final ClockworkStorageUtil storageUtil;
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    private boolean roundActive = false;
    
    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, ClockworkStorageUtil storageUtil) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.storageUtil = storageUtil;
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.participantsAreAlive = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setupTeamOptions();
        startBreatherDelay();
        roundActive = true;
        Bukkit.getLogger().info("Starting Clockwork Round");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participantsAreAlive.put(participant.getUniqueId(), true);
        initializeFastBoard(participant);
        participant.teleport(storageUtil.getStartingLocation());
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void roundIsOver() {
        stop();
        clockworkGame.roundIsOver();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        roundActive = false;
        Bukkit.getLogger().info("Stopping Clockwork round");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        hideFastBoard(participant);
    }
    
    private void cancelAllTasks() {
        
    }
    
    private void startBreatherDelay() {
        
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                ""
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                ""
        );
    }
    
    private void initializeAliveCountFastBoard(Player participant, String countLine) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                countLine
        );
    }
    
    private void updateAliveCountFastBoard(Player participant, String countLine) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                countLine
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
}
