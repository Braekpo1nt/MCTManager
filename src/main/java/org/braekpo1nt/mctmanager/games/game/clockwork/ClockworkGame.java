package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkStorageUtil;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClockworkGame implements MCTGame, Configurable {
    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkStorageUtil storageUtil;
    private final String title = ChatColor.BLUE+"Clockwork";
    private List<Player> participants = new ArrayList<>();
    private List<ClockworkRound> rounds;
    private int currentRoundIndex = 0;
    private boolean gameActive = false;
    
    public ClockworkGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new ClockworkStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.CLOCKWORK;
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
            rounds.add(new ClockworkRound(plugin, gameManager, this, storageUtil, i+1));
        }
        currentRoundIndex = 0;
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setupTeamOptions();
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started clockwork");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        gameManager.getSidebarManager().addPlayer(participant);
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            ClockworkRound currentRound = rounds.get(currentRoundIndex);
            currentRound.stop();
        }
        rounds.clear();
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        gameManager.getSidebarManager().deleteAllLines();
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Clockwork");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    public void roundIsOver() {
        if (currentRoundIndex+1 >= rounds.size()) {
            stop();
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void startNextRound() {
        ClockworkRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
        updateRoundFastBoard();
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    private void cancelAllTasks() {
        
    }
    
    private void initializeSidebar() {
        gameManager.getSidebarManager().addLines(
                new KeyLine("team", ""),
                new KeyLine("points", ""),
                new KeyLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size())),
                new KeyLine("title", title),
                new KeyLine("playerCount", ""),
                new KeyLine("timer", "")
        );
    }
    
    private void updateScoreSidebar(Player participant) {
        UUID playerUUID = participant.getUniqueId();
        String teamName = gameManager.getTeamName(playerUUID);
        String teamDisplayName = gameManager.getTeamDisplayName(teamName);
        ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
        int teamScore = gameManager.getScore(teamName);
        int playerScore = gameManager.getScore(playerUUID);
        gameManager.getSidebarManager().updateLine(playerUUID, "team", String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        gameManager.getSidebarManager().updateLine(playerUUID, "points", String.format("%sPoints: %s", ChatColor.GOLD, playerScore));
    }
    
    private void updateRoundFastBoard() {
        gameManager.getSidebarManager().updateLine("round", String.format("Round %d/%d", currentRoundIndex+1, rounds.size()));
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
