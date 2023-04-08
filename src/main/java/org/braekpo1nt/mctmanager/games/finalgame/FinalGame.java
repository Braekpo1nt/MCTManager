package org.braekpo1nt.mctmanager.games.finalgame;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class FinalGame implements MCTGame, Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final World finalGameWorld;
    private boolean gameActive = false;
    private List<Player> participants;
    private int finalGameCountDownTaskId;
    private Map<UUID, Integer> killCounts;

    public FinalGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.finalGameWorld = worldManager.getMVWorld("FT").getCBWorld();
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.killCounts = new HashMap<>(newParticipants.size());
        replaceSandGate();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setUpTeamOptions();
        startFinalGameCountDownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started final game");
    }

    private void initializeParticipant(Player participant) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        killCounts.put(participantUniqueId, 0);
        teleportParticipantToStartingPosition(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        resetHealthAndHunger(participant);
        clearStatusEffects(participant);
        initializeFastBoard(participant);
    }

    @Override
    public void stop() {
        cancelAllTasks();
        replaceSandGate();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping final game");
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
    
    private void startFinalGame() {
        dropSandGate();
        messageAllParticipants(Component.text("Go!")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN));
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

    private void startFinalGameCountDownTask() {
        this.finalGameCountDownTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startFinalGame();
                    this.cancel();
                    return;
                }
                messageAllParticipants(Component.text(count));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(finalGameCountDownTaskId);
    }

    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
}
