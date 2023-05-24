package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class SpleefGame implements MCTGame, Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private List<Player> participants;
    private final World spleefWorld;
    private Map<UUID, Boolean> participantsAlive;
    private boolean gameActive = false;
    private boolean spleefStarted = false;
    private Location spleefStartAnchor;
    private final PotionEffect SATURATION = new PotionEffect(PotionEffectType.SATURATION, 70, 250, true, false, false);
    private int statusEffectsTaskId;
    private int startCountDownTaskID;
    private final String title = ChatColor.BLUE+"Spleef";
    private final BoundingBox spleefArea = new BoundingBox(-20, 25, -1981, 21, 0, -2021);
    private final List<BoundingBox> layers;
    private int decayTaskId;
    
    public SpleefGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.spleefWorld = worldManager.getMVWorld("FT").getCBWorld();
        this.layers = createLayers();
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.SPLEEF;
    }

    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>();
        participantsAlive = new HashMap<>();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.spleefStartAnchor = anchorManager.getAnchorLocation("spleef");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placeLayers();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        startStatusEffectsTask();
        startStartSpleefCountDownTask();
        setupTeamOptions();
        gameActive = true;
        Bukkit.getLogger().info("Starting Spleef game");
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

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        placeLayers();
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAlive.clear();
        gameActive = false;
        spleefStarted = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Spleef game");
    }

    @Override
    public void onParticipantJoin(Player participant) {
        if (!gameActive) {
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
        if (!gameActive) {
            return false;
        }
        return participantsAlive.containsKey(participant.getUniqueId());
    }

    @Override
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
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
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
        && !cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameActive) {
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
            stop();
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
                gameManager.awardPointsToPlayer(participant, 10);
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
        spleefStarted = true;
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
                            return;
                        }
                        count--;
                    }
                    case TOP_HALF -> {
                        if (count <= 0) {
                            decayStage = DecayStage.BOTTOM_HALF;
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

    private void startStartSpleefCountDownTask() {
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
            layer1.place(new Location(spleefWorld, -23, 33, -2023), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer2 != null) {
            layer2.place(new Location(spleefWorld, -23, 29, -2023), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer3 != null) {
            layer3.place(new Location(spleefWorld, -23, 25, -2023), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (layer4 != null) {
            layer4.place(new Location(spleefWorld, -23, 21, -2023), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameActive) {
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
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                "Starting in",
                ""
        );
    }
    
    private void initializeAliveCountFastBoard(Player participant, String count) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                "Alive:",
                count
        );
    }
    
    private void updateAliveCountFastBoard(Player participant, String count) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                count
        );
    }
    
    private void updateCountDownFastBoard(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
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
        layers.add(new BoundingBox(-23, 33, -2023, 21, 33, -1979));
        layers.add(new BoundingBox(-23, 29, -2023, 21, 29, -1979));
        layers.add(new BoundingBox(-23, 25, -2023, 21, 25, -1979));
        layers.add(new BoundingBox(-23, 21, -2023, 21, 21, -1979));
        return layers;
    }
}
