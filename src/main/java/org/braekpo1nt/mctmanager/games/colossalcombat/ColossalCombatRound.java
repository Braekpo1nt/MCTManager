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
    private ColossalRoundTeam first;
    private ColossalRoundTeam second;
    private Map<UUID, ColossalRoundParticipant> firstPlaceParticipants = new HashMap<>();
    private Map<UUID, ColossalRoundParticipant> secondPlaceParticipants = new HashMap<>();
    private Map<UUID, Participant> spectators = new HashMap<>();
    private int antiSuffocationTaskId;
    private boolean antiSuffocation = false;
    private boolean roundActive = false;
    private boolean roundHasStarted = false;
    private boolean captureTheFlagStarted = false;
    private Location flagPosition = null;
    private Participant hasFlag = null;
    
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
    
    public void start(Collection<ColossalParticipant> newFirstPlaceParticipants, Collection<ColossalParticipant> newSecondPlaceParticipants, Collection<Participant> newSpectators, ColossalTeam newFirst, ColossalTeam newSecond) {
        this.first = new ColossalRoundTeam(newFirst);
        this.second = new ColossalRoundTeam(newSecond);
        firstPlaceParticipants = new HashMap<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new HashMap<>(newSecondPlaceParticipants.size());
        spectators = new HashMap<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        antiSuffocation = false;
        captureTheFlagStarted = false;
        flagPosition = null;
        hasFlag = null;
        colossalCombatGame.closeGates();
        for (ColossalParticipant participant : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(participant);
        }
        for (ColossalParticipant participant : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(participant);
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
    
    private void initializeFirstPlaceParticipant(ColossalParticipant newParticipant) {
        initializeFirstPlaceParticipant(newParticipant, true);
    }
    
    private void initializeFirstPlaceParticipant(ColossalParticipant newParticipant, boolean alive) {
        ColossalRoundParticipant participant = new ColossalRoundParticipant(newParticipant, alive);
        first.addParticipant(participant);
        firstPlaceParticipants.put(participant.getUniqueId(), participant);
        participant.teleport(config.getFirstPlaceSpawn());
        participant.setRespawnLocation(config.getFirstPlaceSpawn(), true);
        participant.getInventory().clear();
        participant.setGameMode(alive ? GameMode.ADVENTURE : GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        giveParticipantEquipment(participant);
        updateAliveCount(first);
    }
    
    private void initializeSecondPlaceParticipant(ColossalParticipant newParticipant) {
        initializeSecondPlaceParticipant(newParticipant, true);
    }
    
    private void initializeSecondPlaceParticipant(ColossalParticipant newParticipant, boolean alive) {
        ColossalRoundParticipant participant = new ColossalRoundParticipant(newParticipant, alive);
        second.addParticipant(participant);
        secondPlaceParticipants.put(participant.getUniqueId(), participant);
        participant.teleport(config.getSecondPlaceSpawn());
        participant.setRespawnLocation(config.getSecondPlaceSpawn(), true);
        participant.getInventory().clear();
        participant.setGameMode(alive ? GameMode.ADVENTURE : GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        giveParticipantEquipment(participant);
        updateAliveCount(second);
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
            first.removeParticipant(participant.getUniqueId());
        }
        firstPlaceParticipants.clear();
        for (Participant participant : secondPlaceParticipants.values()) {
            resetParticipant(participant);
            second.removeParticipant(participant.getUniqueId());
        }
        secondPlaceParticipants.clear();
        for (Participant participant : spectators.values()) {
            resetParticipant(participant);
        }
        flagPosition = null;
        spectators.clear();
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
    
    public void onParticipantJoin(ColossalParticipant participant) {
        if (!roundActive) {
            return;
        }
        if (participant.getAffiliation() == Affiliation.FIRST) {
            onFirstPlaceParticipantJoin(participant);
        } else {
            onSecondPlaceParticipantJoin(participant);
        }
    }
    
    public void onSpectatorJoin(Participant participant) {
        if (!roundActive) {
            return;
        }
        initializeSpectator(participant);
    }
    
    private void onFirstPlaceParticipantJoin(ColossalParticipant participant) {
        if (roundHasStarted) {
            initializeFirstPlaceParticipant(participant, false);
            return;
        }
        initializeFirstPlaceParticipant(participant);
    }
    
    private void onSecondPlaceParticipantJoin(ColossalParticipant participant) {
        if (roundHasStarted) {
            initializeSecondPlaceParticipant(participant, false);
            return;
        }
        initializeSecondPlaceParticipant(participant);
    }
    
    public void onParticipantQuit(Participant participant) {
        if (!roundActive) {
            return;
        }
        if (first.getTeamId().equals(participant.getTeamId())) {
            ColossalRoundParticipant colossalRoundParticipant = firstPlaceParticipants.get(participant.getUniqueId());
            if (colossalRoundParticipant == null) {
                return;
            }
            if (roundHasStarted) {
                killParticipant(colossalRoundParticipant);
            } else {
                updateAliveCount(first);
            }
            resetParticipant(participant);
            first.removeParticipant(participant.getUniqueId());
            firstPlaceParticipants.remove(participant.getUniqueId());
        } else if (second.getTeamId().equals(participant.getTeamId())) {
            ColossalRoundParticipant colossalRoundParticipant = secondPlaceParticipants.get(participant.getUniqueId());
            if (colossalRoundParticipant == null) {
                return;
            }
            if (roundHasStarted) {
                killParticipant(colossalRoundParticipant);
            } else {
                updateAliveCount(second);
            }
            resetParticipant(participant);
            second.removeParticipant(participant.getUniqueId());
            secondPlaceParticipants.remove(participant.getUniqueId());
        } else {
            resetParticipant(participant);
            spectators.remove(participant.getUniqueId());
        }
    }
    
    public void killParticipant(ColossalRoundParticipant participant) {
        Component deathMessage = Component.empty()
                .append(participant.displayName())
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
        ColossalRoundParticipant killed;
        if (firstPlaceParticipants.containsKey(event.getPlayer().getUniqueId())) {
            killed = firstPlaceParticipants.get(event.getPlayer().getUniqueId());
        } else if (secondPlaceParticipants.containsKey(event.getPlayer().getUniqueId())) {
            killed = secondPlaceParticipants.get(event.getPlayer().getUniqueId());
        } else {
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
        Player killerPlayer = killed.getKiller();
        if (killerPlayer != null) {
            ColossalRoundParticipant killer;
            if (firstPlaceParticipants.containsKey(killerPlayer.getUniqueId())) {
                killer = firstPlaceParticipants.get(killerPlayer.getUniqueId());
            } else if (secondPlaceParticipants.containsKey(killerPlayer.getUniqueId())) {
                killer = secondPlaceParticipants.get(killerPlayer.getUniqueId());
            } else {
                return;
            }
            onParticipantGetKill(killer, killed);
        }
        onParticipantDeath(killed);
    }
    
    private void onParticipantDeath(ColossalRoundParticipant killed) {
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
        killed.setAlive(false);
        addDeath(killed);
        if (killed.getAffiliation() == Affiliation.FIRST) {
            updateAliveCount(first);
            if (first.isDead()) {
                onSecondPlaceTeamWin();
                return;
            }
        } else {
            updateAliveCount(second);
            if (second.isDead()) {
                onFirstPlaceTeamWin();
                return;
            }
        }
        if (shouldStartCaptureTheFlag()) {
            startCaptureTheFlagCountdown();
        }
    }
    
    private void onParticipantGetKill(@NotNull ColossalRoundParticipant killer, @NotNull ColossalRoundParticipant killed) {
        if (!firstPlaceParticipants.containsKey(killer.getUniqueId()) 
                && !secondPlaceParticipants.containsKey(killer.getUniqueId())) {
            return;
        }
        addKill(killer);
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
        return first.getAlive() <= config.getCaptureTheFlagMaximumPlayers() 
                && second.getAlive() <= config.getCaptureTheFlagMaximumPlayers();
    }
    
    private void updateAliveCount(@NotNull ColossalRoundTeam team) {
        int alive = team.getAlive();
        int dead = team.size() - alive;
        topbar.setMembers(team.getTeamId(), alive, dead);
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
    private boolean hasFlag(Participant participant) {
        return Objects.equals(participant, hasFlag);
    }
    
    private void startRound() {
        roundHasStarted = true;
        openGates();
        spawnItemDrops();
        if (first.isDead() || firstPlaceParticipants.isEmpty()) {
            onSecondPlaceTeamWin();
        } else if (second.isDead() || secondPlaceParticipants.isEmpty()) {
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
        ColossalRoundParticipant participant1 = firstPlaceParticipants.get(event.getPlayer().getUniqueId());
        if (participant1 != null) {
            onFirstPlaceParticipantMove(participant1, event);
            return;
        }
        ColossalRoundParticipant participant2 = secondPlaceParticipants.get(event.getPlayer().getUniqueId());
        if (participant2 != null) {
            onSecondPlaceParticipantMove(participant2, event);
        }
    }
    
    private void onFirstPlaceParticipantMove(ColossalRoundParticipant participant, PlayerMoveEvent event) {
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
        if (participant.isDead()) {
            return;
        }
        Location location = participant.getLocation();
        if (canPickUpFlag(location)) {
            pickupFlag(participant);
            return;
        }
        if (canDeliverFlagToFirst(participant)) {
            deliverFlagToFirst();
        }
    }
    
    private void onSecondPlaceParticipantMove(ColossalRoundParticipant participant, PlayerMoveEvent event) {
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
        if (participant.isDead()) {
            return;
        }
        Location location = participant.getLocation();
        if (canPickUpFlag(location)) {
            pickupFlag(participant);
            return;
        }
        if (canDeliverFlagToSecond(participant)) {
            deliverFlagToSecond();
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
    
    private void pickupFlag(Participant participant) {
        Component displayName = participant.getTeamId().equals(first.getTeamId()) ?
                first.getFormattedDisplayName() :
                second.getFormattedDisplayName();
        messageAllParticipants(Component.empty()
                .append(displayName)
                .append(Component.text(" has the flag")));
        participant.getEquipment().setHelmet(new ItemStack(config.getFlagMaterial()));
        flagPosition.getBlock().setType(Material.AIR);
        flagPosition = null;
        hasFlag = participant;
    }
    
    private void dropFlag(Participant participant) {
        Component displayName = participant.getTeamId().equals(first.getTeamId()) ?
                first.getFormattedDisplayName() :
                second.getFormattedDisplayName();
        messageAllParticipants(Component.empty()
                .append(displayName)
                .append(Component.text(" dropped the flag")));
        participant.getEquipment().setHelmet(null);
        flagPosition = BlockPlacementUtils.getBlockDropLocation(participant.getLocation());
        BlockPlacementUtils.placeFlag(config.getFlagMaterial(), flagPosition, participant.getFacing());
        hasFlag = null;
    }
    
    private boolean canDeliverFlagToFirst(Participant first) {
        if (!hasFlag(first)) {
            return false;
        }
        Location location = first.getLocation();
        return config.getFirstPlaceFlagGoal().contains(location.toVector());
    }
    
    private boolean canDeliverFlagToSecond(Participant second) {
        if (!hasFlag(second)) {
            return false;
        }
        Location location = second.getLocation();
        return config.getSecondPlaceFlagGoal().contains(location.toVector());
    }
    
    private void deliverFlagToFirst() {
        messageAllParticipants(Component.empty()
                .append(first.getFormattedDisplayName())
                .append(Component.text(" captured the flag!")));
        onFirstPlaceTeamWin();
    }
    
    private void deliverFlagToSecond() {
        messageAllParticipants(Component.empty()
                .append(second.getFormattedDisplayName())
                .append(Component.text(" captured the flag!")));
        onSecondPlaceTeamWin();
    }
    
    /**
     * @param participant the participant to add a kill to
     */
    void addKill(@NotNull ColossalRoundParticipant participant) {
        int oldKillCount = participant.getKills();
        int newKillCount = oldKillCount + 1;
        participant.setKills(newKillCount);
        colossalCombatGame.setKills(participant.getUniqueId(), participant.getAffiliation(), newKillCount);
        topbar.setKills(participant.getUniqueId(), newKillCount);
    }
    
    /**
     * @param participant the participant to add a death to
     */
    void addDeath(@NotNull ColossalRoundParticipant participant) {
        int oldDeathCount = participant.getDeaths();
        int newDeathCount = oldDeathCount + 1;
        participant.setDeaths(newDeathCount);
        colossalCombatGame.setDeaths(participant.getUniqueId(), participant.getAffiliation(), newDeathCount);
        topbar.setDeaths(participant.getUniqueId(), newDeathCount);
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
    
    public enum Affiliation {
        FIRST,
        SECOND,
    }
}
