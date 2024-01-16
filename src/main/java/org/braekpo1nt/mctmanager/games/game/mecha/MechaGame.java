package org.braekpo1nt.mctmanager.games.game.mecha;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MechaGame implements MCTGame, Configurable, Listener, Headerable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final MechaStorageUtil storageUtil;
    private boolean gameActive = false;
    private boolean mechaHasStarted = false;
    private boolean isInvulnerable = false;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private int startMechaTaskId;
    private int stopMechaCountdownTaskId;
    private int startInvulnerableTaskID;
    private WorldBorder worldBorder;
    private int borderShrinkingTaskId;
    private String lastKilledTeam;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    private final String title = ChatColor.BLUE+"MECHA";
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new MechaStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.MECHA;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        lastKilledTeam = null;
        killCounts = new HashMap<>(newParticipants.size());
        worldBorder = storageUtil.getWorld().getWorldBorder();
        isInvulnerable = false;
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        fillAllChests();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeAndTeleportToPlatforms();
        initializeSidebar();
        setUpTeamOptions();
        initializeWorldBorder();
        startAdmins(newAdmins);
        displayDescription();
        startStartMechaCountdownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started mecha");
    }
    
    private void displayDescription() {
        messageAllParticipants(storageUtil.getDescription());
    }

    private void initializeParticipant(Player participant) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        livingPlayers.add(participantUniqueId);
        killCounts.put(participantUniqueId, 0);
        sidebar.addPlayer(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins = new ArrayList<>(newAdmins.size());
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        initializeAdmin(admin);
        adminSidebar.updateLine(admin.getUniqueId(), "title", title);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    private void initializeAdmin(Player admin) {
        admins.add(admin);
        adminSidebar.addPlayer(admin);
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(storageUtil.getPlatformSpawns().get(0));
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        clearFloorItems();
        clearAllChests();
        clearContainers();
        removePlatforms();
        lastKilledTeam = null;
        worldBorder.reset();
        stopAdmins();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        mechaHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped mecha");
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
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        sidebar.removePlayer(participant.getUniqueId());
    }
    
    private void clearContainers() {
        Bukkit.getLogger().info("Clearing containers");
        List<Chunk> chunks = getChunksInBoundingBox(storageUtil.getWorld(), storageUtil.getRemoveArea());
        int count = 0;
        for (Chunk chunk : chunks) {
            Collection<BlockState> blockStates = chunk.getTileEntities(block -> block.getState() instanceof InventoryHolder, false);
            count += blockStates.size();
            for (BlockState blockState : blockStates) {
                ((InventoryHolder) blockState).getInventory().clear();
            }
        }
        Bukkit.getLogger().info(String.format("%s chunks found, %s InventoryHolders", chunks.size(), count));
    }
    
    public static List<Chunk> getChunksInBoundingBox(World world, BoundingBox boundingBox) {
        List<Chunk> chunksInBoundingBox = new ArrayList<>();
        
        int minX = (int) boundingBox.getMinX();
        int maxX = (int) boundingBox.getMaxX();
        int minZ = (int) boundingBox.getMinZ();
        int maxZ = (int) boundingBox.getMaxZ();
        
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunksInBoundingBox.contains(chunk)) {
                    chunksInBoundingBox.add(chunk);
                }
            }
        }
        
        return chunksInBoundingBox;
    }

    private void clearFloorItems() {
        for (Item item : storageUtil.getWorld().getEntitiesByClass(Item.class)) {
            if (storageUtil.getRemoveArea().contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }

    @Override
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is rejoining MECHA!"))
                    .color(NamedTextColor.YELLOW));
            rejoinParticipant(participant);
        } else {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is joining MECHA!"))
                    .color(NamedTextColor.YELLOW));
            initializeParticipant(participant);
        }
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("title", title),
                new KeyLine("kills", String.format("%sKills: %s", ChatColor.RED, killCounts.get(participant.getUniqueId())))
        );
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined MECHA");
        participants.add(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        sidebar.addPlayer(participant);
    }
    
    /**
     * Checks if the participant was previously in the game, and should thus rejoin
     * @param participant The participant to check
     * @return True if the participant was in the game before, and should rejoin. False
     * if the participant wasn't in the game before. 
     */
    private boolean participantShouldRejoin(Player participant) {
        if (!mechaHasStarted) {
            return false;
        }
        return deadPlayers.contains(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        if (!mechaHasStarted) {
            participants.remove(participant);
            UUID participantUniqueId = participant.getUniqueId();
            livingPlayers.remove(participantUniqueId);
            killCounts.remove(participantUniqueId);
            sidebar.removePlayer(participant);
            return;
        }
        List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                .filter(Objects::nonNull)
                .toList();
        int droppedExp = calculateExpPoints(participant.getLevel());
        Component deathMessage = Component.text(participant.getName())
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, drops, droppedExp, deathMessage);
        Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startMechaTaskId);
        Bukkit.getScheduler().cancelTask(borderShrinkingTaskId);
        Bukkit.getScheduler().cancelTask(stopMechaCountdownTaskId);
        Bukkit.getScheduler().cancelTask(startInvulnerableTaskID);
    }
    
    private void startStartMechaCountdownTask() {
        this.startMechaTaskId = new BukkitRunnable() {
            private int count = storageUtil.getStartDuration();
            
            @Override
            public void run() {
                if (count <= 0) {
                    startMecha();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String message = String.format("Starting: %s", timeLeft);
                sidebar.updateLine("timer", message);
                adminSidebar.updateLine("timer", message);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStopMechaCountdownTask() {
        String endDuration = TimeStringUtils.getTimeString(storageUtil.getEndDuration());
        messageAllParticipants(Component.text("Game ending in ")
                .append(Component.text(endDuration)));
        stopMechaCountdownTaskId = new BukkitRunnable() {
            int count = storageUtil.getEndDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    stop();
                    this.cancel();
                    return;
                }
                if (count <= 3) {
                    messageAllParticipants(Component.text("Game ending in ")
                            .append(Component.text(count)));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void switchPlayerFromLivingToDead(UUID playerUniqueId) {
        livingPlayers.remove(playerUniqueId);
        deadPlayers.add(playerUniqueId);
    }
    
    private void startMecha() {
        this.mechaHasStarted = true;
        kickOffBorderShrinking();
        removePlatforms();
        messageAllParticipants(Component.text("Go!"));
        startInvulnerableTimer();
    }
    
    private void startInvulnerableTimer() {
        isInvulnerable = true;
        String invulnerabilityDuration = TimeStringUtils.getTimeString(storageUtil.getInvulnerabilityDuration());
        messageAllParticipants(Component.text("Invulnerable for ")
                .append(Component.text(invulnerabilityDuration))
                .append(Component.text("!")));
        this.startInvulnerableTaskID = new BukkitRunnable() {
            private int count = storageUtil.getInvulnerabilityDuration();
            
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("invuln", "");
                    adminSidebar.updateLine("invuln", "");
                    isInvulnerable = false;
                    messageAllParticipants(Component.text("Invulnerability has ended!"));
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Invulnerable: %s", timeLeft);
                sidebar.updateLine("invuln", timer);
                adminSidebar.updateLine("invuln", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void onTeamWin(String winningTeamName) {
        Component displayNameComponent = gameManager.getFormattedTeamDisplayName(winningTeamName);
        Bukkit.getServer().sendMessage(Component.text("Team ")
                .append(displayNameComponent)
                .append(Component.text(" wins!")));
        startStopMechaCountdownTask();
    }
    
    private void startSuddenDeath() {
        String message = String.format("%sSudden death", ChatColor.RED);
        sidebar.updateLine("timer", message);
        adminSidebar.updateLine("timer", message);
        messageAllParticipants(Component.text("Sudden death!"));
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (!mechaHasStarted) {
            event.setCancelled(true);
            return;
        }
        if (isInvulnerable) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameActive) {
            return;
        }
        if (!mechaHasStarted) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        dropInventory(killed, event.getDrops());
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        String winningTeam = getWinningTeam();
        if (winningTeam != null) {
            onTeamWin(winningTeam);
        }
    }
    
    
    
    /**
     * Called when:
     * Right-clicking an armor stand
     * Right-clicking an item frame (also; onPlayerInteractEntity() )
     * <p>
     * Not called when:
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!gameActive) {
            return;
        }
        Player clicker = event.getPlayer();
        if (!participants.contains(clicker)) {
            return;
        }
        if (event.getRightClicked() instanceof ArmorStand || event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when:
     * Right-clicking an item frame (also; onPlayerInteractAtEntity() )
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!gameActive) {
            return;
        }
        Player clicker = event.getPlayer();
        if (!participants.contains(clicker)) {
            return;
        }
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when:
     * Left-clicking an armor stand
     * Left-clicking an item frame with an item in it
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Right-clicking an item frame
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!gameActive) {
            return;
        }
        if (event.getDamager() instanceof Player clicker) {
            if (!participants.contains(clicker)) {
                return;
            }
        }
        if (event.getEntity() instanceof ArmorStand || event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when:
     * Left-clicking an item frame without an item in it
     * <p>
     * Not called when:
     * Right-clicking an armor stand
     * Left-clicking an armor stand
     * Right-clicking an item frame
     * Left-clicking an item frame with an item in it
     * Left-clicking an item frame without an item in it
     */
    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!gameActive) {
            return;
        }
        Player clicker = (Player) event.getRemover();
        if (!participants.contains(clicker)) {
            return;
        }
        if (event.getEntity().getType() == EntityType.ITEM_FRAME || event.getEntity().getType() == EntityType.GLOW_ITEM_FRAME) {
            event.setCancelled(true);
        }
    }

    private void onParticipantDeath(Player killed) {
        UUID killedUniqueId = killed.getUniqueId();
        switchPlayerFromLivingToDead(killedUniqueId);
        String teamName = gameManager.getTeamName(killedUniqueId);
        if (teamIsAllDead(teamName)) {
            onTeamDeath(teamName);
        }
        lastKilledTeam = teamName;
    }
    
    private void onTeamDeath(String teamName) {
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" has been eliminated.")));
        for (Player participant : participants) {
            if (livingPlayers.contains(participant.getUniqueId())) {
                gameManager.awardPointsToParticipant(participant, storageUtil.getSurviveTeamScore());
            }
        }
    }
    
    /**
     * Checks if the given team name is all dead
     * @param teamName The team to check
     * @return True if the given team is entirely dead (no living players left), false otherwise
     */
    private boolean teamIsAllDead(String teamName) {
        for (UUID livingPlayerUniqueId : livingPlayers) {
            String livingTeam = gameManager.getTeamName(livingPlayerUniqueId);
            if (teamName.equals(livingTeam)) {
                return false;
            }
        }
        return true;
    }
    
    private int calculateExpPoints(int level) {
        int maxExpPoints = level > 7 ? 100 : level * 7;
        return maxExpPoints / 10;
    }
    
    private void dropInventory(Player killed, List<ItemStack> drops) {
        for (ItemStack item : drops) {
            storageUtil.getWorld().dropItemNaturally(killed.getLocation(), item);
        }
        killed.getInventory().clear();
    }
    
    private void onParticipantGetKill(Player killed) {
        Player killer = killed.getKiller();
        if (killer == null) {
            return;
        }
        if (!participants.contains(killer)) {
            return;
        }
        addKill(killer.getUniqueId());
        gameManager.awardPointsToParticipant(killer, storageUtil.getKillScore());
    }
    
    /**
     * Returns the winning team if there is one
     * @return The team name of the winning team, or null if there is no winning team
     */
    public String getWinningTeam() {
        if (allLivingPlayersAreOnOneTeam()) {
            UUID winningPlayerUniqueId = livingPlayers.get(0);
            return gameManager.getTeamName(winningPlayerUniqueId);
        }
        if (allPlayersAreDead()) {
            return lastKilledTeam;
        }
        return null;
    }
    
    /**
     * Checks if there are no living players anymore
     * @return True if all players are dead, false if not
     */
    private boolean allPlayersAreDead() {
        return livingPlayers.isEmpty();
    }
    
    /**
     * Check if all the living players belong to a single team.
     * @return True if all living players are on a single team. False if there
     * are at least two players alive on different teams, or there are no
     * living players.
     */
    private boolean allLivingPlayersAreOnOneTeam() {
        if (livingPlayers.size() == 0) {
            return false;
        }
        if (livingPlayers.size() == 1) {
            return true;
        }
        UUID firstPlayerUUID = livingPlayers.get(0);
        String firstTeam = gameManager.getTeamName(firstPlayerUUID);
        for (int i = 1; i < livingPlayers.size(); i++) {
            UUID nextPlayerUUID = livingPlayers.get(i);
            String nextTeam = gameManager.getTeamName(nextPlayerUUID);
            if (!nextTeam.equals(firstTeam)) {
                return false;
            }
        }
        return true;
    }
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        sidebar.updateLine(killerUniqueId, "kills", String.format("%sKills: %s", ChatColor.RED, newKillCount));
    }
    
    private void initializeWorldBorder() {
        worldBorder.setCenter(storageUtil.getWorldBorderCenterX(), storageUtil.getWorldBorderCenterZ());
        worldBorder.setSize(storageUtil.getInitialBorderSize());
    }
    
    private void kickOffBorderShrinking() {
        int [] sizes = storageUtil.getSizes();
        int [] delays = storageUtil.getDelays();
        int [] durations = storageUtil.getDurations();
        this.borderShrinkingTaskId = new BukkitRunnable() {
            int delay = 0;
            int duration = 0;
            boolean onDelay = false;
            boolean onDuration = false;
            int stage = 0;
            @Override
            public void run() {
                if (onDelay) {
                    displayBorderDelayFor(delay);
                    if (delay <= 1) {
                        onDelay = false;
                        onDuration = true;
                        duration = durations[stage];
                        int size = sizes[stage];
                        worldBorder.setSize(size, duration);
                        sendBorderShrinkAnnouncement(duration, size);
                        return;
                    }
                    delay--;
                } else if (onDuration) {
                    displayBorderShrinkingFor(duration);
                    if (duration <= 1) {
                        onDuration = false;
                        onDelay = true;
                        stage++;
                        if (stage >= delays.length) {
                            startSuddenDeath();
                            Bukkit.getLogger().info("Border is in final position.");
                            this.cancel();
                            return;
                        }
                        delay = delays[stage];
                        sendBorderDelayAnnouncement(delay);
                        return;
                    }
                    duration--;
                } else {
                    //initialize
                    onDelay = true;
                    delay = delays[0];
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("title", title),
                new KeyLine("timer", ""),
                new KeyLine("invuln", "")
        );
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("personalTeam", ""),
                new KeyLine("personalScore", ""),
                new KeyLine("title", title),
                new KeyLine("kills", ChatColor.RED+"Kills: 0"),
                new KeyLine("timer", ChatColor.LIGHT_PURPLE+"Border: 0:00"),
                new KeyLine("invuln", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    @Override
    public void updateTeamScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalTeam", contents);
    }
    
    @Override
    public void updatePersonalScore(Player participant, String contents) {
        if (sidebar == null) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        sidebar.updateLine(participant.getUniqueId(), "personalScore", contents);
    }
    
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnnouncement(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        messageAllParticipants(Component.text("Border will not shrink for "+timeString));
    }
    
    /**
     * Sends a chat message to all participants saying the border is shrinking
     * @param duration The duration of the shrink in seconds
     * @param size The size of the border in blocks
     */
    private void sendBorderShrinkAnnouncement(int duration, int size) {
        String timeString = TimeStringUtils.getTimeString(duration);
        messageAllParticipants(Component.text("Border shrinking to ")
                .append(Component.text(size))
                .append(Component.text(" for "))
                .append(Component.text(timeString)));
    }
    
    /**
     * Displays the time left for the border shrink to the participants on the FastBoards
     * @param duration The seconds left in the border shrink
     */
    private void displayBorderShrinkingFor(int duration) {
        String timeString = TimeStringUtils.getTimeString(duration);
        String message = String.format("%sShrinking: %s", ChatColor.RED, timeString);
        sidebar.updateLine("timer", message);
        adminSidebar.updateLine("timer", message);
    }
    
    /**
     * Displays the time left till the border shrinks again on the FastBoards
     * @param delay The seconds left till the border shrinks
     */
    private void displayBorderDelayFor(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        String message = String.format("%sBorder: %s", ChatColor.LIGHT_PURPLE, timeString);
        sidebar.updateLine("timer", message);
        adminSidebar.updateLine("timer", message);
    }
    
    /**
     * Places the platforms for the teams, with floors of the concrete colors of the teams. 
     * Only places as many platforms as there are teams. Also teleports participants to the appropriate spawn location. 
     * <p>
     * Note: If there are more teams than there are platforms, will wrap around.
     */
    private void initializeAndTeleportToPlatforms() {
        List<String> teams = gameManager.getTeamNames(participants);
        List<BoundingBox> platformBarriers = storageUtil.getPlatformBarriers();
        List<Location> platformSpawns = storageUtil.getPlatformSpawns();
        Map<String, Location> teamToSpawn = new HashMap<>();
        for (int i = 0; i < teams.size(); i++) {
            int platformIndex = wrapIndex(i, platformBarriers.size());
            BoundingBox barrier = platformBarriers.get(platformIndex);
            BlockPlacementUtils.createHollowCube(storageUtil.getWorld(), barrier, Material.BARRIER);
            String team = teams.get(i);
            Material concreteColor = gameManager.getTeamConcreteColor(team);
            BoundingBox concreteArea = new BoundingBox(
                    barrier.getMinX()+1, 
                    barrier.getMinY(), 
                    barrier.getMinZ()+1, 
                    barrier.getMaxX()-1, 
                    barrier.getMinY(), 
                    barrier.getMaxZ()-1);
            BlockPlacementUtils.createCube(storageUtil.getWorld(), concreteArea, concreteColor);
            double spawnX = barrier.getCenterX() + 0.5;
            double spawnY = concreteArea.getMin().getBlockY() + 1;
            double spawnZ = barrier.getCenterZ() + 0.5;
            Location platformSpawn = platformSpawns.get(platformIndex);
            Location spawnLocation = new Location(storageUtil.getWorld(), spawnX, spawnY, spawnZ, platformSpawn.getYaw(), platformSpawn.getPitch());
            teamToSpawn.put(team, spawnLocation);
        }
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location spawn = teamToSpawn.get(team);
            participant.teleport(spawn);
        }
    }
    
    /**
     * @param index the index to wrap
     * @param size the size to wrap around
     * @return the wrapped version of the index. e.g. if index is 1, and size is 4, returns 1; if index is 6, and size is 4, returns 1;
     */
    private int wrapIndex(int index, int size) {
        Preconditions.checkArgument(size > 0, "size must be greater than 0");
        return (index % size + size) % size; 
    }
    
    private void removePlatforms() {
        List<BoundingBox> platformBarriers = storageUtil.getPlatformBarriers();
        for (BoundingBox barrier : platformBarriers) {
            BlockPlacementUtils.createCube(storageUtil.getWorld(), barrier, Material.AIR);
        }
    }
    
    /**
     * Fill all chests in the mecha world, map chests and spawn chests
     */
    private void fillAllChests() {
        fillSpawnChests();
        fillMapChests();
    }
    
    private void clearAllChests() {
        List<Vector> allChestCoords = new ArrayList<>(storageUtil.getSpawnChestCoords());
        allChestCoords.addAll(storageUtil.getMapChestCoords());
        for (Vector coords : allChestCoords) {
            Block block = storageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    private void fillSpawnChests() {
        for (Vector coords : storageUtil.getSpawnChestCoords()) {
            Block block = storageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.setLootTable(storageUtil.getSpawnLootTable());
            chest.update();
        }
    }
    
    private void fillMapChests() {
        for (Vector coords : storageUtil.getMapChestCoords()) {
            Block block = storageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillMapChest(((Chest) block.getState()));
        }
    }
    
    /**
     * Fills the given chest with a random loot table
     * @param chest The chest to fill
     */
    private void fillMapChest(Chest chest) {
        LootTable lootTable = getRandomLootTable(storageUtil.getWeightedMechaLootTables());
        chest.setLootTable(lootTable);
        chest.update();
    }
    
    /**
     * Gets a random loot table from loots, using the provided weights
     * @return A loot table for a chest. Null if there are zero weightedLootTables passed in
     */
    private @Nullable LootTable getRandomLootTable(Map<LootTable, Integer> weightedLootTables) {
        int totalWeight = 0;
        Collection<Integer> weights = weightedLootTables.values();
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randomIndex = (int) (Math.random() * totalWeight);
        int weightSum = 0;
        for (Map.Entry<LootTable, Integer> entry : weightedLootTables.entrySet()) {
            int weight = entry.getValue();
            weightSum += weight;
            if (randomIndex < weightSum) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
}
