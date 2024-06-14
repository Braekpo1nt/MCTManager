package org.braekpo1nt.mctmanager.games.game.mecha;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfig;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfigController;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class MechaGame implements MCTGame, Configurable, Listener, Headerable {
    
    private final Main plugin;
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final MechaConfigController configController;
    private MechaConfig config;
    private boolean gameActive = false;
    private boolean mechaHasStarted = false;
    private boolean isInvulnerable = false;
    private List<Player> participants = new ArrayList<>();
    private List<Player> admins = new ArrayList<>();
    private int startMechaTaskId;
    private int gameEndCountdownTaskId;
    private int startInvulnerableTaskID;
    private WorldBorder worldBorder;
    private int borderShrinkingTaskId;
    /**
     * A list of all teams who are alive
     */
    private List<String> livingTeams = new ArrayList<>();;
    private List<UUID> livingPlayers = new ArrayList<>();;
    private List<UUID> deadPlayers = new ArrayList<>();
    private Map<UUID, Integer> killCounts = new HashMap<>();
    private int descriptionPeriodTaskId;
    private boolean descriptionShowing = false;
    private final String title = ChatColor.BLUE+"MECHA";
    /**
     * true when the game is over, and countdown is started, so no points should be awarded
     */
    private boolean gameEndCountDown = false;
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configController = new MechaConfigController(plugin.getDataFolder());
    }
    
    @Override
    public GameType getType() {
        return GameType.MECHA;
    }
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        livingTeams = gameManager.getTeamNames(newParticipants);
        killCounts = new HashMap<>(newParticipants.size());
        worldBorder = config.getWorld().getWorldBorder();
        isInvulnerable = false;
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        fillAllChests();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        List<String> teams = gameManager.getTeamNames(participants);
        createPlatforms(teams);
        teleportTeams(teams);
        initializeSidebar();
        setUpTeamOptions();
        initializeWorldBorder();
        startAdmins(newAdmins);
        displayDescription();
        gameEndCountDown = false;
        gameActive = true;
        startDescriptionPeriod();
        Bukkit.getLogger().info("Started mecha");
    }
    
    private void displayDescription() {
        messageAllParticipants(config.getDescription());
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
        admin.teleport(config.getAdminSpawn());
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        descriptionShowing = false;
        gameEndCountDown = false;
        cancelAllTasks();
        clearFloorItems();
        clearAllChests();
        clearContainers();
        removePlatforms();
        worldBorder.reset();
        stopAdmins();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        livingPlayers.clear();
        deadPlayers.clear();
        livingTeams.clear();
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
        if (!config.shouldClearContainers()) {
            return;
        }
        Bukkit.getLogger().info("Clearing containers");
        List<Chunk> chunks = getChunksInBoundingBox(config.getWorld(), config.getRemoveArea());
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
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (config.getRemoveArea().contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is joining MECHA!"))
                    .color(NamedTextColor.YELLOW));
            initializeParticipant(participant);
            if (!mechaHasStarted) {
                List<String> teams = gameManager.getTeamNames(participants);
                createPlatforms(teams);
                teleportTeams(teams);
            } else {
                participant.teleport(config.getPlatformSpawns().get(0));
            }
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
        Bukkit.getScheduler().cancelTask(gameEndCountdownTaskId);
        Bukkit.getScheduler().cancelTask(startInvulnerableTaskID);
        Bukkit.getScheduler().cancelTask(descriptionPeriodTaskId);
    }
    
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        this.descriptionPeriodTaskId = new BukkitRunnable() {
            private int count = config.getDescriptionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    descriptionShowing = false;
                    startStartMechaCountdownTask();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timerString = String.format("Starting soon: %s", timeLeft);
                sidebar.updateLine("timer", timerString);
                adminSidebar.updateLine("timer", timerString);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStartMechaCountdownTask() {
        this.startMechaTaskId = new BukkitRunnable() {
            private int count = config.getStartDuration();
            
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
    
    /**
     * This adds a slight delay to the end of the match, so that players can have time to revel
     */
    private void startGameEndCountdownTask() {
        gameEndCountDown = true;
        sidebar.addLine("ending", "");
        adminSidebar.addLine("ending", "");
        gameEndCountdownTaskId = new BukkitRunnable() {
            int count = config.getEndDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.deleteLine("ending");
                    adminSidebar.deleteLine("ending");
                    gameEndCountDown = false;
                    stop();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                String timerString = String.format("Game ending: %s", timeString);
                sidebar.updateLine("ending", timerString);
                adminSidebar.updateLine("ending", timerString);
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
        String invulnerabilityDuration = TimeStringUtils.getTimeString(config.getInvulnerabilityDuration());
        messageAllParticipants(Component.text("Invulnerable for ")
                .append(Component.text(invulnerabilityDuration))
                .append(Component.text("!")));
        this.startInvulnerableTaskID = new BukkitRunnable() {
            private int count = config.getInvulnerabilityDuration();
            
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
    
    private void onTeamWin(String winningTeam) {
        Component displayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        plugin.getServer().sendMessage(Component.text("Team ")
                .append(displayName)
                .append(Component.text(" wins!")));
        gameManager.awardPointsToTeam(winningTeam, config.getFirstPlaceScore());
        startGameEndCountdownTask();
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
        if (descriptionShowing) {
            event.setCancelled(true);
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
            plugin.getServer().sendMessage(deathMessage);
        }
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        onParticipantDeath(killed);
    }
    
    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (!gameActive) {
            return;
        }
        if (!config.lockOtherInventories()) {
            return;
        }
        if (!(event.getPlayer() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof BlockInventoryHolder blockInventoryHolder)) {
            return;
        }
        Location location = blockInventoryHolder.getBlock().getLocation();
        Vector pos = location.toVector();
        if (config.getSpawnChestCoords().contains(pos)
                || config.getMapChestCoords().contains(pos)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (!gameActive) {
            return;
        }
        if (!event.getInventory().getType().equals(InventoryType.CHEST)) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Chest chest)) {
            return;
        }
        if (!inventory.isEmpty()) {
            return;
        }
        if (!(event.getPlayer() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (countParticipantViewers(viewers) > 1) {
            return;
        }
        Block block = chest.getBlock();
        Vector chestPos = block.getLocation().toVector();
        if (!config.getSpawnChestCoords().contains(chestPos) 
                && !config.getMapChestCoords().contains(chestPos)) {
            return;
        }
        block.setType(Material.AIR);
    }
    
    private int countParticipantViewers(List<HumanEntity> viewers) {
        int count = 0;
        for (HumanEntity viewer : viewers) {
            if (viewer instanceof Player participant) {
                if (participants.contains(participant)) {
                    if (livingPlayers.contains(participant.getUniqueId())) {
                        count++;
                    }
                }
            }
        }
        return count;
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
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameActive) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        Material blockType = clickedBlock.getType();
        if (!config.getPreventInteractions().contains(blockType)) {
            return;
        }
        event.setCancelled(true);
    }

    private void onParticipantDeath(Player killed) {
        UUID killedUniqueId = killed.getUniqueId();
        switchPlayerFromLivingToDead(killedUniqueId);
        String teamName = gameManager.getTeamName(killedUniqueId);
        if (teamIsAllDead(teamName)) {
            onTeamDeath(teamName);
        }
    }
    
    /**
     * Call when all of a team's members are dead. 
     * @param deadTeam the team who just died
     */
    private void onTeamDeath(String deadTeam) {
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(deadTeam);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" has been eliminated.")));
        livingTeams.remove(deadTeam);
        if (gameEndCountDown) {
            // points shouldn't be awarded and a team has already won if this is true
            return;
        }
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (livingPlayers.contains(participant.getUniqueId()) && !deadTeam.equals(team)) {
                gameManager.awardPointsToParticipant(participant, config.getSurviveTeamScore());
            }
        }
        Component displayName = gameManager.getFormattedTeamDisplayName(deadTeam);
        switch (livingTeams.size()) {
            case 2 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(displayName)
                        .append(Component.text(" got third place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getThirdPlaceScore());
            }
            case 1 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(displayName)
                        .append(Component.text(" got second place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getSecondPlaceScore());
                onTeamWin(livingTeams.get(0));
            }
            case 0 -> {
                // this is a provision for when there is only 1 team at the beginning, for testing purposes
                onTeamWin(deadTeam);
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
            config.getWorld().dropItemNaturally(killed.getLocation(), item);
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
        gameManager.awardPointsToParticipant(killer, config.getKillScore());
    }
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        sidebar.updateLine(killerUniqueId, "kills", String.format("%sKills: %s", ChatColor.RED, newKillCount));
    }
    
    private void initializeWorldBorder() {
        worldBorder.setCenter(config.getWorldBorderCenterX(), config.getWorldBorderCenterZ());
        worldBorder.setSize(config.getInitialBorderSize());
        worldBorder.setDamageAmount(config.getWorldBorderDamageAmount());
        worldBorder.setDamageBuffer(config.getWorldBorderDamageBuffer());
        worldBorder.setWarningDistance(config.getWorldBorderWarningDistance());
        worldBorder.setWarningTime(config.getWorldBorderWarningTime());
    }
    
    private void kickOffBorderShrinking() {
        int [] sizes = config.getSizes();
        int [] delays = config.getDelays();
        int [] durations = config.getDurations();
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
     * Creates platforms for teams to spawn on made of a hollow rectangle of Barrier blocks where the bottom layer is Concrete that matches the color of the team
     * <br>
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - place n platforms, but no more than m platforms
     * @param teams the teams that will be teleported
     */
    private void createPlatforms(List<String> teams) {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        World world = config.getWorld();
        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            int platformIndex = MathUtils.wrapIndex(i, platformBarriers.size());
            BoundingBox barrierArea = platformBarriers.get(platformIndex);
            BoundingBox concreteArea = new BoundingBox(
                    barrierArea.getMinX()+1,
                    barrierArea.getMinY(),
                    barrierArea.getMinZ()+1,
                    barrierArea.getMaxX()-1,
                    barrierArea.getMinY(),
                    barrierArea.getMaxZ()-1);
            Material concreteColor = gameManager.getTeamConcreteColor(team);
            BlockPlacementUtils.createHollowCube(world, barrierArea, Material.BARRIER);
            BlockPlacementUtils.createCube(world, concreteArea, concreteColor);
        }
    }
    
    /**
     * For n teams and m platforms in storageUtil.getPlatformBarriers():<br>
     * - teleport teams to their designated platforms. If n is greater than m, then it will start wrapping around and teleporting different teams to the same platforms, until all teams have a platform. 
     * @param teams the teams to teleport (players will be selected from the participants list)
     */
    private void teleportTeams(List<String> teams) {
        List<Location> platformSpawns = config.getPlatformSpawns();
        Map<String, Location> teamSpawnLocations = new HashMap<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            String team = teams.get(i);
            int platformIndex = MathUtils.wrapIndex(i, platformSpawns.size());
            Location platformSpawn = platformSpawns.get(platformIndex);
            teamSpawnLocations.put(team, platformSpawn);
        }
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location spawn = teamSpawnLocations.get(team);
            participant.teleport(spawn);
        }
    }
    
    private void removePlatforms() {
        List<BoundingBox> platformBarriers = config.getPlatformBarriers();
        for (BoundingBox barrier : platformBarriers) {
            BlockPlacementUtils.createCube(config.getWorld(), barrier, Material.AIR);
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
        List<Vector> allChestCoords = new ArrayList<>(config.getSpawnChestCoords());
        allChestCoords.addAll(config.getMapChestCoords());
        for (Vector coords : allChestCoords) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    private void fillSpawnChests() {
        for (Vector coords : config.getSpawnChestCoords()) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.setLootTable(config.getSpawnLootTable());
            chest.update();
        }
    }
    
    private void fillMapChests() {
        for (Vector coords : config.getMapChestCoords()) {
            Block block = config.getWorld().getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillMapChest(((Chest) block.getState()));
        }
    }
    
    /**
     * Fills the given chest with a random loot table
     * @param chest The chest to fill
     */
    private void fillMapChest(Chest chest) {
        LootTable lootTable = MathUtils.getWeightedRandomValue(config.getWeightedMechaLootTables());
        chest.setLootTable(lootTable);
        chest.update();
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
}
