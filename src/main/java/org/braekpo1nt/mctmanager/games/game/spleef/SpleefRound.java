package org.braekpo1nt.mctmanager.games.game.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
    private final SpleefStorageUtil spleefStorageUtil;
    private List<Player> participants = new ArrayList<>();
    private final World spleefWorld;
    private Map<UUID, Boolean> participantsAlive;
    private boolean roundActive = false;
    private final SpleefGame spleefGame;
    private final Location spleefStartAnchor;
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private int startCountDownTaskID;
    private final String title = ChatColor.BLUE+"Spleef";
    private final BoundingBox spleefArea = new BoundingBox(-20, 25, -1981, 21, 0, -2021);
    private final List<BoundingBox> layers;
    private int decayTaskId;
    
    public SpleefRound(Main plugin, GameManager gameManager, SpleefGame spleefGame, SpleefStorageUtil spleefStorageUtil) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.spleefGame = spleefGame;
        this.spleefStorageUtil = spleefStorageUtil;
        this.spleefStartAnchor = spleefStorageUtil.getStartingLocation();
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.spleefWorld = spleefStorageUtil.getWorld();
        this.layers = createLayers();
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>();
        participantsAlive = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placeLayers();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setupTeamOptions();
        startStatusEffectsTask();
        startRoundStartingCountDown();
        roundActive = true;
        Bukkit.getLogger().info("Starting Spleef round");
    }
    
    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        participantsAlive.put(participantUniqueId, true);
        initializeFastBoard(participant);
        teleportPlayerToStartingPosition(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined Spleef");
        participants.add(participant);
        initializeFastBoard(participant);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    private void roundIsOver() {
        stop();
        spleefGame.roundIsOver();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        placeLayers();
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAlive.clear();
        roundActive = false;
        Bukkit.getLogger().info("Stopping Spleef round");
    }
    
    public void onParticipantJoin(Player participant) {
        if (!roundActive) {
            return;
        }
        if (participantShouldRejoin(participant)) {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is rejoining Spleef!"))
                    .color(NamedTextColor.YELLOW));
            rejoinParticipant(participant);
            return;
        }
        messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" is joining Spleef!"))
                .color(NamedTextColor.YELLOW));
        initializeParticipant(participant);
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
        if (lessThanTwoPlayersAlive()) {
            roundIsOver();
        }
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
    
    private boolean lessThanTwoPlayersAlive() {
        int aliveCount = 0;
        for (boolean isAlive : participantsAlive.values()) {
            if (isAlive) {
                aliveCount += 1;
            }
        }
        return aliveCount < 2;
    }
    
    private void onParticipantDeath(Player killed) {
        participantsAlive.put(killed.getUniqueId(), false);
        int count = participants.size();
        for (Player participant : participants) {
            if (participantsAlive.get(participant.getUniqueId())) {
                gameManager.awardPointsToParticipant(participant, 10);
            } else {
                count--;
            }
        }
        for (Player participant : participants) {
            updateAliveCountFastBoard(participant, ""+count);
        }
    }
    
    private void startSpleef() {
        placeLayers();
        String count = "" + participants.size();
        for (Player participant : participants) {
            participant.setGameMode(GameMode.SURVIVAL);
            initializeAliveCountFastBoard(participant, count);
        }
        givePlayersShovels();
        startDecayTask();
    }
    
    private void startDecayTask() {
        this.decayTaskId = new BukkitRunnable() {
            private final Random random = new Random();
            private DecayStage decayStage = DecayStage.NONE;
            private int count = 60;
            @Override
            public void run() {
                switch (decayStage) {
                    case NONE -> {
                        if (count <= 0) {
                            count = 60;
                            decayStage = DecayStage.TOP_HALF;
                            messageAllParticipants(Component.text("Top two levels are decaying")
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
                        
                        decayLayer(layers.get(0), 8);
                        decayLayer(layers.get(1), 6);
                    }
                    case BOTTOM_HALF -> {
                        decayLayer(layers.get(0), 8);
                        decayLayer(layers.get(1), 6);
                        decayLayer(layers.get(2), 4);
                        decayLayer(layers.get(3), 2);
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
                            Block block = spleefWorld.getBlockAt(x, y, z);
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
                            Block block = spleefWorld.getBlockAt(x, y, z);
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
            private int count = 10;
            
            @Override
            public void run() {
                if (count <= 0) {
                    startSpleef();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                for (Player participant : participants) {
                    updateCountDownFastBoard(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void placeLayers() {
        Structure layer1 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer1"));
        Structure layer2 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer2"));
        Structure layer3 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer3"));
        Structure layer4 = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "spleef/spleef_layer4"));
        
        if (layer1 != null) {
            layer1.place(new Location(spleefWorld, -22, 37, -2022), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer2 != null) {
            layer2.place(new Location(spleefWorld, -22, 30, -2022), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer3 != null) {
            layer3.place(new Location(spleefWorld, -22, 24, -2022), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer4 != null) {
            layer4.place(new Location(spleefWorld, -22, 19, -2022), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
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
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Starting in"
        );
    }
    
    private void initializeAliveCountFastBoard(Player participant, String count) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Alive:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                count
        );
    }
    
    private void updateAliveCountFastBoard(Player participant, String count) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                count
        );
    }
    
    private void updateCountDownFastBoard(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                timeLeft
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    private void teleportPlayerToStartingPosition(Player player) {
        player.sendMessage("Teleporting to Spleef");
        player.teleport(spleefStartAnchor);
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
    
    private List<BoundingBox> createLayers() {
        List<BoundingBox> layers = new ArrayList<>(4);
        layers.add(new BoundingBox(-22, 37, -2023, 22, 37, -1978));
        layers.add(new BoundingBox(-22, 30, -2023, 22, 30, -1978));
        layers.add(new BoundingBox(-22, 24, -2023, 22, 24, -1978));
        layers.add(new BoundingBox(-22, 19, -2023, 22, 19, -1978));
        return layers;
    }
}
