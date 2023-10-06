package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class SpleefRound implements Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final SpleefStorageUtil storageUtil;
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAlive;
    private boolean spleefHasStarted = false;
    private boolean roundActive = false;
    private final SpleefGame spleefGame;
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private int startCountDownTaskID;
    private int decayTaskId;
    
    public SpleefRound(Main plugin, GameManager gameManager, SpleefGame spleefGame, SpleefStorageUtil spleefStorageUtil, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.spleefGame = spleefGame;
        this.storageUtil = spleefStorageUtil;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        participantsAlive = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placeLayers();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        initializeAdminSidebar();
        setupTeamOptions();
        startStatusEffectsTask();
        startRoundStartingCountDown();
        spleefHasStarted = false;
        roundActive = true;
        Bukkit.getLogger().info("Starting Spleef round");
    }
    
    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        participantsAlive.put(participantUniqueId, true);
        teleportPlayerToRandomStartingPosition(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined Spleef");
        participants.add(participant);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void roundIsOver() {
        stop();
        spleefGame.roundIsOver();
    }
    
    public void stop() {
        spleefHasStarted = false;
        roundActive = false;
        HandlerList.unregisterAll(this);
        placeLayers();
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        clearAdminSidebar();
        participants.clear();
        participantsAlive.clear();
        Bukkit.getLogger().info("Stopping Spleef round");
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void onParticipantJoin(Player participant) {
        if (!roundActive) {
            return;
        }
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is rejoining Spleef!"))
                    .color(NamedTextColor.YELLOW));
        } else {
            initializeParticipant(participant);
            if (spleefHasStarted) {
                giveParticipantShovel(participant);
                participant.setGameMode(GameMode.SURVIVAL);
            }
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is joining Spleef!"))
                    .color(NamedTextColor.YELLOW));
        }
        long aliveCount = participantsAlive.values().stream().filter((alive) -> alive).count();
        String alive = String.format("Alive: %s", aliveCount);
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
    }
    
    private boolean participantShouldRejoin(Player participant) {
        if (!roundActive) {
            return false;
        }
        return participantsAlive.containsKey(participant.getUniqueId());
    }
    
    public void onParticipantQuit(Player participant) {
        if (!roundActive) {
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
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!roundActive) {
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
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
            event.setCancelled(true);
            return;
        }
        if (!spleefHasStarted) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!roundActive) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        killed.getInventory().clear();
        event.setCancelled(true);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (lessThanTwoPlayersAlive() || exactlyOneTeamIsAlive()) {
            roundIsOver();
        }
    }
    
    /**
     * @return true if exactly one team is alive, false otherwise
     */
    private boolean exactlyOneTeamIsAlive() {
        String onlyTeam = null;
        for (Player participant : participants) {
            if (participantsAlive.get(participant.getUniqueId())) {
                String livingTeam = gameManager.getTeamName(participant.getUniqueId());
                if (onlyTeam == null) {
                    onlyTeam = livingTeam;
                } else if (!onlyTeam.equals(livingTeam)) {
                    return false;
                }
            }
        }
        return onlyTeam != null;
    }
    
    private boolean lessThanTwoPlayersAlive() {
        int aliveCount = 0;
        for (boolean isAlive : participantsAlive.values()) {
            if (isAlive) {
                aliveCount += 1;
            }
        }
        return aliveCount < 2;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!roundActive) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_SHOVEL) {
            if (event.getClickedBlock() != null) {
                Material clickedBlockType = event.getClickedBlock().getType();
                if (clickedBlockType == Material.DIRT || clickedBlockType == Material.COARSE_DIRT) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private void onParticipantDeath(Player killed) {
        participantsAlive.put(killed.getUniqueId(), false);
        String killedTeam = gameManager.getTeamName(killed.getUniqueId());
        int count = participants.size();
        for (Player participant : participants) {
            if (participantsAlive.get(participant.getUniqueId())) {
                String teamName = gameManager.getTeamName(participant.getUniqueId());
                if (!teamName.equals(killedTeam)) {
                    gameManager.awardPointsToParticipant(participant, storageUtil.getSurviveScore());
                }
            } else {
                count--;
            }
        }
        String alive = String.format("Alive: %s", count);
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
    }
    
    private void startSpleef() {
        placeLayers();
        String alive = String.format("Alive: %s", participants.size());
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        givePlayersShovels();
        for (Player participant : participants) {
            participant.setGameMode(GameMode.SURVIVAL);
        }
        spleefHasStarted = true;
        startDecayTask();
    }
    
    private void startDecayTask() {
        int halfLayerCount = storageUtil.getDecayLayers().size() / 2;
        this.decayTaskId = new BukkitRunnable() {
            private final Random random = new Random();
            private DecayStage decayStage = DecayStage.NONE;
            private int count = storageUtil.getDecayTopLayersDuration();
            @Override
            public void run() {
                switch (decayStage) {
                    case NONE -> {
                        if (count <= 0) {
                            count = storageUtil.getDecayBottomLayersDuration();
                            decayStage = DecayStage.TOP_HALF;
                            messageAllParticipants(Component.text("The first ")
                                    .append(Component.text(halfLayerCount))
                                    .append(Component.text(" layers are decaying"))
                                    .color(NamedTextColor.YELLOW));
                            return;
                        }
                        count--;
                    }
                    case TOP_HALF -> {
                        if (count <= 0) {
                            decayStage = DecayStage.BOTTOM_HALF;
                            messageAllParticipants(Component.text("All levels are decaying")
                                    .color(NamedTextColor.YELLOW));
                            return;
                        }
                        count--;
                        
                        for (int i = 0; i < halfLayerCount; i++) {
                            decayLayer(storageUtil.getDecayLayers().get(i), storageUtil.getDecayRates().get(i));
                        }
                    }
                    case BOTTOM_HALF -> {
                        for (int i = 0; i < storageUtil.getDecayLayers().size(); i++) {
                            decayLayer(storageUtil.getDecayLayers().get(i), storageUtil.getDecayRates().get(i));
                        }
                    }
                }
            }
            
            private void decayLayer(BoundingBox layer1, int blocks) {
                List<Block> coarseDirtBlocks = getCoarseDirtBlocks(layer1);
                List<Block> dirtBlocks = getDirtBlocks(layer1);
                
                // Decay coarse dirt blocks to air
                if (!coarseDirtBlocks.isEmpty()) {
                    for (int i = 0; i < blocks; i++) {
                        Block randomCoarseDirtBlock = coarseDirtBlocks.get(random.nextInt(coarseDirtBlocks.size()));
                        randomCoarseDirtBlock.setType(Material.AIR);
                    }
                }
                
                // Decay dirt blocks to coarse dirt
                if (!dirtBlocks.isEmpty()) {
                    for (int i = 0; i < blocks; i++) {
                        Block randomDirtBlock = dirtBlocks.get(random.nextInt(dirtBlocks.size()));
                        randomDirtBlock.setType(Material.COARSE_DIRT);
                    }
                }
            }
            
            private List<Block> getDirtBlocks(BoundingBox layer) {
                List<Block> dirtBlocks = new ArrayList<>();
                
                for (int x = layer.getMin().getBlockX(); x <= layer.getMaxX(); x++) {
                    for (int y = layer.getMin().getBlockY(); y <= layer.getMaxY(); y++) {
                        for (int z = layer.getMin().getBlockZ(); z <= layer.getMaxZ(); z++) {
                            Block block = storageUtil.getWorld().getBlockAt(x, y, z);
                            if (block.getType() == Material.DIRT) {
                                dirtBlocks.add(block);
                            }
                        }
                    }
                }
                
                return dirtBlocks;
            }
            
            private List<Block> getCoarseDirtBlocks(BoundingBox layer) {
                List<Block> coarseDirtBlocks = new ArrayList<>();
                
                for (int x = layer.getMin().getBlockX(); x <= layer.getMaxX(); x++) {
                    for (int y = layer.getMin().getBlockY(); y <= layer.getMaxY(); y++) {
                        for (int z = layer.getMin().getBlockZ(); z <= layer.getMaxZ(); z++) {
                            Block block = storageUtil.getWorld().getBlockAt(x, y, z);
                            if (block.getType() == Material.COARSE_DIRT) {
                                coarseDirtBlocks.add(block);
                            }
                        }
                    }
                }
                
                return coarseDirtBlocks;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void givePlayersShovels() {
        for (Player participant : participants) {
            giveParticipantShovel(participant);
        }
    }
    
    private void giveParticipantShovel(Player participant) {
        ItemStack diamondShovel = new ItemStack(Material.DIAMOND_SHOVEL);
        diamondShovel.addEnchantment(Enchantment.DIG_SPEED, 5);
        diamondShovel.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        participant.getInventory().addItem(diamondShovel);
    }
    
    private void startRoundStartingCountDown() {
        this.startCountDownTaskID = new BukkitRunnable() {
            private int count = storageUtil.getRoundStartingDuration();
            
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    startSpleef();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Starting in: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void placeLayers() {
        for (int i = 0; i < storageUtil.getStructures().size(); i++) {
            Structure layer = storageUtil.getStructures().get(i);
            layer.place(storageUtil.getStructureOrigins().get(i), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!roundActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        Block block = event.getBlock();
        Material type = block.getType();
        if (!type.equals(Material.DIRT) && !type.equals(Material.COARSE_DIRT)) {
            return;
        }
        event.setDropItems(false);
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Player participant : participants) {
                    participant.addPotionEffect(SATURATION);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
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
    
    private void initializeAdminSidebar() {
        adminSidebar.addLine("alive", "Alive: ");
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteLine("alive");
    }
    
    private void initializeSidebar() {
        sidebar.addLine("alive", "Alive: ");
    }
    
    private void clearSidebar() {
        sidebar.deleteLine("alive");
    }
    
    private void teleportPlayerToRandomStartingPosition(Player player) {
        player.sendMessage("Teleporting to Spleef");
        int index = new Random().nextInt(storageUtil.getStartingLocations().size());
        player.teleport(storageUtil.getStartingLocations().get(index));
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(decayTaskId);
    }
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private int calculateExpPoints(int level) {
        int maxExpPoints = level > 7 ? 100 : level * 7;
        return maxExpPoints / 10;
    }
}
