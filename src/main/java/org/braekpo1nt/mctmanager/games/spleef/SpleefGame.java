package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class SpleefGame implements MCTGame {
    private final Main plugin;
    private final GameManager gameManager;
    private final String title = ChatColor.BLUE+"Spleef";
    private final Location startingLocation;
    private List<Player> participants = new ArrayList<>();
    private List<SpleefRound> rounds;
    private int currentRoundIndex = 0;
    private boolean gameActive = false;
    private int roundDelayTaskId;
    
    public SpleefGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.startingLocation = anchorManager.getAnchorLocation("spleef");
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.SPLEEF;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        rounds = new ArrayList<>(3);
        rounds.add(new SpleefRound(plugin, gameManager, this, startingLocation));
        rounds.add(new SpleefRound(plugin, gameManager, this, startingLocation));
        rounds.add(new SpleefRound(plugin, gameManager, this, startingLocation));
        currentRoundIndex = 0;
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setupTeamOptions();
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started Spleef");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        initializeFastBoard(participant);
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            SpleefRound currentRound = rounds.get(currentRoundIndex);
            currentRound.stop();
        }
        rounds.clear();
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Spleef");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    public void roundIsOver() {
        if (currentRoundIndex+1 >= rounds.size()) {
            stop();
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 5*20L).getTaskId();
    }
    
    public void startNextRound() {
        SpleefRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
        for (Player participant : participants) {
            updateRoundFastBoard(participant);
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundDelayTaskId);
    }
    
    private void updateRoundFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                String.format("Round %d/%d", currentRoundIndex+1, rounds.size())
        );
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                String.format("Round %d/%d", currentRoundIndex+1, rounds.size()),
                "",
                "Starting in",
                ""
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
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
}
