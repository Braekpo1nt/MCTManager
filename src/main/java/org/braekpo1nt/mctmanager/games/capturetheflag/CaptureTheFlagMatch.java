package org.braekpo1nt.mctmanager.games.capturetheflag;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class CaptureTheFlagMatch implements Listener {
    
    private final CaptureTheFlagRound captureTheFlagRound;
    private final Main plugin;
    private final GameManager gameManager;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private final Location spawnObservatory;
    private List<Player> northParticipants;
    private List<Player> southParticipants;
    private List<Player> allParticipants;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<UUID, Integer> killCounts;
    private boolean matchActive = false;
    private int classSelectionCountdownTaskIt;
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
    
    public CaptureTheFlagMatch(CaptureTheFlagRound captureTheFlagRound, Main plugin, GameManager gameManager, MatchPairing matchPairing, Arena arena, Location spawnObservatory) {
        this.captureTheFlagRound = captureTheFlagRound;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.northClassPicker = new ClassPicker();
        this.southClassPicker = new ClassPicker();
        this.spawnObservatory = spawnObservatory;
    }
    
    public MatchPairing getMatchPairing() {
        return matchPairing;
    }
    
    public void start(List<Player> newNorthParticipants, List<Player> newSouthParticipants) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        northParticipants = new ArrayList<>();
        southParticipants = new ArrayList<>();
        allParticipants = new ArrayList<>();
        participantsAreAlive = new HashMap<>();
        killCounts = new HashMap<>();
        String northTeam = gameManager.getTeamName(newNorthParticipants.get(0).getUniqueId());
        String southTeam = gameManager.getTeamName(newSouthParticipants.get(0).getUniqueId());
        placeFlags(northTeam, southTeam);
        closeGlassBarriers();
        for (Player northParticipant : newNorthParticipants) {
            initializeParticipant(northParticipant, true);
        }
        for (Player southParticipant : newSouthParticipants) {
            initializeParticipant(southParticipant, false);
        }
        setupTeamOptions();
        startClassSelectionPeriod();
        matchActive = true;
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
        allParticipants.add(participant);
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
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
        if (northFlagPosition != null) {
            northFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        if (southFlagPosition != null) {
            southFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        for (Player participant : allParticipants) {
            resetParticipant(participant);
        }
        allParticipants.clear();
        northParticipants.clear();
        southParticipants.clear();
        Bukkit.getLogger().info("Stopping capture the flag match " + matchPairing);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
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
        Bukkit.getScheduler().cancelTask(classSelectionCountdownTaskIt);
        Bukkit.getScheduler().cancelTask(matchTimerTaskId);
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
        gameManager.awardPointsToPlayer(killer, 20);
    }
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        gameManager.getFastBoardManager().updateLine(
                killerUniqueId,
                7,
                ChatColor.RED+"Kills: " + newKillCount
        );
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
        killed.teleport(spawnObservatory);
        killed.lookAt(arena.northFlag().getX(), arena.northFlag().getY(), arena.northFlag().getZ(), LookAnchor.EYES);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!matchActive) {
            return;
        }
        Player participant = event.getPlayer();
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
        if (canDeliverSouthFlag(location)) {
            deliverSouthFlag(northParticipant);
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
        Location ground = getSolidBlockBelow(northParticipant.getLocation());
        southFlagPosition = ground.add(0, 1, 0);
        placeFlag(southBanner, southFlagPosition, northParticipant.getFacing());
        hasSouthFlag = null;
    }
    
    /**
     * Gets the first solid block below the given location. If there is no floor all the way to the min height, returns the given location.
     * @param location The location to check below
     * @return the location below the given location that is a solid block. If there are no solid blocks
     */
    private Location getSolidBlockBelow(Location location) {
        Location nonAirLocation = location.subtract(0, 1, 0);
        while (nonAirLocation.getBlockY() > -64) {
            Block block = nonAirLocation.getBlock();
            if (!block.getType().equals(Material.AIR) &&
                !block.getType().equals(Material.CAVE_AIR) &&
                !block.getType().equals(Material.VOID_AIR)) {
                return nonAirLocation;
            }
            nonAirLocation = nonAirLocation.subtract(0, 1, 0);
        }
        return location;
    }
    
    private boolean canDeliverSouthFlag(Location location) {
        return arena.northFlag().getBlockX() == location.getBlockX() && arena.northFlag().getBlockY() == location.getBlockY() && arena.northFlag().getBlockZ() == location.getBlockZ();
    }
    
    private void deliverSouthFlag(Player northParticipant) {
        placeFlag(southBanner, arena.northFlag(), BlockFace.NORTH);
        northParticipant.getInventory().remove(southBanner);
        onParticipantWin(northParticipant);
    }
    
    /**
     * Places the provided flag type at the given location facing the given direction
     * @param flagBlock
     * @param facing
     * @param flagType
     */
    private void placeFlag(Material flagType, Location flagLocation, BlockFace facing) {
        Block flagBlock = flagLocation.getBlock();
        flagBlock.setType(flagType);
        Directional flagData = (Directional) flagBlock.getBlockData();
        flagData.setFacing(facing);
        flagBlock.setBlockData(flagData);
    }
    
    private void onSouthParticipantMove(Player southParticipant) {
        Location location = southParticipant.getLocation();
        if (canPickUpNorthFlag(location)) {
            pickUpNorthFlag(southParticipant);
            return;
        }
        if (canDeliverNorthFlag(location)) {
            deliverNorthFlag(southParticipant);
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
        Location ground = getSolidBlockBelow(southParticipant.getLocation());
        northFlagPosition = ground.add(0, 1, 0);
        placeFlag(northBanner, northFlagPosition, southParticipant.getFacing());
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
    
    private boolean canDeliverNorthFlag(Location location) {
        return arena.southFlag().getBlockX() == location.getBlockX() && arena.southFlag().getBlockY() == location.getBlockY() && arena.southFlag().getBlockZ() == location.getBlockZ();
    }
    
    private void deliverNorthFlag(Player southParticipant) {
        placeFlag(northBanner, arena.southFlag(), BlockFace.SOUTH);
        southParticipant.getInventory().remove(northBanner);
        onParticipantWin(southParticipant);
    }
    
    private void onParticipantWin(Player participant) {
        String team = gameManager.getTeamName(participant.getUniqueId());
        Component displayName = gameManager.getFormattedTeamDisplayName(team);
        messageAllParticipants(Component.empty()
                .append(displayName)
                .append(Component.text(" wins!")));
        gameManager.awardPointsToTeam(team, 100);
        matchIsOver();
    }
    
    private void onBothTeamsLose(Component reason) {
        messageAllParticipants(Component.empty()
                .append(Component.text("Game over. "))
                .append(reason));
        matchIsOver();
    }
    
    
    private void startClassSelectionPeriod() {
        messageAllParticipants(Component.text("Choose your class"));
        northClassPicker.start(plugin, northParticipants);
        southClassPicker.start(plugin, southParticipants);
        
        this.classSelectionCountdownTaskIt = new BukkitRunnable() {
            private int count = 20;
            @Override
            public void run() {
                if (count <= 0) {
                    northClassPicker.stop(true);
                    southClassPicker.stop(true);
                    startMatch();
                    this.cancel();
                    return;
                }
                for (Player participant : allParticipants) {
                    String timeString = TimeStringUtils.getTimeString(count);
                    updateClassSelectionFastBoardTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startMatchTimer() {
        this.matchTimerTaskId = new BukkitRunnable() {
            int count = 3*60;
            @Override
            public void run() {
                if (count <= 0) {
                    onBothTeamsLose(Component.text("Time ran out."));
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : allParticipants) {
                    updateMatchTimer(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeFastBoard(Player participant) {
        String enemyTeam = matchPairing.northTeam();
        if (northParticipants.contains(participant)) {
            enemyTeam = matchPairing.southTeam();
        }
        ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeam);
        String enemyDisplayName = gameManager.getTeamDisplayName(enemyTeam);
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                1,
                "vs: "+enemyColor+enemyDisplayName
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Round:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                "3:00"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                7,
                ChatColor.RED+"Kills: 0"
        );
    }
    
    private void updateClassSelectionFastBoardTimer(Player participant, String timerString) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Class selection:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                timerString
        );
    }
    
    private void updateMatchTimer(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                "Round:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                5,
                timeLeft
        );
    }
    
    private void placeFlags(String northTeam, String southTeam) {
        northBanner = gameManager.getTeamBannerColor(northTeam);
        northFlagPosition = arena.northFlag();
        placeFlag(northBanner, northFlagPosition, BlockFace.SOUTH);
        
        southBanner = gameManager.getTeamBannerColor(southTeam);
        southFlagPosition = arena.southFlag();
        placeFlag(southBanner, southFlagPosition, BlockFace.NORTH);
    }
    
    /**
     * Closes the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void closeGlassBarriers() {
        BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.GLASS_PANE);
        BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.GLASS_PANE);
    }
    
    /**
     * Opens the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void openGlassBarriers() {
        BlockPlacementUtils.createCube(arena.northBarrier(), 5, 4, 1, Material.AIR);
        BlockPlacementUtils.createCube(arena.southBarrier(), 5, 4, 1, Material.AIR);
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
    
    private void messageAllParticipants(Component message) {
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
    
}
