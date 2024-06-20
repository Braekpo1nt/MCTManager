package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.ui.topbar.Topbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MaterialUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class CaptureTheFlagMatch implements Listener {
    
    private final CaptureTheFlagRound captureTheFlagRound;
    private final Main plugin;
    private final GameManager gameManager;
    private final CaptureTheFlagConfig config;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final BattleTopbar topbar;
    private List<Player> northParticipants = new ArrayList<>();
    private List<Player> southParticipants = new ArrayList<>();
    private List<Player> allParticipants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<UUID, Integer> killCounts;
    private boolean matchActive = false;
    private int classSelectionCountdownTaskId;
    private int matchTimerTaskId;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    /**
     * The position of the north flag, if it has been dropped and can be picked up. Null if not
     */
    private Location northFlagPosition;
    /**
     * The position of the south flag, if it has been dropped and can be picked up. Null if not
     */
    private Location southFlagPosition;
    /**
     * The player who has the north flag, if it is picked up. Null if not.
     */
    private Player hasNorthFlag;
    /**
     * The player who has the south flag, if it is picked up. Null if not.
     */
    private Player hasSouthFlag;
    private Material northBanner;
    private Material southBanner;
    
    public CaptureTheFlagMatch(CaptureTheFlagRound captureTheFlagRound, Main plugin,
                               GameManager gameManager, MatchPairing matchPairing, Arena arena,
                               CaptureTheFlagConfig config, Sidebar sidebar, Sidebar adminSidebar) {
        this.captureTheFlagRound = captureTheFlagRound;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.config = config;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.northClassPicker = new ClassPicker();
        this.southClassPicker = new ClassPicker();
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
        this.topbar = new BattleTopbar();
    }
    
    public MatchPairing getMatchPairing() {
        return matchPairing;
    }
    
    public void start(List<Player> newNorthParticipants, List<Player> newSouthParticipants) {
        if (newNorthParticipants.isEmpty() || newSouthParticipants.isEmpty()) {
            Bukkit.getLogger().info(String.format("Skipping capture the flag match %s one of the teams is offline", matchPairing));
            matchIsOver();
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        allParticipants = new ArrayList<>();
        participantsAreAlive = new HashMap<>();
        killCounts = new HashMap<>();
        placeFlags();
        closeGlassBarriers();
        NamedTextColor northColor = gameManager.getTeamNamedTextColor(matchPairing.northTeam());
        NamedTextColor southColor = gameManager.getTeamNamedTextColor(matchPairing.southTeam());
        topbar.addTeamPair(matchPairing.northTeam(), northColor, matchPairing.southTeam(), southColor);
        for (Player northParticipant : newNorthParticipants) {
            initializeParticipant(northParticipant, true);
        }
        for (Player southParticipant : newSouthParticipants) {
            initializeParticipant(southParticipant, false);
        }
        initializeSidebar();
        setupTeamOptions();
        matchActive = true;
        startClassSelectionPeriod();
        Bukkit.getLogger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    private void initializeParticipant(Player participant, boolean north) {
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        killCounts.put(participantUniqueId, 0);
        if (north) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
            participant.lookAt(arena.southSpawn().getX(), arena.southSpawn().getY(), arena.southSpawn().getZ(), LookAnchor.EYES);
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.southSpawn());
            participant.lookAt(arena.northSpawn().getX(), arena.northSpawn().getY(), arena.northSpawn().getZ(), LookAnchor.EYES);
        }
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        topbar.showPlayer(participant, teamId);
        topbar.addMember(true, teamId);
        initializeSidebar(participant);
        allParticipants.add(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Player participant, boolean north) {
        UUID participantUniqueId = participant.getUniqueId();
        if (north) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
            participant.lookAt(arena.southSpawn().getX(), arena.southSpawn().getY(), arena.southSpawn().getZ(), LookAnchor.EYES);
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.southSpawn());
            participant.lookAt(arena.northSpawn().getX(), arena.northSpawn().getY(), arena.northSpawn().getZ(), LookAnchor.EYES);
        }
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        topbar.showPlayer(participant, teamId);
        initializeSidebar(participant);
        allParticipants.add(participant);
    }
    
    private void initializeDeadParticipant(Player participant, boolean north) {
        Location lookLocation;
        if (north) {
            northParticipants.add(participant);
            lookLocation = arena.northFlag();
        } else {
            southParticipants.add(participant);
            lookLocation = arena.southFlag();
        }
        
        initializeSidebar(participant);
        allParticipants.add(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.teleport(config.getSpawnObservatory());
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    private void matchIsOver() {
        stop();
        captureTheFlagRound.matchIsOver(this);
    }
    
    public void stop() {
        matchActive = false;
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        northClassPicker.stop(false);
        southClassPicker.stop(false);
        hasNorthFlag = null;
        hasSouthFlag = null;
        resetArena();
        for (Player participant : allParticipants) {
            resetParticipant(participant);
        }
        clearSidebar();
        allParticipants.clear();
        northParticipants.clear();
        southParticipants.clear();
        Bukkit.getLogger().info("Stopping capture the flag match " + matchPairing);
    }
    
    private void resetArena() {
        openGlassBarriers();
        if (northFlagPosition != null) {
            northFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        if (southFlagPosition != null) {
            southFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        // remove items/arrows on the ground
        BoundingBox removeArea = arena.boundingBox();
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
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void onParticipantJoin(Player participant) {
        if (!matchActive) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (matchPairing.northTeam().equals(teamName)) {
            onNorthParticipantJoin(participant);
            return;
        }
        if (matchPairing.southTeam().equals(teamName)) {
            onSouthParticipantJoin(participant);
            return;
        }
    }
    
    /**
     * @param participant the participant
     * @return true if the participant has been in this match and logged out, false if this participant has never been in this match
     */
    private boolean shouldRejoin(Player participant) {
        return participantsAreAlive.containsKey(participant.getUniqueId());
    }
    
    private void onNorthParticipantJoin(Player northParticipant) {
        announceMatchToParticipant(northParticipant, matchPairing.northTeam(), matchPairing.southTeam());
        if (shouldRejoin(northParticipant)) {
            rejoinParticipant(northParticipant, true);
        } else {
            initializeParticipant(northParticipant, true);
        }
        if (northClassPicker.isActive()) {
            northClassPicker.addTeamMate(northParticipant);
            return;
        }
        participantsAreAlive.put(northParticipant.getUniqueId(), false);
        initializeDeadParticipant(northParticipant, true);
    }
    
    private void onSouthParticipantJoin(Player southParticipant) {
        announceMatchToParticipant(southParticipant, matchPairing.southTeam(), matchPairing.northTeam());
        if (shouldRejoin(southParticipant)) {
            rejoinParticipant(southParticipant, false);
        } else {
            initializeParticipant(southParticipant, false);
        }
        if (southClassPicker.isActive()) {
            southClassPicker.addTeamMate(southParticipant);
            return;
        }
        participantsAreAlive.put(southParticipant.getUniqueId(), false);
        initializeDeadParticipant(southParticipant, false);
    }
    
    private void announceMatchToParticipant(Player participant, String team, String oppositeTeam) {
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(team);
        Component oppositeTeamDisplayName = gameManager.getFormattedTeamDisplayName(oppositeTeam);
        participant.sendMessage(Component.empty()
                .append(teamDisplayName)
                .append(Component.text(" is competing against "))
                .append(oppositeTeamDisplayName)
                .append(Component.text("."))
                .color(NamedTextColor.YELLOW));
    }
    
    public void onParticipantQuit(Player participant) {
        if (!matchActive) {
            return;
        }
        if (!allParticipants.contains(participant)) {
            return;
        }
        if (northParticipants.contains(participant)) {
            onNorthParticipantQuit(participant);
            return;
        }
        if (southParticipants.contains(participant)) {
            onSouthParticipantQuit(participant);
            return;
        }
    }
    
    private void onNorthParticipantQuit(Player northParticipant) {
        if (northClassPicker.isActive()) {
            northClassPicker.removeTeamMate(northParticipant);
            resetParticipant(northParticipant);
            allParticipants.remove(northParticipant);
            northParticipants.remove(northParticipant);
            topbar.hidePlayer(northParticipant.getUniqueId());
            return;
        }
        if (!participantsAreAlive.get(northParticipant.getUniqueId())) {
            resetParticipant(northParticipant);
            allParticipants.remove(northParticipant);
            northParticipants.remove(northParticipant);
            topbar.hidePlayer(northParticipant.getUniqueId());
            return;
        }
        Component deathMessage = Component.empty()
                .append(Component.text(northParticipant.getName()))
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(northParticipant, Collections.emptyList(), 0, deathMessage);
        Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
        resetParticipant(northParticipant);
        allParticipants.remove(northParticipant);
        northParticipants.remove(northParticipant);
        topbar.hidePlayer(northParticipant.getUniqueId());
    }
    
    private void onSouthParticipantQuit(Player soutParticipant) {
        if (southClassPicker.isActive()) {
            southClassPicker.removeTeamMate(soutParticipant);
            resetParticipant(soutParticipant);
            allParticipants.remove(soutParticipant);
            southParticipants.remove(soutParticipant);
            topbar.hidePlayer(soutParticipant.getUniqueId());
            return;
        }
        if (!participantsAreAlive.get(soutParticipant.getUniqueId())) {
            resetParticipant(soutParticipant);
            allParticipants.remove(soutParticipant);
            southParticipants.remove(soutParticipant);
            topbar.hidePlayer(soutParticipant.getUniqueId());
            return;
        }
        Component deathMessage = Component.empty()
                .append(Component.text(soutParticipant.getName()))
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(soutParticipant, Collections.emptyList(), 0, deathMessage);
        Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
        resetParticipant(soutParticipant);
        allParticipants.remove(soutParticipant);
        southParticipants.remove(soutParticipant);
        topbar.hidePlayer(soutParticipant.getUniqueId());
    }
    
    private void startMatch() {
        for (Player participants : allParticipants) {
            participants.closeInventory();
        }
        messageAllParticipants(Component.text("Begin!"));
        openGlassBarriers();
        startMatchTimer();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(classSelectionCountdownTaskId);
        Bukkit.getScheduler().cancelTask(matchTimerTaskId);
    }
    
    public void onPlayerDamage(Player participant, EntityDamageEvent event) {
        if (!matchActive) {
            return;
        }
        if (!allParticipants.contains(participant)) {
            return;
        }
        if (participantsAreAlive.get(participant.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!matchActive) {
            return;
        }
        Player killed = event.getPlayer();
        if (!allParticipants.contains(killed)) {
            return;
        }
        killed.getInventory().clear();
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParicipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        if (allParticipantsAreDead()) {
            onBothTeamsLose(Component.text("Both teams are dead."));
        }
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!allParticipants.contains(participant)) {
            return;
        }
        Material itemType = item.getType();
        if (!MaterialUtils.isBanner(itemType)) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Checks if all participants are dead.
     * @return True if all participants are dead, false otherwise
     */
    private boolean allParticipantsAreDead() {
        return !participantsAreAlive.containsValue(true);
    }
    
    private void onParticipantGetKill(Player killed) {
        Player killer = killed.getKiller();
        if (!allParticipants.contains(killer)) {
            return;
        }
        addKill(killer.getUniqueId());
        gameManager.awardPointsToParticipant(killer, config.getKillScore());
    }
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        sidebar.updateLine(
                killerUniqueId,
                "kills",
                ChatColor.RED+"Kills: " + newKillCount
        );
        topbar.setRight(killerUniqueId,
                Component.text("Kills: ")
                .append(Component.text(newKillCount)));
    }
    
    private void onParicipantDeath(Player killed) {
        if (northParticipants.contains(killed)) {
            if (hasSouthFlag(killed)) {
                dropSouthFlag(killed);
            }
        } else if (southParticipants.contains(killed)) {
            if (hasNorthFlag(killed)){
                dropNorthFlag(killed);
            }
        }
        
        participantsAreAlive.put(killed.getUniqueId(), false);
        ParticipantInitializer.resetHealthAndHunger(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        killed.teleport(config.getSpawnObservatory());
        killed.lookAt(arena.northFlag().getX(), arena.northFlag().getY(), arena.northFlag().getZ(), LookAnchor.EYES);
        
        String teamId = gameManager.getTeamName(killed.getUniqueId());
        topbar.addDeath(teamId);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!matchActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!allParticipants.contains(participant)) {
            return;
        }
        if (!participantsAreAlive.get(participant.getUniqueId())) {
            return;
        }
        if (northParticipants.contains(participant)) {
            onNorthParticipantMove(participant);
        } else if (southParticipants.contains(participant)) {
            onSouthParticipantMove(participant);
        }
    }
    
    private void onNorthParticipantMove(Player northParticipant) {
        Location location = northParticipant.getLocation();
        if (canPickUpSouthFlag(location)) {
            pickUpSouthFlag(northParticipant);
            return;
        }
        if (canDeliverSouthFlag(northParticipant)) {
            deliverSouthFlag(northParticipant);
            return;
        }
        if (canRecoverNorthFlag(location)) {
            recoverNorthFlag(northParticipant);
            return;
        }
    }
    
    /**
     * Returns true if the south flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#southFlagPosition}
     * @param location The location to check
     * @return Whether the south flag is dropped and the location is on the south flag
     */
    private boolean canPickUpSouthFlag(Location location) {
        if (southFlagPosition == null) {
            return false;
        }
        return southFlagPosition.getBlockX() == location.getBlockX() && southFlagPosition.getBlockY() == location.getBlockY() && southFlagPosition.getBlockZ() == location.getBlockZ();
    }
    
    private boolean hasSouthFlag(Player northParticipant) {
        return Objects.equals(hasSouthFlag, northParticipant);
    }
    
    private synchronized void pickUpSouthFlag(Player northParticipant) {
        messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was captured"))
                .color(NamedTextColor.DARK_RED));
        messageNorthParticipants(Component.empty()
                .append(Component.text("You captured the flag"))
                .color(NamedTextColor.GREEN));
        northParticipant.getEquipment().setHelmet(new ItemStack(southBanner));
        southFlagPosition.getBlock().setType(Material.AIR);
        southFlagPosition = null;
        hasSouthFlag = northParticipant;
    }
    
    private synchronized void dropSouthFlag(Player northParticipant) {
        messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        messageNorthParticipants(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        northParticipant.getEquipment().setHelmet(null);
        southFlagPosition = BlockPlacementUtils.getBlockDropLocation(northParticipant.getLocation());
        BlockPlacementUtils.placeFlag(southBanner, southFlagPosition, northParticipant.getFacing());
        hasSouthFlag = null;
    }
    
    private boolean canDeliverSouthFlag(Player northParticipant) {
        if (!hasSouthFlag(northParticipant)) {
            return false;
        }
        Location location = northParticipant.getLocation();
        boolean isOnNorthGoal = arena.northFlag().getBlockX() == location.getBlockX() && arena.northFlag().getBlockY() == location.getBlockY() && arena.northFlag().getBlockZ() == location.getBlockZ();
        return isOnNorthGoal;
    }
    
    private void deliverSouthFlag(Player northParticipant) {
        BlockPlacementUtils.placeFlag(southBanner, arena.northFlag(), BlockFace.NORTH);
        northParticipant.getInventory().remove(southBanner);
        onParticipantWin(northParticipant);
    }
    
    private boolean canRecoverSouthFlag(Location location) {
        if (southFlagPosition == null) {
            return false;
        }
        boolean alreadyRecovered = southFlagPosition.getBlockX() == arena.southFlag().getBlockX() && southFlagPosition.getBlockY() == arena.southFlag().getBlockY() && southFlagPosition.getBlockZ() == arena.southFlag().getBlockZ();
        return !alreadyRecovered && canPickUpSouthFlag(location);
    }
    
    private void recoverSouthFlag(Player southParticipant) {
        messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        messageNorthParticipants(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        southFlagPosition.getBlock().setType(Material.AIR);
        southFlagPosition = arena.southFlag();
        BlockPlacementUtils.placeFlag(southBanner, southFlagPosition, BlockFace.NORTH);
    }
    
    private void onSouthParticipantMove(Player southParticipant) {
        Location location = southParticipant.getLocation();
        if (canPickUpNorthFlag(location)) {
            pickUpNorthFlag(southParticipant);
            return;
        }
        if (canDeliverNorthFlag(southParticipant)) {
            deliverNorthFlag(southParticipant);
            return;
        }
        if (canRecoverSouthFlag(location)) {
            recoverSouthFlag(southParticipant);
            return;
        }
    }
    
    /**
     * Returns true if the north flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#northFlagPosition}
     * @param location The location to check
     * @return Whether the north flag is dropped and the location is on the north flag
     */
    private boolean canPickUpNorthFlag(Location location) {
        if (northFlagPosition == null) {
            return false;
        }
        return northFlagPosition.getBlockX() == location.getBlockX() && northFlagPosition.getBlockY() == location.getBlockY() && northFlagPosition.getBlockZ() == location.getBlockZ();
    }
    
    private synchronized void dropNorthFlag(Player southParticipant) {
        messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        messageSouthParticipants(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        southParticipant.getEquipment().setHelmet(null);
        northFlagPosition = BlockPlacementUtils.getBlockDropLocation(southParticipant.getLocation());
        BlockPlacementUtils.placeFlag(northBanner, northFlagPosition, southParticipant.getFacing());
        hasNorthFlag = null;
    }
    
    private boolean hasNorthFlag(Player southParticipant) {
        return Objects.equals(hasNorthFlag, southParticipant);
    }
    
    private synchronized void pickUpNorthFlag(Player southParticipant) {
        messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was captured!"))
                .color(NamedTextColor.DARK_RED));
        messageSouthParticipants(Component.empty()
                .append(Component.text("You captured the flag!"))
                .color(NamedTextColor.GREEN));
        southParticipant.getEquipment().setHelmet(new ItemStack(northBanner));
        northFlagPosition.getBlock().setType(Material.AIR);
        northFlagPosition = null;
        hasNorthFlag = southParticipant;
    }
    
    private boolean canDeliverNorthFlag(Player southParticipant) {
        if (!hasNorthFlag(southParticipant)) {
            return false;
        }
        Location location = southParticipant.getLocation();
        boolean isOnSouthGoal = arena.southFlag().getBlockX() == location.getBlockX() && arena.southFlag().getBlockY() == location.getBlockY() && arena.southFlag().getBlockZ() == location.getBlockZ();
        return isOnSouthGoal;
    }
    
    private void deliverNorthFlag(Player southParticipant) {
        BlockPlacementUtils.placeFlag(northBanner, arena.southFlag(), BlockFace.SOUTH);
        southParticipant.getInventory().remove(northBanner);
        onParticipantWin(southParticipant);
    }
    
    private boolean canRecoverNorthFlag(Location location) {
        if (northFlagPosition == null) {
            return false;
        }
        boolean alreadyRecovered = northFlagPosition.getBlockX() == arena.northFlag().getBlockX() && northFlagPosition.getBlockY() == arena.northFlag().getBlockY() && northFlagPosition.getBlockZ() == arena.northFlag().getBlockZ();
        return !alreadyRecovered && canPickUpNorthFlag(location);
    }
    
    private void recoverNorthFlag(Player northParticipant) {
        messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        messageSouthParticipants(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        northFlagPosition.getBlock().setType(Material.AIR);
        northFlagPosition = arena.northFlag();
        BlockPlacementUtils.placeFlag(northBanner, northFlagPosition, BlockFace.SOUTH);
    }
    
    private void onParticipantWin(Player participant) {
        String winningTeam = gameManager.getTeamName(participant.getUniqueId());
        String losingTeam = matchPairing.northTeam();
        if (winningTeam.equals(matchPairing.northTeam())) {
            losingTeam = matchPairing.southTeam();
        }
        Component winningTeamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        Component losingTeamDisplayName = gameManager.getFormattedTeamDisplayName(losingTeam);
        captureTheFlagRound.messageAllGameParticipants(Component.empty()
                .append(winningTeamDisplayName)
                .append(Component.text(" captured "))
                .append(losingTeamDisplayName)
                .append(Component.text("'s flag!"))
                .color(NamedTextColor.YELLOW));
        gameManager.awardPointsToTeam(winningTeam, config.getWinScore());
        matchIsOver();
    }
    
    private void onBothTeamsLose(Component reason) {
        messageAllParticipants(Component.empty()
                .append(Component.text("Game over. "))
                .append(reason));
        matchIsOver();
    }
    
    public void startClassSelectionPeriod() {
        northClassPicker.start(plugin, northParticipants, config.getLoadouts());
        southClassPicker.start(plugin, southParticipants, config.getLoadouts());
        
        this.classSelectionCountdownTaskId = new BukkitRunnable() {
            private int count = config.getClassSelectionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    northClassPicker.stop(true);
                    southClassPicker.stop(true);
                    startMatch();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Class selection: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                topbar.setMiddle(Component.text(timer));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startMatchTimer() {
        this.matchTimerTaskId = new BukkitRunnable() {
            int count = config.getRoundTimerDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    onBothTeamsLose(Component.text("Time ran out."));
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Round: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                topbar.setMiddle(Component.text(timer));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeSidebar() {
        sidebar.updateLine("timer", "Round: ");
        adminSidebar.updateLine("timer", "Round: ");
        topbar.setMiddle(Component.text("Round: "));
    }
    
    private void initializeSidebar(Player participant) {
        sidebar.updateLine(participant.getUniqueId(),"kills", String.format("%sKills: %s", ChatColor.RED, killCounts.get(participant.getUniqueId())));
        topbar.setRight(participant.getUniqueId(), Component.text("Kills: ")
                .append(Component.text(killCounts.get(participant.getUniqueId()))));
    }
    
    private void clearSidebar() {
        sidebar.updateLine("kills", "");
        topbar.setRight(Component.empty());
        topbar.hideAllPlayers();
        topbar.removeAllTeamPairs();
    }
    
    private void placeFlags() {
        northBanner = gameManager.getTeamBannerColor(matchPairing.northTeam());
        northFlagPosition = arena.northFlag();
        BlockPlacementUtils.placeFlag(northBanner, northFlagPosition, BlockFace.SOUTH);
        
        southBanner = gameManager.getTeamBannerColor(matchPairing.southTeam());
        southFlagPosition = arena.southFlag();
        BlockPlacementUtils.placeFlag(southBanner, southFlagPosition, BlockFace.NORTH);
    }
    
    /**
     * Closes the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void closeGlassBarriers() {
        Arena.BarrierSize size = arena.barrierSize();
        BlockPlacementUtils.createCube(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.GLASS_PANE);
        BlockPlacementUtils.updateDirection(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize());
        BlockPlacementUtils.createCube(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.GLASS_PANE);
        BlockPlacementUtils.updateDirection(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize());
    }
    
    /**
     * Opens the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void openGlassBarriers() {
        Arena.BarrierSize size = arena.barrierSize();
        BlockPlacementUtils.createCube(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.AIR);
        BlockPlacementUtils.createCube(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.AIR);
    }
    
    /**
     * Sets up the team options for the teams in this match
     */
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            if (team.getName().matches(matchPairing.northTeam()) || team.getName().matches(matchPairing.southTeam())) {
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(true);
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
    }
    
    /**
     * Check if the match is active
     * @return True if the match is active, false otherwise
     */
    public boolean isActive() {
        return matchActive;
    }

    /**
     * Checks if either of the teams is still in class selection period
     * @return True if either team is in class selection, 
     * false if no teams are in class selection
     */
    public boolean isInClassSelection() {
        return northClassPicker.isActive() || southClassPicker.isActive();
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : allParticipants) {
            participant.sendMessage(message);
        }
    }
    
    private void messageNorthParticipants(Component message) {
        for (Player participant : northParticipants) {
            participant.sendMessage(message);
        }
    }
    
    private void messageSouthParticipants(Component message) {
        for (Player participant : southParticipants) {
            participant.sendMessage(message);
        }
    }
    
    /**
     * Check if the participant is alive and in the match
     * @param participant The participant
     * @return True if the participant is in this match and is alive, false otherwise
     */
    public boolean isAliveInMatch(Player participant) {
        if (!allParticipants.contains(participant)) {
            return false;
        }
        return participantsAreAlive.get(participant.getUniqueId());
    }
    
    // Test methods
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(matchPairing.northTeam());
        if (northParticipants != null) {
            sb.append(":");
            sb.append(northParticipants.size());
        }
        sb.append(" vs ");
        sb.append(matchPairing.southTeam());
        if (southParticipants != null) {
            sb.append(":");
            sb.append(southParticipants.size());
        }
        return sb.toString();
    }
    
    public ClassPicker getNorthClassPicker() {
        return northClassPicker;
    }
    
    public ClassPicker getSouthClassPicker() {
        return southClassPicker;
    }
    
    /**
     * @return a copy of the south participiants list
     */
    public List<Player> getSouthParticipants() {
        return new ArrayList<>(southParticipants);
    }
    
    /**
     * @return a copy of the north participiants list
     */
    public List<Player> getNorthParticipants() {
        return new ArrayList<>(northParticipants);
    }
    
    /**
     * @return a copy of the allParticipants list
     */
    public List<Player> getAllParticipants() {
        return new ArrayList<>(allParticipants);
    }
    
    public void onClickInventory(Player participant, InventoryClickEvent event) {
        if (!matchActive) {
            return;
        }
        if (!allParticipants.contains(participant)) {
            return;
        }
        if (northParticipants.contains(participant)) {
            if (northClassPicker.isActive()) {
                return; // don't interfere with class picking
            }
        } else { // southParticipants must contain participant
            if (southClassPicker.isActive()) {
                return; // don't interfere with class picking
            }
        }
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
