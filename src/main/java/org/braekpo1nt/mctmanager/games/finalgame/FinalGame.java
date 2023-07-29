package org.braekpo1nt.mctmanager.games.finalgame;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

@Deprecated
public class FinalGame implements MCTGame, Listener {

    private static final int MAX_KILLS = 9;
    private final Main plugin;
    private final GameManager gameManager;
    private final World finalGameWorld;
    private final String title = ChatColor.BLUE+"Final Game";
    private boolean gameActive = false;
    private List<Player> participants;
    private int finalGameCountDownTaskId;
    private Map<String, Integer> teamKillCounts;
    private Map<String, Location> teamSpawns;
    private String teamA;
    private String teamB;

    public FinalGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.finalGameWorld = worldManager.getMVWorld("FT").getCBWorld();
    }
    
    @Override
    public MCTGames getType() {
        return null;
    }
    
    public void teleportViewers(List<Player> otherParticipants) {
        Location viewLocation = new Location(finalGameWorld, -999, 9, 20, 179, 20);
        for (Player participant : otherParticipants) {
            participant.teleport(viewLocation);
            participant.setBedSpawnLocation(viewLocation);
            participant.getInventory().clear();
            participant.setGameMode(GameMode.ADVENTURE);
        }
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        this.teamKillCounts = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setUpTeams(newParticipants);
        setUpTeamSpawns();
        replaceSandGateA(teamA);
        replaceSandGateB(teamB);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setUpTeamOptions();
        startFinalGameCountDownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started final game");
    }

    private void setUpTeams(List<Player> newParticipants) {
        List<String> teams = new ArrayList<>();
        for (Player participant : newParticipants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (!teams.contains(team)) {
                teams.add(team);
            }
        }
        this.teamA = teams.get(0);
        this.teamB = teams.get(1);
    }

    private void setUpTeamSpawns() {
        teamSpawns = new HashMap<>();
        teamSpawns.put(teamA, new Location(finalGameWorld, -999, 2, 22, 179, 0));
        teamSpawns.put(teamB, new Location(finalGameWorld, -999, 2, -21, 0, 0));
    }
    
    private void replaceSandGateA(String teamA) {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(finalGameWorld, -1002, -3, 19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(finalGameWorld, -1002, 1, 19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(teamA);
        BlockPlacementUtils.createCubeReplace(finalGameWorld, -1002, 2, 19, 5, 4, 1, Material.AIR, teamPowderColor);
    }
    
    private void replaceSandGateB(String teamB) {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(finalGameWorld, -1002, -3, -19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(finalGameWorld, -1002, 1, -19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(teamB);
        BlockPlacementUtils.createCubeReplace(finalGameWorld, -1002, 2, -19, 5, 4, 1, Material.AIR, teamPowderColor);
    }
    
    private void dropSandGates() {
        //a
        BlockPlacementUtils.createCube(finalGameWorld, -1002, 1, 19, 5, 1, 1, Material.AIR);
        //b
        BlockPlacementUtils.createCube(finalGameWorld, -1002, 1, -19, 5, 1, 1, Material.AIR);
    }
    

    private void initializeParticipant(Player participant) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        String team = gameManager.getTeamName(participantUniqueId);
        if (!teamKillCounts.containsKey(team)) {
            teamKillCounts.put(team, 0);
        }
        teleportParticipantToSpawn(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        giveParticipantEquipment(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        initializeFastBoard(participant);
    }

    private void giveParticipantEquipment(Player participant) {
        //stone sword, bow, 16 arrows, leather chest and boots, 16 steak
        participant.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        participant.getInventory().addItem(new ItemStack(Material.BOW));
        participant.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        participant.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        replaceSandGateA(teamA);
        replaceSandGateB(teamB);
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping final game");
    }
    
    private void stop(String winningTeam) {
        cancelAllTasks();
        replaceSandGateA(teamA);
        replaceSandGateB(teamB);
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        gameActive = false;
        gameManager.finalGameIsOver(winningTeam);
        Bukkit.getLogger().info(winningTeam + " won the final game");
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
        dropSandGates();
        messageAllParticipants(Component.text("Go!")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN));
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameActive) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        onParticipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        String teamWith3Kills = getTeamWith3Kills();
        if (teamWith3Kills != null) {
            finishFinalGame(teamWith3Kills);
        }
        event.setCancelled(true);
    }
    
    private void finishFinalGame(String winningTeam) {
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Bukkit.getServer().sendMessage(Component.empty()
                .append(gameManager.getFormattedTeamDisplayName(winningTeam))
                .append(Component.text(" wins MCT #4!"))
                .color(teamColor)
                .decorate(TextDecoration.BOLD));
        stop(winningTeam);
    }

    /**
     * returns the team with 3 or more kills, or null if no team has 3 kills
     * @return the team with 3 or more kills, or null if no team has 3 kills
     */
    private String getTeamWith3Kills() {
        for (String teamName : teamKillCounts.keySet()) {
            if (teamKillCounts.get(teamName) >= MAX_KILLS) {
                return teamName;
            }
        }
        return null;
    }

    private void onParticipantGetKill(Player killed) {
        Player killer = killed.getKiller();
        if (!participants.contains(killer)) {
            return;
        }
        addKill(killer);
    }

    private void addKill(Player killer) {
        String killerTeam = gameManager.getTeamName(killer.getUniqueId());
        int oldKillCount = teamKillCounts.get(killerTeam);
        int newKillCount = oldKillCount + 1;
        teamKillCounts.put(killerTeam, newKillCount);
        Component displayName = gameManager.getFormattedTeamDisplayName(killerTeam);
        Bukkit.getServer().sendMessage(Component.empty()
                .append(displayName)
                .append(Component.text(" has "))
                .append(Component.text(newKillCount))
                .append(Component.text("/"))
                .append(Component.text(MAX_KILLS))
                .append(Component.text(" kills!"))
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
        );
    }

    private void onParticipantDeath(Player killed) {
        ParticipantInitializer.resetHealthAndHunger(killed);
        killed.getInventory().clear();
        giveParticipantEquipment(killed);
        new BukkitRunnable() {
            @Override
            public void run() {
                teleportParticipantToSpawn(killed);
            }
        }.runTaskLater(plugin, 1L);
    }
    
    private void teleportParticipantToSpawn(Player participant) {
        String team = gameManager.getTeamName(participant.getUniqueId());
        Location spawnLocation = teamSpawns.get(team);
        participant.teleport(spawnLocation);
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
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private void initializeFastBoard(Player participant) {
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        int teamKillCount = teamKillCounts.getOrDefault(teamName, 0);
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+teamKillCount+"/"+MAX_KILLS,
                ""
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
}
