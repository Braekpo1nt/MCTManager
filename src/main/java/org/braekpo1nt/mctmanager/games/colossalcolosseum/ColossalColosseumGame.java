package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcolosseum.config.ColossalColosseumStorageUtil;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ColossalColosseumGame implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ColossalColosseumStorageUtil storageUtil;
    private final String title = ChatColor.BLUE+"Colossal Colosseum";
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private List<ColossalColosseumRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private String firstTeamName;
    private String secondTeamName;
    private int roundDelayTaskId;
    private boolean gameIsActive = false;
    
    public ColossalColosseumGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new ColossalColosseumStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    /**
     * Start the game with the first and second place teams, and the spectators. 
     * @param newFirstPlaceParticipants The participants in the first place team
     * @param newSecondPlaceParticipants The participants in the second place team
     * @param newSpectators The participants who are third place and on, who should spectate the game
     */
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators) {
        firstTeamName = gameManager.getTeamName(newFirstPlaceParticipants.get(0).getUniqueId());
        secondTeamName = gameManager.getTeamName(newSecondPlaceParticipants.get(0).getUniqueId());
        firstPlaceRoundWins = 0;
        secondPlaceRoundWins = 0;
        closeFirstGate();
        closeSecondGate();
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        spectators = new ArrayList<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        int numOfRounds = (storageUtil.getRequiredWins() * 2) - 1;
        rounds = new ArrayList<>(numOfRounds);
        for (int i = 0; i < numOfRounds; i++) {
            rounds.add(new ColossalColosseumRound(plugin, gameManager, this, storageUtil));
        }
        currentRoundIndex = 0;
        for (Player first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Player second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Player spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        initializeSidebar();
        setupTeamOptions();
        startNextRound();
        gameIsActive = true;
        Bukkit.getLogger().info("Started Colossal Colosseum");
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.teleport(storageUtil.getFirstPlaceSpawn());
        first.setGameMode(GameMode.ADVENTURE);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.teleport(storageUtil.getSecondPlaceSpawn());
        second.setGameMode(GameMode.ADVENTURE);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        spectator.teleport(storageUtil.getSpectatorSpawn());
        spectator.setGameMode(GameMode.ADVENTURE);
    }
    
    private void startNextRound() {
        ColossalColosseumRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(firstPlaceParticipants, secondPlaceParticipants, spectators);
        gameManager.getSidebarManager().updateLine("round", String.format("Round: %s", currentRoundIndex+1));
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        updateRoundWinFastBoard();
        if (firstPlaceRoundWins >= storageUtil.getRequiredWins()) {
            stop(firstTeamName);
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 5*20L).getTaskId();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        updateRoundWinFastBoard();
        if (secondPlaceRoundWins >= storageUtil.getRequiredWins()) {
            stop(secondTeamName);
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 5*20L).getTaskId();
    }
    
    public void stop(@Nullable String winningTeam) {
        gameIsActive = false;
        cancelAllTasks();
        HandlerList.unregisterAll(this);
        if (currentRoundIndex < rounds.size()) {
            ColossalColosseumRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.stop();
            }
        }
        rounds.clear();
        for (Player participant : firstPlaceParticipants) {
            resetParticipant(participant);
        }
        firstPlaceParticipants.clear();
        for (Player participant : secondPlaceParticipants) {
            resetParticipant(participant);
        }
        secondPlaceParticipants.clear();
        for (Player participant : spectators) {
            resetParticipant(participant);
        }
        clearSidebar();
        spectators.clear();
        gameManager.getEventManager().colossalColosseumIsOver(winningTeam);
        Bukkit.getLogger().info("Stopping Colossal Colosseum");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundDelayTaskId);
    }
    
    public void onParticipantJoin(Player participant) {
        
    }
    
    public void onParticipantQuit(Player participant) {
        
    }
    
    @EventHandler
    public void onSpectatorGetDamaged(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!spectators.contains(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    private void updateRoundWinFastBoard() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        gameManager.getSidebarManager().updateLine("firstWinCount", String.format("%s: %s/3", firstDisplayName, firstPlaceRoundWins));
        gameManager.getSidebarManager().updateLine("secondWinCount", String.format("%s: %s/3", secondDisplayName, secondPlaceRoundWins));
    }
    
    private void initializeSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        gameManager.getSidebarManager().addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", String.format("%s: 0/3", firstDisplayName)),
                new KeyLine("secondWinCount", String.format("%s: 0/3", secondDisplayName)),
                new KeyLine("round", "Round: 1"),
                new KeyLine("timer", "")
        );
    }
    
    private void clearSidebar() {
        gameManager.getSidebarManager().deleteLines("title", "firstWinCount", "secondWinCount", "round", "timer");
    }
    
    private void closeFirstGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), -1002, -3, -19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(storageUtil.getWorld(), -1002, 1, -19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(secondTeamName);
        BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), -1002, 2, -19, 5, 4, 1, Material.AIR, teamPowderColor);
    }
    
    private void closeSecondGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), -1002, -3, 19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(storageUtil.getWorld(), -1002, 1, 19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(firstTeamName);
        BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), -1002, 2, 19, 5, 4, 1, Material.AIR, teamPowderColor);
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
    
    public boolean isActive() {
        return gameIsActive;
    }
}
