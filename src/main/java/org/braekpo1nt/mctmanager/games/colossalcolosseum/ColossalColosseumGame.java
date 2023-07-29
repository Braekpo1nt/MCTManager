package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
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

import java.util.ArrayList;
import java.util.List;

public class ColossalColosseumGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final String title = ChatColor.BLUE+"Colossal Colosseum";
    private final Location firstPlaceSpawn;
    private final Location secondPlaceSpawn;
    private final Location spectatorSpawn;
    private final World colossalColosseumWorld;
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private List<ColossalColosseumRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private final int MAX_ROUND_WINS = 3;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private String firstTeamName;
    private String secondTeamName;
    private int roundDelayTaskId;
    
    public ColossalColosseumGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.colossalColosseumWorld = worldManager.getMVWorld("FT").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.firstPlaceSpawn = anchorManager.getAnchorLocation("cc-first-place-spawn");
        this.secondPlaceSpawn = anchorManager.getAnchorLocation("cc-second-place-spawn");
        this.spectatorSpawn = anchorManager.getAnchorLocation("cc-spectator-spawn");
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.COLOSSAL_COLOSSEUM;
    }
    
    /**
     * Do not use this method. Instead, use {@link ColossalColosseumGame#start(List, List, List)}
     * @param newParticipants The participants
     */
    @Override
    public void start(List<Player> newParticipants) {
        throw new UnsupportedOperationException("ColossalColosseumGame is a special case, because it is the final game. Please use the overload method, start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators).");
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
        rounds = new ArrayList<>(3);
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
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
        setupTeamOptions();
        startNextRound();
        Bukkit.getLogger().info("Started Colossal Colosseum");
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        initializeFastBoard(first);
        first.teleport(firstPlaceSpawn);
        first.setGameMode(GameMode.ADVENTURE);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        initializeFastBoard(second);
        second.teleport(secondPlaceSpawn);
        second.setGameMode(GameMode.ADVENTURE);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        initializeFastBoard(spectator);
        spectator.teleport(spectatorSpawn);
        spectator.setGameMode(GameMode.ADVENTURE);
    }
    
    private void startNextRound() {
        ColossalColosseumRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(firstPlaceParticipants, secondPlaceParticipants, spectators);
        for (Player participant : firstPlaceParticipants) {
            updateRoundFastBoard(participant);
        }
        for (Player participant : secondPlaceParticipants) {
            updateRoundFastBoard(participant);
        }
        for (Player participant : spectators) {
            updateRoundFastBoard(participant);
        }
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        for (Player participant : firstPlaceParticipants) {
            updateRoundWinFastBoard(participant);
        }
        for (Player participant : secondPlaceParticipants) {
            updateRoundWinFastBoard(participant);
        }
        for (Player participant : spectators) {
            updateRoundWinFastBoard(participant);
        }
        if (firstPlaceRoundWins >= MAX_ROUND_WINS) {
            onTeamWinGame(firstTeamName);
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 5*20L).getTaskId();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        for (Player participant : firstPlaceParticipants) {
            updateRoundWinFastBoard(participant);
        }
        for (Player participant : secondPlaceParticipants) {
            updateRoundWinFastBoard(participant);
        }
        for (Player participant : spectators) {
            updateRoundWinFastBoard(participant);
        }
        if (secondPlaceRoundWins >= MAX_ROUND_WINS) {
            onTeamWinGame(secondTeamName);
            return;
        }
        currentRoundIndex++;
        this.roundDelayTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::startNextRound, 5*20L).getTaskId();
    }
    
    private void onTeamWinGame(String winningTeam) {
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Bukkit.getServer().sendMessage(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(winningTeam))
                .append(Component.text(" wins MCT #4!"))
                .color(teamColor)
                .decorate(TextDecoration.BOLD));
        gameManager.finalGameIsOver(winningTeam);
        Bukkit.getLogger().info(String.format("%s won Colossal Colosseum", winningTeam));
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        HandlerList.unregisterAll(this);
        if (currentRoundIndex < rounds.size()) {
            ColossalColosseumRound currentRound = rounds.get(currentRoundIndex);
            currentRound.stop();
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
        spectators.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Colossal Colosseum");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundDelayTaskId);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
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
    
    private void updateRoundWinFastBoard(Player participant) {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                2,
                String.format("%s: %s/3", firstDisplayName, firstPlaceRoundWins)
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                String.format("%s: %s/3", secondDisplayName, secondPlaceRoundWins)
        );
    }
    
    private void initializeFastBoard(Player participant) {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                String.format("%s: 0/3", firstDisplayName), // first team win count
                String.format("%s: 0/3", secondDisplayName), // second team win count
                "",
                "Round: 1", // Round: x
                "",
                "", //Starting:
                "" // time countdown
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void updateRoundFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                String.format("Round: %s", currentRoundIndex+1)
        );
    }
    
    private void closeFirstGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, -3, -19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, -19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(secondTeamName);
        BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, 2, -19, 5, 4, 1, Material.AIR, teamPowderColor);
    }
    
    private void closeSecondGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, -3, 19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, 19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(firstTeamName);
        BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, 2, 19, 5, 4, 1, Material.AIR, teamPowderColor);
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