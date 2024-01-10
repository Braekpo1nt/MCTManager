package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcolosseum.config.ColossalColosseumStorageUtil;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ColossalColosseumGame implements Listener, Configurable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final ColossalColosseumStorageUtil storageUtil;
    private final String title = ChatColor.BLUE+"Colossal Colosseum";
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private List<ColossalColosseumRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    private int firstPlaceRoundWins = 0;
    private int secondPlaceRoundWins = 0;
    private String firstTeamName;
    private String secondTeamName;
    private boolean gameActive = false;
    
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
     * @param newAdmins The admins
     */
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators, List<Player> newAdmins) {
        firstTeamName = gameManager.getTeamName(newFirstPlaceParticipants.get(0).getUniqueId());
        secondTeamName = gameManager.getTeamName(newSecondPlaceParticipants.get(0).getUniqueId());
        firstPlaceRoundWins = 0;
        secondPlaceRoundWins = 0;
        closeGates();
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        spectators = new ArrayList<>(newSpectators.size());
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        int numOfRounds = (storageUtil.getRequiredWins() * 2) - 1;
        rounds = new ArrayList<>(numOfRounds);
        for (int i = 0; i < numOfRounds; i++) {
            rounds.add(new ColossalColosseumRound(plugin, gameManager, this, storageUtil, sidebar, adminSidebar));
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
        startAdmins(newAdmins);
        startNextRound();
        gameActive = true;
        Bukkit.getLogger().info("Started Colossal Colosseum");
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.teleport(storageUtil.getFirstPlaceSpawn());
        first.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(first);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.teleport(storageUtil.getSecondPlaceSpawn());
        second.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(second);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        spectator.teleport(storageUtil.getSpectatorSpawn());
        spectator.setGameMode(GameMode.ADVENTURE);
        sidebar.addPlayer(spectator);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        updateRoundWinSidebar();
        adminSidebar.updateLines(admin.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round: %s", currentRoundIndex+1))
        );
    }
    
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(storageUtil.getSpectatorSpawn());
    }
    
    private void startNextRound() {
        ColossalColosseumRound nextRound = rounds.get(currentRoundIndex);
        nextRound.start(firstPlaceParticipants, secondPlaceParticipants, spectators, firstTeamName, secondTeamName);
        sidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
        adminSidebar.updateLine("round", String.format("Round: %s", currentRoundIndex+1));
    }
    
    public void onFirstPlaceWinRound() {
        firstPlaceRoundWins++;
        updateRoundWinSidebar();
        if (firstPlaceRoundWins >= storageUtil.getRequiredWins()) {
            stop(firstTeamName);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void onSecondPlaceWinRound() {
        secondPlaceRoundWins++;
        updateRoundWinSidebar();
        if (secondPlaceRoundWins >= storageUtil.getRequiredWins()) {
            stop(secondTeamName);
            return;
        }
        currentRoundIndex++;
        startNextRound();
    }
    
    public void stop(@Nullable String winningTeam) {
        gameActive = false;
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
        stopAdmins();
        spectators.clear();
        gameManager.getEventManager().colossalColosseumIsOver(winningTeam);
        Bukkit.getLogger().info("Stopping Colossal Colosseum");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        sidebar.removePlayer(participant.getUniqueId());
    }
    
    private void stopAdmins() {
        for (Player admin : admins) {
            resetAdmin(admin);
        }
        clearAdminSidebar();
        admins.clear();
    }
    
    private void resetAdmin(Player admin) {
        adminSidebar.removePlayer(admin);
    }
    
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            firstPlaceParticipants.add(participant);
            participant.setGameMode(GameMode.SPECTATOR);
            participant.teleport(storageUtil.getFirstPlaceSpawn());
        } else if (secondTeamName.equals(teamName)) {
            secondPlaceParticipants.add(participant);
            participant.setGameMode(GameMode.SPECTATOR);
            participant.teleport(storageUtil.getSecondPlaceSpawn());
        } else {
            spectators.add(participant);
            participant.teleport(storageUtil.getSpectatorSpawn());
        }
        sidebar.addPlayer(participant);
        if (currentRoundIndex < rounds.size()) {
            ColossalColosseumRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantJoin(participant);
            }
        }
        updateRoundWinSidebar();
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("round", String.format("Round: %s", currentRoundIndex+1))
        );
    }
    
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        resetParticipant(participant);
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            firstPlaceParticipants.remove(participant);
        } else if (secondTeamName.equals(teamName)) {
            secondPlaceParticipants.remove(participant);
        } else {
            spectators.remove(participant);
        }
        if (currentRoundIndex < rounds.size()) {
            ColossalColosseumRound currentRound = rounds.get(currentRoundIndex);
            if (currentRound.isActive()) {
                currentRound.onParticipantQuit(participant);
            }
        }
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
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (spectators.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (firstPlaceParticipants.contains(participant) 
                || secondPlaceParticipants.contains(participant)) {
            // don't let them drop items from their inventory
            if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
                event.setCancelled(true);
                return;
            }
            // don't let them remove their armor
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!gameActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (firstPlaceParticipants.contains(participant)
                || secondPlaceParticipants.contains(participant)
                || spectators.contains(participant)) {
            event.setCancelled(true);
        }
    }
    
    private void updateRoundWinSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        sidebar.updateLine("firstWinCount", String.format("%s: %s/%s", firstDisplayName, firstPlaceRoundWins, storageUtil.getRequiredWins()));
        sidebar.updateLine("secondWinCount", String.format("%s: %s/%s", secondDisplayName, secondPlaceRoundWins, storageUtil.getRequiredWins()));
        adminSidebar.updateLine("firstWinCount", String.format("%s: %s/%s", firstDisplayName, firstPlaceRoundWins, storageUtil.getRequiredWins()));
        adminSidebar.updateLine("secondWinCount", String.format("%s: %s/%s", secondDisplayName, secondPlaceRoundWins, storageUtil.getRequiredWins()));
    }
    
    private void initializeAdminSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", String.format("%s: 0/%s", firstDisplayName, storageUtil.getRequiredWins())),
                new KeyLine("secondWinCount", String.format("%s: 0/%s", secondDisplayName, storageUtil.getRequiredWins())),
                new KeyLine("round", "Round: 1"),
                new KeyLine("timer", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        ChatColor firstChatColor = gameManager.getTeamChatColor(firstTeamName);
        String firstDisplayName = ChatColor.BOLD + "" +  firstChatColor + gameManager.getTeamDisplayName(firstTeamName);
        ChatColor secondChatColor = gameManager.getTeamChatColor(secondTeamName);
        String secondDisplayName = ChatColor.BOLD + "" +  secondChatColor + gameManager.getTeamDisplayName(secondTeamName);
        sidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("firstWinCount", String.format("%s: 0/%s", firstDisplayName, storageUtil.getRequiredWins())),
                new KeyLine("secondWinCount", String.format("%s: 0/%s", secondDisplayName, storageUtil.getRequiredWins())),
                new KeyLine("round", "Round: 1"),
                new KeyLine("timer", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    void closeGates() {
        closeGate(
                storageUtil.getFirstPlaceClearArea(), 
                storageUtil.getFirstPlaceStone(), 
                storageUtil.getFirstPlacePlaceArea(), 
                gameManager.getTeamPowderColor(firstTeamName)
        );
        closeGate(
                storageUtil.getSecondPlaceClearArea(), 
                storageUtil.getSecondPlaceStone(), 
                storageUtil.getSecondPlacePlaceArea(), 
                gameManager.getTeamPowderColor(secondTeamName)
        );
    }
    
    private void closeGate(BoundingBox clearArea, BoundingBox stoneArea, BoundingBox placeArea, Material teamPowderColor) {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), clearArea, powderColor, Material.AIR);
        }
        //place stone under the powder area
        BlockPlacementUtils.createCube(storageUtil.getWorld(), stoneArea, Material.STONE);
        //replace air with team powder color
        BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), placeArea, Material.AIR, teamPowderColor);
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
        return gameActive;
    }
}
