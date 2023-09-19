package org.braekpo1nt.mctmanager.games.game.spleef;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class SpleefGame implements MCTGame, Configurable {
    private final Main plugin;
    private final GameManager gameManager;
    private final SpleefStorageUtil storageUtil;
    private final String title = ChatColor.BLUE+"Spleef";
    private List<Player> participants = new ArrayList<>();
    private List<SpleefRound> rounds;
    private int currentRoundIndex = 0;
    private boolean gameActive = false;
    private int roundDelayTaskId;
    
    public SpleefGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new SpleefStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.SPLEEF;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        rounds = new ArrayList<>(storageUtil.getRounds());
        for (int i = 0; i < storageUtil.getRounds(); i++) {
            rounds.add(new SpleefRound(plugin, gameManager, this, storageUtil));
        }
        currentRoundIndex = 0;
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setupTeamOptions();
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started Spleef");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.stop();
            }
        }
        rounds.clear();
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Spleef");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
            return;
        }
        if (currentRoundIndex < rounds.size() && currentRoundIndex >= rounds.size() - 1) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            currentRound.onParticipantJoin(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        if (currentRoundIndex < rounds.size() && currentRoundIndex >= rounds.size() - 1) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            currentRound.onParticipantQuit(participant);
        }
    }
    
    public void roundIsOver() {
        if (currentRoundIndex+1 >= rounds.size()) {
            stop();
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, storageUtil.getRoundEndingDuration()*20L).getTaskId();
    }
    
    public void startNextRound() {
        SpleefRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
        gameManager.getSidebarManager().updateLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size()));
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundDelayTaskId);
    }
    
    private void initializeSidebar() {
        gameManager.getSidebarManager().addLines(
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size())),
                new KeyLine("timer", "")
        );
    }
    
    private void clearSidebar() {
        gameManager.getSidebarManager().deleteLines("title", "round", "timer");
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
}
