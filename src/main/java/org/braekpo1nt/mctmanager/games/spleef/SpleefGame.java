package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    private final BoundingBox spleefArea = new BoundingBox(-20, 25, -1981, 21, 0, -2021);;

    public SpleefGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.spleefWorld = worldManager.getMVWorld("FT").getCBWorld();
    }

    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>();
        participantsAlive = new HashMap<>();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.spleefStartAnchor = anchorManager.getAnchorLocation("spleef");
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
        participant.setGameMode(GameMode.SURVIVAL);
        clearStatusEffects(participant);
        resetHealthAndHunger(participant);
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
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.LAVA)
        || event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)) {
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
        Iterator<Boolean> isAlives = participantsAlive.values().iterator();
        while (isAlives.hasNext()) {
            boolean isAlive = isAlives.next();
            if (isAlive) {
                aliveCount += 1;
            }
        }
        return aliveCount < 2;
    }

    private void onParticipantDeath(Player killed) {
        participantsAlive.put(killed.getUniqueId(), false);
        for (Player participant : participants) {
            if (!participant.getUniqueId().equals(killed.getUniqueId())) {
                gameManager.awardPointsToPlayer(participant, 10);
            }
        }
    }

    private void startSpleef() {
        placeLayers();
        givePlayersShovels();
        spleefStarted = true;
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
                for (Player participant : participants) {
                    if (count <= 0) {
                        participant.sendMessage(Component.text("Go!"));
                    } else {
                        participant.sendMessage(Component.text(count));
                    }
                }
                if (count <= 0) {
                    startSpleef();
                    this.cancel();
                    return;
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

        layer1.place(new Location(spleefWorld, -15, 21, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer2.place(new Location(spleefWorld, -15, 16, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer3.place(new Location(spleefWorld, -15, 11, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        layer4.place(new Location(spleefWorld, -15, 6, -2015), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameActive) {
            return;
        }
        Block block = event.getBlock();
        if (!isSpleefBlock(block)) {
            return;
        }
        // cancel the event and drop nothing
        event.setCancelled(true);
        block.setType(Material.AIR);
    }

    private boolean isSpleefBlock(Block block) {
        if (spleefArea.contains(block.getLocation().toVector())) {
            if (block.getType().equals(Material.DIRT)) {
                return true;
            }
        }
        return false;
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
                ""
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

    private void clearStatusEffects(Player participant) {
        for (PotionEffect effect : participant.getActivePotionEffects()) {
            participant.removePotionEffect(effect.getType());
        }
    }

    private void resetHealthAndHunger(Player participant) {
        participant.setHealth(participant.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        participant.setFoodLevel(20);
        participant.setSaturation(5);
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
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
}
