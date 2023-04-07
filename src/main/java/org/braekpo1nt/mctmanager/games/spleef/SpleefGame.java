package org.braekpo1nt.mctmanager.games.spleef;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;

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
        participant.setGameMode(GameMode.ADVENTURE);
        clearStatusEffects(participant);
        resetHealthAndHunger(participant);
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
        gameActive = false;
        spleefStarted = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Spleef game");
    }

    @Override
    public void onParticipantJoin(Player participant) {

    }

    @Override
    public void onParticipantQuit(Player participant) {

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
}
