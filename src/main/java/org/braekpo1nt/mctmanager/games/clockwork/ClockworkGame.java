package org.braekpo1nt.mctmanager.games.clockwork;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ClockworkGame implements MCTGame, Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private final World clockworkWorld;
    private final Location startingPosition;
    private List<Player> participants;
    private List<ClockworkRound> rounds;
    private int currentRoundIndex = 0;
    private boolean gameActive = false;
    private static final String title = ChatColor.BLUE+"Clockwork";
    private int roundDelayTaskId;

    public ClockworkGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld mvCaptureTheFlagWorld = worldManager.getMVWorld("FT");
        this.clockworkWorld = mvCaptureTheFlagWorld.getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        startingPosition = anchorManager.getAnchorLocation("clockwork");
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.CLOCKWORK;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        rounds = new ArrayList<>();
        rounds.add(new ClockworkRound(plugin, gameManager, this, startingPosition));
        rounds.add(new ClockworkRound(plugin, gameManager, this, startingPosition));
        rounds.add(new ClockworkRound(plugin, gameManager, this, startingPosition));
        currentRoundIndex = 0;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setUpTeamOptions();
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started Clockwork");
    }

    private void initializeParticipant(Player participant) {
        participants.add(participant);
        initializeFastBoard(participant);
    }

    @Override
    public void stop() {
        cancelAllTasks();
        HandlerList.unregisterAll(this);
        ClockworkRound currentRound = rounds.get(currentRoundIndex);
        currentRound.stop();
        rounds.clear();
        gameActive = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Clockwork");
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundDelayTaskId);
    }

    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
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
    
    private void startNextRound() {
        ClockworkRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(participants);
        for (Player participant : participants) {
            updateRoundFastBoard(participant);
        }
    }

    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
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
                "", // teams alive
                "", // number of teams alive
                "",
                "" // countdown
        );
    }
}
