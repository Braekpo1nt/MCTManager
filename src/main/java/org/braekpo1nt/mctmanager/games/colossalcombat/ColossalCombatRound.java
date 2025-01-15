package org.braekpo1nt.mctmanager.games.colossalcombat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ColossalCombatRound implements Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ColossalCombatGame colossalCombatGame;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final BattleTopbar topbar;
    private final TimerManager timerManager;
    private ColossalCombatConfig config;
    private Map<UUID, Boolean> firstPlaceParticipantsAlive = new HashMap<>();
    private Map<UUID, Boolean> secondPlaceParticipantsAlive = new HashMap<>();
    private String firstTeamId;
    private String secondTeamId;
    private Map<UUID, Participant> firstPlaceParticipants = new HashMap<>();
    private Map<UUID, Participant> secondPlaceParticipants = new HashMap<>();
    private Map<UUID, Participant> spectators = new HashMap<>();
    private int antiSuffocationTaskId;
    private boolean antiSuffocation = false;
    private boolean roundActive = false;
    private boolean roundHasStarted = false;
    private boolean captureTheFlagStarted = false;
    private Location flagPosition = null;
    private Player hasFlag = null;
    
    public ColossalCombatRound(Main plugin, GameManager gameManager, ColossalCombatGame colossalCombatGame, ColossalCombatConfig config, Sidebar sidebar, Sidebar adminSidebar, @NotNull BattleTopbar topbar) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.colossalCombatGame = colossalCombatGame;
        this.config = config;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
        this.topbar = topbar;
        this.timerManager = new TimerManager(plugin);
    }
    
    public void setConfig(ColossalCombatConfig config) {
        this.config = config;
    }
    
    public void start(Collection<Participant> newFirstPlaceParticipants, Collection<Participant> newSecondPlaceParticipants, Collection<Participant> newSpectators, String firstTeamId, String secondTeamId) {
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
        firstPlaceParticipants = new HashMap<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new HashMap<>(newSecondPlaceParticipants.size());
        firstPlaceParticipantsAlive = new HashMap<>();
        secondPlaceParticipantsAlive = new HashMap<>();
        spectators = new HashMap<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        antiSuffocation = false;
        captureTheFlagStarted = false;
        flagPosition = null;
        hasFlag = null;
        colossalCombatGame.closeGates();
        for (Participant first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Participant second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Participant spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        initializeSidebar();
        setupTeamOptions();
        roundActive = true;
        roundHasStarted = false;
        startRoundStartingCountDown();
        if (shouldStartCaptureTheFlag()) {
            startCaptureTheFlagCountdown();
        }
        Main.logger().info("Starting Colossal Combat round");
    }
    
    private void initializeFirstPlaceParticipant(Participant first) {
        firstPlaceParticipants.put(first.getUniqueId(), first);
        first.teleport(config.getFirstPlaceSpawn());
        first.setRespawnLocation(config.getFirstPlaceSpawn(), true);
        firstPlaceParticipantsAlive.put(first.getUniqueId(), true);
        first.getInventory().clear();
        first.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(first);
        ParticipantInitializer.resetHealthAndHunger(first);
        giveParticipantEquipment(first);
        updateAliveCount(firstPlaceParticipantsAlive, firstTeamId);
    }
    
    private void rejoinFirstPlaceParticipant(Participant first) {
        firstPlaceParticipants.put(first.getUniqueId(), first);
        first.getInventory().clear();
        first.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(first);
        ParticipantInitializer.resetHealthAndHunger(first);
    }
    
    private void initializeSecondPlaceParticipant(Participant second) {
        secondPlaceParticipants.put(second.getUniqueId(), second);
        second.teleport(config.getSecondPlaceSpawn());
        second.setRespawnLocation(config.getSecondPlaceSpawn(), true);
        secondPlaceParticipantsAlive.put(second.getUniqueId(), true);
        second.getInventory().clear();
        second.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(second);
        ParticipantInitializer.resetHealthAndHunger(second);
        giveParticipantEquipment(second);
        updateAliveCount(secondPlaceParticipantsAlive, secondTeamId);
    }
    
    private void rejoinSecondPlaceParticipant(Participant second) {
        secondPlaceParticipants.put(second.getUniqueId(), second);
        second.getInventory().clear();
        second.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(second);
        ParticipantInitializer.resetHealthAndHunger(second);
    }
    
    private void initializeSpectator(Participant spectator) {
        spectators.put(spectator.getUniqueId(), spectator);
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
        captureTheFlagStarted = false;
        hasFlag = null;
        resetArena();
        for (Participant participant : firstPlaceParticipants.values()) {
            resetParticipant(participant);
        }
        firstPlaceParticipants.clear();
        for (Participant participant : secondPlaceParticipants.values()) {
            resetParticipant(participant);
        }
        secondPlaceParticipants.clear();
        for (Participant participant : spectators.values()) {
            resetParticipant(participant);
        }
        flagPosition = null;
        spectators.clear();
        firstTeamId = null;
        secondTeamId = null;
        Main.logger().info("Stopping Colossal Combat round");
    }
    
    private void resetArena() {
        colossalCombatGame.closeGates();
        // remove items/arrows on the ground
        BoundingBox removeArea = config.getRemoveArea();
        for (Arrow arrow : config.getWorld().getEntitiesByClass(Arrow.class)) {
            if (removeArea.contains(arrow.getLocation().toVector())) {
                arrow.remove();
            }
        }
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (removeArea.contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
        if (flagPosition != null) {
            flagPosition.getBlock().setType(Material.AIR);
        }
        colossalCombatGame.removeConcrete();
    }
    
    private void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
    }
    
    public void onParticipantJoin(Participant participant) {
        if (!roundActive) {
            return;
        }
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        if (firstTeamId.equals(teamId)) {
            onFirstPlaceParticipantJoin(participant);
        } else if (secondTeamId.equals(teamId)) {
            onSecondPlaceParticipantJoin(participant);
        } else {
            initializeSpectator(participant);
        }
    }
    
    private void onFirstPlaceParticipantJoin(Participant first) {
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
    
    private void onSecondPlaceParticipantJoin(Participant second) {
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
    
    private boolean participantShouldRejoin(Participant participant) {
        return firstPlaceParticipantsAlive.containsKey(participant.getUniqueId()) || secondPlaceParticipantsAlive.containsKey(participant.getUniqueId());
    }
    
    public void onParticipantQuit(Participant participant) {
        if (!roundActive) {
            return;
        }
        String teamId = gameManager.getTeamId(participant.getUniqueId());
        if (firstTeamId.equals(teamId)) {
            if (roundHasStarted) {
                killParticipant(participant);
            } else {
                firstPlaceParticipantsAlive.remove(participant.getUniqueId());
                updateAliveCount(firstPlaceParticipantsAlive, firstTeamId);
            }
            resetParticipant(participant);
            firstPlaceParticipants.remove(participant.getUniqueId());
        } else if (secondTeamId.equals(teamId)) {
            if (roundHasStarted) {
                killParticipant(participant);
            } else {
                secondPlaceParticipantsAlive.remove(participant.getUniqueId());
                updateAliveCount(secondPlaceParticipantsAlive, secondTeamId);
            }
            resetParticipant(participant);
            secondPlaceParticipants.remove(participant.getUniqueId());
        } else {
            resetParticipant(participant);
            spectators.remove(participant.getUniqueId());
        }
    }
    
    public void killParticipant(Participant participant) {
        Component deathMessage = Component.empty()
                .append(Component.text(participant.getName()))
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(), 
                DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
        onPlayerDeath(fakeDeathEvent);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(antiSuffocationTaskId);
        timerManager.cancel();
    }
    
    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        Player participant = event.getPlayer();
        if (!firstPlaceParticipants.containsKey(participant.getUniqueId()) 
                && !secondPlaceParticipants.containsKey(participant.getUniqueId()) 
                && !spectators.containsKey(participant.getUniqueId())) {
            return;
        }
        event.getArrow().remove();
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        if (!firstPlaceParticipants.containsKey(killed.getUniqueId()) 
                && !secondPlaceParticipants.containsKey(killed.getUniqueId())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "ColossalCombatRound.onPlayerDeath() cancelled");
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        Player killer = killed.getKiller();
        if (killer != null) {
            onParticipantGetKill(killer, killed);
        }
        onParticipantDeath(killed);
    }
    
    private void onParticipantDeath(Player killed) {
        killed.setGameMode(GameMode.SPECTATOR);
        killed.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        Bukkit.getScheduler().runTaskLater(plugin, () -> ParticipantInitializer.clearStatusEffects(killed), 2L);
        if (captureTheFlagStarted) {
            if (hasFlag(killed)) {
                dropFlag(killed);
            }
        }
        colossalCombatGame.addDeath(killed.getUniqueId());
        if (firstPlaceParticipants.containsKey(killed.getUniqueId())) {
            firstPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            updateAliveCount(firstPlaceParticipantsAlive, firstTeamId);
            if (allParticipantsAreDead(firstPlaceParticipantsAlive)) {
                onSecondPlaceTeamWin();
                return;
            }
        } else if (secondPlaceParticipants.containsKey(killed.getUniqueId())) {
            secondPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            updateAliveCount(secondPlaceParticipantsAlive, secondTeamId);
            if (allParticipantsAreDead(secondPlaceParticipantsAlive)) {
                onFirstPlaceTeamWin();
                return;
            }
        }
        if (shouldStartCaptureTheFlag()) {
            startCaptureTheFlagCountdown();
        }
    }
    
    private void onParticipantGetKill(@NotNull Player killer, @NotNull Player killed) {
        if (!firstPlaceParticipants.containsKey(killer.getUniqueId()) 
                && !secondPlaceParticipants.containsKey(killer.getUniqueId())) {
            return;
        }
        colossalCombatGame.addKill(killer.getUniqueId());
        UIUtils.showKillTitle(killer, killed);
    }
    
    /**
     * @return true if the capture-the-flag countdown should start, false otherwise
     */
    private boolean shouldStartCaptureTheFlag() {
        if (!config.shouldStartCaptureTheFlag()) {
            return false;
        }
        if (captureTheFlagStarted) {
            return false;
        }
        long firstCount = countLivingParticipants(firstPlaceParticipantsAlive);
        long secondCount = countLivingParticipants(secondPlaceParticipantsAlive);
        return firstCount <= config.getCaptureTheFlagMaximumPlayers() && secondCount <= config.getCaptureTheFlagMaximumPlayers();
    }
    
    private long countLivingParticipants(Map<UUID, Boolean> participantsAlive) {
        return participantsAlive.values().stream().filter(alive -> alive).count();
    }
    
    private void updateAliveCount(@NotNull Map<UUID, Boolean> participantsAlive, @NotNull String teamId) {
        int alive = (int) countLivingParticipants(participantsAlive);
        int dead = participantsAlive.size() - alive;
        topbar.setMembers(teamId, alive, dead);
    }
    
    private void startCaptureTheFlagCountdown() {
        timerManager.start(Timer.builder()
                        .duration(config.getCaptureTheFlagDuration())
                        .withSidebar(adminSidebar, "timer")
                        .sidebarPrefix(Component.text("Flag spawns in: "))
                        .onCompletion(this::startCaptureTheFlag)
                        .build());
    }
    
    private void startCaptureTheFlag() {
        captureTheFlagStarted = true;
        flagPosition = config.getFlagLocation();
        BlockPlacementUtils.placeFlag(config.getFlagMaterial(), flagPosition, config.getInitialFlagDirection());
        messageAllParticipants(config.getFlagSpawnMessage());
    }
    
    /**
     * @param participant the participant
     * @return true if this participant has the flag
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasFlag(Player participant) {
        return Objects.equals(participant, hasFlag);
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
        spawnItemDrops();
        if (allParticipantsAreDead(firstPlaceParticipantsAlive) || firstPlaceParticipants.isEmpty()) {
            onSecondPlaceTeamWin();
        } else if (allParticipantsAreDead(secondPlaceParticipantsAlive) || secondPlaceParticipants.isEmpty()) {
            onFirstPlaceTeamWin();
        }
    }
    
    private void giveParticipantEquipment(Participant participant) {
        participant.getInventory().setContents(config.getLoadout());
        GameManagerUtils.colorLeatherArmor(gameManager, participant);
    }
    
    private void spawnItemDrops() {
        if (config.getItemDropLocations() == null 
                || config.getItemDropLocations().isEmpty() 
                || config.getItemDrops() == null 
                || config.getItemDrops().isEmpty()) {
            return;
        }
        for (int i = 0; i < config.getItemDropLocations().size(); i++) {
            Location location = config.getItemDropLocations().get(i);
            ItemStack item = config.getItemDrops().get(i);
            boolean glowing = config.getGlowingItemDrops().get(i);
            Item itemEntity = config.getWorld().dropItem(location, item);
            itemEntity.setGlowing(glowing);
        }
    }
    
    private void startRoundStartingCountDown() {
        timerManager.start(Timer.builder()
                        .duration(config.getRoundStartingDuration())
                        .withSidebar(adminSidebar, "timer")
                        .withTopbar(topbar)
                        .sidebarPrefix(Component.text("Starting: "))
                        .titleThreshold(5)
                        .titleAudience(Audience.audience(
                                Audience.audience(firstPlaceParticipants.values()),
                                Audience.audience(secondPlaceParticipants.values()),
                                Audience.audience(spectators.values())))
                        .onCompletion(this::startRound)
                        .build());
    }
    
    private void initializeSidebar() {
        adminSidebar.updateLine("timer", "Starting:");
        topbar.setMiddle(Component.empty());
    }
    
    private void openGates() {
        antiSuffocation = true;
        this.antiSuffocationTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> antiSuffocation = false, config.getAntiSuffocationDuration()).getTaskId();
        //first
        BlockPlacementUtils.createCube(config.getWorld(), config.getFirstPlaceStone(), Material.AIR);
        //second
        BlockPlacementUtils.createCube(config.getWorld(), config.getSecondPlaceStone(), Material.AIR);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!roundActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (firstPlaceParticipants.containsKey(participant.getUniqueId())) {
            onFirstPlaceParticipantMove(participant, event);
        } else if (secondPlaceParticipants.containsKey(participant.getUniqueId())) {
            onSecondPlaceParticipantMove(participant, event);
        }
    }
    
    private void onFirstPlaceParticipantMove(Player participant, PlayerMoveEvent event) {
        if (antiSuffocation) {
            Location to = event.getTo();
            if (config.getFirstPlaceAntiSuffocationArea().contains(to.toVector())) {
                event.setCancelled(true);
            }
            return;
        }
        if (!captureTheFlagStarted) {
            return;
        }
        if (!firstPlaceParticipantsAlive.get(participant.getUniqueId())) {
            return;
        }
        Location location = participant.getLocation();
        if (canPickUpFlag(location)) {
            pickupFlag(participant);
            return;
        }
        if (canDeliverFlagToFirst(participant)) {
            deliverFlagToFirst(participant);
        }
    }
    
    private void onSecondPlaceParticipantMove(Player participant, PlayerMoveEvent event) {
        if (antiSuffocation) {
            Location to = event.getTo();
            if (config.getSecondPlaceAntiSuffocationArea().contains(to.toVector())) {
                event.setCancelled(true);
            }
            return;
        }
        if (!captureTheFlagStarted) {
            return;
        }
        if (!secondPlaceParticipantsAlive.get(participant.getUniqueId())) {
            return;
        }
        Location location = participant.getLocation();
        if (canPickUpFlag(location)) {
            pickupFlag(participant);
            return;
        }
        if (canDeliverFlagToSecond(participant)) {
            deliverFlagToSecond(participant);
        }
    }
    
    /**
     * @param location the location to check
     * @return true if the flag is on the ground, and the given location's blockLocation is equal to the flag's current position
     */
    private boolean canPickUpFlag(Location location) {
        if (flagPosition == null) {
            return false;
        }
        return flagPosition.getBlockX() == location.getBlockX() && flagPosition.getBlockY() == location.getBlockY() && flagPosition.getBlockZ() == location.getBlockZ();
    }
    
    private void pickupFlag(Player participant) {
        String team = gameManager.getTeamId(participant.getUniqueId());
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" has the flag")));
        participant.getEquipment().setHelmet(new ItemStack(config.getFlagMaterial()));
        flagPosition.getBlock().setType(Material.AIR);
        flagPosition = null;
        hasFlag = participant;
    }
    
    private void dropFlag(Player participant) {
        String team = gameManager.getTeamId(participant.getUniqueId());
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" dropped the flag")));
        participant.getEquipment().setHelmet(null);
        flagPosition = BlockPlacementUtils.getBlockDropLocation(participant.getLocation());
        BlockPlacementUtils.placeFlag(config.getFlagMaterial(), flagPosition, participant.getFacing());
        hasFlag = null;
    }
    
    private boolean canDeliverFlagToFirst(Player first) {
        if (!hasFlag(first)) {
            return false;
        }
        Location location = first.getLocation();
        return config.getFirstPlaceFlagGoal().contains(location.toVector());
    }
    
    private boolean canDeliverFlagToSecond(Player second) {
        if (!hasFlag(second)) {
            return false;
        }
        Location location = second.getLocation();
        return config.getSecondPlaceFlagGoal().contains(location.toVector());
    }
    
    private void deliverFlagToFirst(Player first) {
        String team = gameManager.getTeamId(first.getUniqueId());
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" captured the flag!")));
        onFirstPlaceTeamWin();
    }
    
    private void deliverFlagToSecond(Player second) {
        String team = gameManager.getTeamId(second.getUniqueId());
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" captured the flag!")));
        onSecondPlaceTeamWin();
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
        Audience.audience(
                Audience.audience(firstPlaceParticipants.values()),
                Audience.audience(secondPlaceParticipants.values()),
                Audience.audience(spectators.values())
        ).sendMessage(message);
    }
    
}
