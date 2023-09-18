package org.braekpo1nt.mctmanager.games.game.mecha;

import com.onarandombox.MultiverseCore.utils.AnchorManager;
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
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MechaGame implements MCTGame, Configurable, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final MechaStorageUtil mechaStorageUtil;
    private boolean gameActive = false;
    private boolean mechaHasStarted = false;
    private List<Player> participants;
    private int startMechaTaskId;
    private int stopMechaCountdownTaskId;
    private WorldBorder worldBorder;
    private int borderShrinkingTaskId;
    private String lastKilledTeam;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    private final String title = ChatColor.BLUE+"MECHA";
    private Map<String, Location> teamLocations;
    private PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 200, true, false, true);
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.mechaStorageUtil = new MechaStorageUtil(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.MECHA;
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return mechaStorageUtil.loadConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        lastKilledTeam = null;
        killCounts = new HashMap<>(newParticipants.size());
        worldBorder = mechaStorageUtil.getWorld().getWorldBorder();
        resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, mechaStorageUtil.getInvulnerabilityDuration(), 200, true, false, true);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placePlatforms();
        fillAllChests();
        initializeTeamLocations();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setUpTeamOptions();
        initializeWorldBorder();
        startStartMechaCountdownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started mecha");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        livingPlayers.add(participantUniqueId);
        killCounts.put(participantUniqueId, 0);
        teleportParticipantToStartingPosition(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        clearFloorItems();
        placePlatforms();
        clearAllChests();
        clearContainers();
        lastKilledTeam = null;
        worldBorder.reset();
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
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    private void clearContainers() {
        Bukkit.getLogger().info("Clearing containers");
        List<Chunk> chunks = getChunksInBoundingBox(mechaStorageUtil.getWorld(), mechaStorageUtil.getRemoveArea());
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
        for (Item item : mechaStorageUtil.getWorld().getEntitiesByClass(Item.class)) {
            if (mechaStorageUtil.getRemoveArea().contains(item.getLocation().toVector())) {
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
            return;
        }
        messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" is joining MECHA!"))
                .color(NamedTextColor.YELLOW));
        initializeParticipant(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined MECHA");
        participants.add(participant);
        participant.setGameMode(GameMode.SPECTATOR);
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
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startMechaTaskId);
        Bukkit.getScheduler().cancelTask(borderShrinkingTaskId);
        Bukkit.getScheduler().cancelTask(stopMechaCountdownTaskId);
    }
    
    private void startStartMechaCountdownTask() {
        this.startMechaTaskId = new BukkitRunnable() {
            private int count = mechaStorageUtil.getStartDuration();
            
            @Override
            public void run() {
                if (count <= 0) {
                    startMecha();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                gameManager.getSidebarManager().updateLine("timer", String.format("Starting: %s", timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStopMechaCountdownTask() {
        String endDuration = TimeStringUtils.getTimeString(mechaStorageUtil.getEndDuration());
        messageAllParticipants(Component.text("Game ending in ")
                .append(Component.text(endDuration)));
        stopMechaCountdownTaskId = new BukkitRunnable() {
            int count = mechaStorageUtil.getEndDuration();
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
        giveInvulnerabilityForTenSeconds();
    }
    
    private void giveInvulnerabilityForTenSeconds() {
        for (Player participant : participants) {
            participant.addPotionEffect(resistance);
        }
        String invulnerabilityDuration = TimeStringUtils.getTimeString(mechaStorageUtil.getInvulnerabilityDuration());
        messageAllParticipants(Component.text("Invulnerable for ")
                .append(Component.text(invulnerabilityDuration))
                .append(Component.text("!")));
    }
    
    private void onTeamWin(String winningTeamName) {
        Component displayNameComponent = gameManager.getFormattedTeamDisplayName(winningTeamName);
        Bukkit.getServer().sendMessage(Component.text("Team ")
                .append(displayNameComponent)
                .append(Component.text(" wins!")));
        startStopMechaCountdownTask();
    }
    
    private void startSuddenDeath() {
        gameManager.getSidebarManager().updateLine("timer", String.format("%sSudden death", ChatColor.RED));
        messageAllParticipants(Component.text("Sudden death!"));
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
                gameManager.awardPointsToParticipant(participant, mechaStorageUtil.getSurviveTeamScore());
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
            mechaStorageUtil.getWorld().dropItemNaturally(killed.getLocation(), item);
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
        gameManager.awardPointsToParticipant(killer, mechaStorageUtil.getKillScore());
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
        gameManager.getSidebarManager().updateLine("kills", String.format("%sKills: %s", ChatColor.RED, newKillCount));
    }
    
    private void initializeWorldBorder() {
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(mechaStorageUtil.getInitialBorderSize());
    }
    
    private void kickOffBorderShrinking() {
        int [] sizes = mechaStorageUtil.getSizes();
        int [] delays = mechaStorageUtil.getDelays();
        int [] durations = mechaStorageUtil.getDurations();
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
                        sendBorderDelayAnouncement(delay);
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
    
    private void initializeSidebar() {
        gameManager.getSidebarManager().addLines(
                new KeyLine("title", title), // 0
                new KeyLine("kills", ChatColor.RED+"Kills: 0"), // 2
                new KeyLine("timer", ChatColor.LIGHT_PURPLE+"Border: 0:00") // 4, 5
        );
    }
    
    private void clearSidebar() {
        gameManager.getSidebarManager().deleteLines("title", "kills", "timer");
    }
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnouncement(int delay) {
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
        gameManager.getSidebarManager().updateLine("timer", String.format("%sShrinking: %s", ChatColor.RED, timeString));
    }
    
    /**
     * Displays the time left till the border shrinks again on the FastBoards
     * @param delay The seconds left till the border shrinks
     */
    private void displayBorderDelayFor(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        gameManager.getSidebarManager().updateLine("timer", String.format("%sBorder: %s", ChatColor.LIGHT_PURPLE, timeString));
    }
    
    private void teleportParticipantToStartingPosition(Player participant) {
        String team = gameManager.getTeamName(participant.getUniqueId());
        Location teamLocation = teamLocations.getOrDefault(team, teamLocations.get("yellow"));
        participant.teleport(teamLocation);
    }
    
    private void placePlatforms() {
        mechaStorageUtil.getPlatformStructure().place(mechaStorageUtil.getPlatformsOrigin(), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void removePlatforms() {
        mechaStorageUtil.getPlatformRemovedStructure().place(mechaStorageUtil.getPlatformsOrigin(), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    /**
     * Fill all chests in the mecha world, map chests and spawn chests
     */
    private void fillAllChests() {
        fillSpawnChests();
        fillMapChests();
    }
    
    private void clearAllChests() {
        List<Vector> allChestCoords = new ArrayList<>(mechaStorageUtil.getSpawnChestCoords());
        allChestCoords.addAll(mechaStorageUtil.getMapChestCoords());
        for (Vector coords : allChestCoords) {
            Block block = mechaStorageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    private void fillSpawnChests() {
        for (Vector coords : mechaStorageUtil.getSpawnChestCoords()) {
            Block block = mechaStorageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.setLootTable(mechaStorageUtil.getSpawnLootTable());
            chest.update();
        }
    }
    
    private void fillMapChests() {
        for (Vector coords : mechaStorageUtil.getSpawnChestCoords()) {
            Block block = mechaStorageUtil.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillMapChest(((Chest) block.getState()));
        }
    }
    
    /**
     * Fills the given chest with a random loot table
     * @param chest The chest to fill
     */
    private void fillMapChest(Chest chest) {
        LootTable lootTable = getRandomLootTable(mechaStorageUtil.getWeightedMechaLootTables());
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
    
    private void initializeTeamLocations() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        teamLocations = new HashMap<>();
        teamLocations.put("orange", anchorManager.getAnchorLocation("mecha-orange"));
        teamLocations.put("yellow", anchorManager.getAnchorLocation("mecha-yellow"));
        teamLocations.put("green", anchorManager.getAnchorLocation("mecha-green"));
        teamLocations.put("dark-green", anchorManager.getAnchorLocation("mecha-dark-green"));
        teamLocations.put("cyan", anchorManager.getAnchorLocation("mecha-cyan"));
        teamLocations.put("blue", anchorManager.getAnchorLocation("mecha-blue"));
        teamLocations.put("purple", anchorManager.getAnchorLocation("mecha-purple"));
        teamLocations.put("red", anchorManager.getAnchorLocation("mecha-red"));
    }
    
}
