package org.braekpo1nt.mctmanager.games.colossalcombat;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class ColossalCombatRound implements Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ColossalCombatGame colossalCombatGame;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ColossalCombatStorageUtil storageUtil;
    private Map<UUID, Boolean> firstPlaceParticipantsAlive = new HashMap<>();
    private Map<UUID, Boolean> secondPlaceParticipantsAlive = new HashMap<>();
    private String firstTeamName;
    private String secondTeamName;
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private int startCountDownTaskId;
    private int antiSuffocationTaskId;
    private boolean antiSuffocation = false;
    private boolean roundActive = false;
    private boolean roundHasStarted = false;
    
    public ColossalCombatRound(Main plugin, GameManager gameManager, ColossalCombatGame colossalCombatGame, ColossalCombatStorageUtil storageUtil, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.colossalCombatGame = colossalCombatGame;
        this.storageUtil = storageUtil;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
    }
    
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators, String firstTeamName, String secondTeamName) {
        this.firstTeamName = firstTeamName;
        this.secondTeamName = secondTeamName;
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        firstPlaceParticipantsAlive = new HashMap<>();
        secondPlaceParticipantsAlive = new HashMap<>();
        spectators = new ArrayList<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        antiSuffocation = false;
        colossalCombatGame.closeGates();
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
        roundActive = true;
        roundHasStarted = false;
        startRoundStartingCountDown();
        Bukkit.getLogger().info("Starting Colossal Combat round");
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.teleport(storageUtil.getFirstPlaceSpawn());
        firstPlaceParticipantsAlive.put(first.getUniqueId(), true);
        first.getInventory().clear();
        first.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(first);
        ParticipantInitializer.resetHealthAndHunger(first);
    }
    
    private void rejoinFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.getInventory().clear();
        first.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(first);
        ParticipantInitializer.resetHealthAndHunger(first);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.teleport(storageUtil.getSecondPlaceSpawn());
        secondPlaceParticipantsAlive.put(second.getUniqueId(), true);
        second.getInventory().clear();
        second.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(second);
        ParticipantInitializer.resetHealthAndHunger(second);
    }
    
    private void rejoinSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.getInventory().clear();
        second.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(second);
        ParticipantInitializer.resetHealthAndHunger(second);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        spectator.getInventory().clear();
        spectator.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(spectator);
        ParticipantInitializer.resetHealthAndHunger(spectator);
    }
    
    private void onFirstPlaceTeamWin() {
        stop();
        colossalCombatGame.onFirstPlaceWinRound();
    }
    
    private void onSecondPlaceTeamWin() {
        stop();
        colossalCombatGame.onSecondPlaceWinRound();
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        roundActive = false;
        antiSuffocation = false;
        resetArena();
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
        firstTeamName = null;
        secondTeamName = null;
        Bukkit.getLogger().info("Stopping Colossal Combat round");
    }
    
    private void resetArena() {
        colossalCombatGame.closeGates();
        // remove items/arrows on the ground
        BoundingBox removeArea = storageUtil.getRemoveArea();
        for (Arrow arrow : storageUtil.getWorld().getEntitiesByClass(Arrow.class)) {
            if (removeArea.contains(arrow.getLocation().toVector())) {
                arrow.remove();
            }
        }
        for (Item item : storageUtil.getWorld().getEntitiesByClass(Item.class)) {
            if (removeArea.contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    public void onParticipantJoin(Player participant) {
        if (!roundActive) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            onFirstPlaceParticipantJoin(participant);
        } else if (secondTeamName.equals(teamName)) {
            onSecondPlaceParticipantJoin(participant);
        } else {
            initializeSpectator(participant);
        }
    }
    
    private void onFirstPlaceParticipantJoin(Player first) {
        if (roundHasStarted) {
            if (participantShouldRejoin(first)) {
                rejoinFirstPlaceParticipant(first);
                giveParticipantEquipment(first);
                return;
            }
            initializeFirstPlaceParticipant(first);
            giveParticipantEquipment(first);
            return;
        }
        initializeFirstPlaceParticipant(first);
    }
    
    private void onSecondPlaceParticipantJoin(Player second) {
        if (roundHasStarted) {
            if (participantShouldRejoin(second)) {
                rejoinSecondPlaceParticipant(second);
                giveParticipantEquipment(second);
                return;
            }
            initializeSecondPlaceParticipant(second);
            giveParticipantEquipment(second);
            return;
        }
        initializeSecondPlaceParticipant(second);
    }
    
    private boolean participantShouldRejoin(Player participant) {
        return firstPlaceParticipantsAlive.containsKey(participant.getUniqueId()) || secondPlaceParticipantsAlive.containsKey(participant.getUniqueId());
    }
    
    public void onParticipantQuit(Player participant) {
        if (!roundActive) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (firstTeamName.equals(teamName)) {
            if (roundHasStarted) {
                killParticipant(participant);
            } else {
                firstPlaceParticipantsAlive.remove(participant.getUniqueId());
            }
            resetParticipant(participant);
            firstPlaceParticipants.remove(participant);
        } else if (secondTeamName.equals(teamName)) {
            if (roundHasStarted) {
                killParticipant(participant);
            } else {
                secondPlaceParticipantsAlive.remove(participant.getUniqueId());
            }
            resetParticipant(participant);
            secondPlaceParticipants.remove(participant);
        } else {
            resetParticipant(participant);
            spectators.remove(participant);
        }
    }
    
    public void killParticipant(Player participant) {
        Component deathMessage = Component.text(participant.getName())
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, Collections.emptyList(), 0, deathMessage);
        Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskId);
        Bukkit.getScheduler().cancelTask(antiSuffocationTaskId);
    }
    
    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if (!firstPlaceParticipants.contains(event.getPlayer()) 
                && !secondPlaceParticipants.contains(event.getPlayer()) 
                && !spectators.contains(event.getPlayer())) {
            return;
        }
        event.getArrow().remove();
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        Bukkit.getLogger().info("onPlayerDeath " + killed.getName());
        if (!firstPlaceParticipants.contains(killed) && !secondPlaceParticipants.contains(killed)) {
            return;
        }
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
    }
    
    private void onParticipantDeath(Player killed) {
        killed.setGameMode(GameMode.SPECTATOR);
        killed.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        Bukkit.getScheduler().runTaskLater(plugin, () -> ParticipantInitializer.clearStatusEffects(killed), 2L);
        if (firstPlaceParticipants.contains(killed)) {
            firstPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            if (allParticipantsAreDead(firstPlaceParticipantsAlive)) {
                onSecondPlaceTeamWin();
            }
            return;
        }
        if (secondPlaceParticipants.contains(killed)) {
            secondPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            if (allParticipantsAreDead(secondPlaceParticipantsAlive)) {
                onFirstPlaceTeamWin();
            }
        }
    }
    
    /**
     * @param participantsAreAlive the mapping of a participants UUID to their alive status. True is alive, false is dead.
     * @return True if all participants on the given map are dead, false if even one is alive
     */
    private boolean allParticipantsAreDead(Map<UUID, Boolean> participantsAreAlive) {
        for (boolean isAlive : participantsAreAlive.values()) {
            if (isAlive) {
                return false;
            }
        }
        return true;
    }
    
    private void startRound() {
        roundHasStarted = true;
        openGates();
        for (Player participant : firstPlaceParticipants) {
            giveParticipantEquipment(participant);
        }
        for (Player participant : secondPlaceParticipants) {
            giveParticipantEquipment(participant);
        }
        if (allParticipantsAreDead(firstPlaceParticipantsAlive) || firstPlaceParticipants.isEmpty()) {
            onSecondPlaceTeamWin();
        } else if (allParticipantsAreDead(secondPlaceParticipantsAlive) || secondPlaceParticipants.isEmpty()) {
            onFirstPlaceTeamWin();
        }
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
    
    private void startRoundStartingCountDown() {
        this.startCountDownTaskId = new BukkitRunnable() {
            private int count = storageUtil.getRoundStartingDuration();
    
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    startRound();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                sidebar.updateLine("timer", String.format("Starting: %s", timeLeft));
                adminSidebar.updateLine("timer", String.format("Starting: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeSidebar() {
        sidebar.updateLine("timer", "Starting:");
        adminSidebar.updateLine("timer", "Starting:");
    }
    
    private void openGates() {
        antiSuffocation = true;
        this.antiSuffocationTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> antiSuffocation = false, storageUtil.getAntiSuffocationDuration()).getTaskId();
        //first
        BlockPlacementUtils.createCube(storageUtil.getWorld(), storageUtil.getFirstPlaceStone(), Material.AIR);
        //second
        BlockPlacementUtils.createCube(storageUtil.getWorld(), storageUtil.getSecondPlaceStone(), Material.AIR);
    }
    
    @EventHandler
    public void antiSuffocationMovementDetection(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        if (!antiSuffocation) {
            return;
        }
        Location to = event.getTo();
        Player participant = event.getPlayer();
        if (firstPlaceParticipants.contains(participant)) {
            if (storageUtil.getFirstPlaceAntiSuffocationArea().contains(to.toVector())) {
                event.setCancelled(true);
            }
        } else if (secondPlaceParticipants.contains(participant)) {
            if (storageUtil.getSecondPlaceAntiSuffocationArea().contains(to.toVector())) {
                event.setCancelled(true);
            }
        }
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
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : firstPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : secondPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : spectators) {
            participant.sendMessage(message);
        }
    }
    
}
