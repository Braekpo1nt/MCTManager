package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ColossalColosseumRound implements Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ColossalColosseumGame colossalColosseumGame;
    private final World colossalColosseumWorld;
    private final Location firstPlaceSpawn;
    private final Location secondPlaceSpawn;
    private String firstTeamName;
    private String secondTeamName;
    private Map<UUID, Boolean> firstPlaceParticipantsAlive = new HashMap<>();
    private Map<UUID, Boolean> secondPlaceParticipantsAlive = new HashMap<>();
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private int startCountDownTaskId;
    
    public ColossalColosseumRound(Main plugin, GameManager gameManager, ColossalColosseumGame colossalColosseumGame) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.colossalColosseumGame = colossalColosseumGame;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.colossalColosseumWorld = worldManager.getMVWorld("FT").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.firstPlaceSpawn = anchorManager.getAnchorLocation("cc-first-place-spawn");
        this.secondPlaceSpawn = anchorManager.getAnchorLocation("cc-second-place-spawn");
    }
    
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators) {
        firstTeamName = gameManager.getTeamName(newFirstPlaceParticipants.get(0).getUniqueId());
        secondTeamName = gameManager.getTeamName(newSecondPlaceParticipants.get(0).getUniqueId());
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        firstPlaceParticipantsAlive = new HashMap<>();
        secondPlaceParticipantsAlive = new HashMap<>();
        spectators = new ArrayList<>(newSpectators.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        closeFirstGate();
        closeSecondGate();
        for (Player first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Player second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Player spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        setupTeamOptions();
        startRoundStartingCountDown();
        Bukkit.getLogger().info("Starting Colossal Colosseum round");
    }
    
    private void initializeFirstPlaceParticipant(Player first) {
        firstPlaceParticipants.add(first);
        first.teleport(firstPlaceSpawn);
        firstPlaceParticipantsAlive.put(first.getUniqueId(), true);
        initializeParticipant(first);
    }
    
    private void initializeSecondPlaceParticipant(Player second) {
        secondPlaceParticipants.add(second);
        second.teleport(secondPlaceSpawn);
        secondPlaceParticipantsAlive.put(second.getUniqueId(), true);
        initializeParticipant(second);
    }
    
    private void initializeParticipant(Player participant) {
        initializeFastBoard(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void initializeSpectator(Player spectator) {
        spectators.add(spectator);
        initializeFastBoard(spectator);
        spectator.getInventory().clear();
        spectator.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(spectator);
        ParticipantInitializer.resetHealthAndHunger(spectator);
    }
    
    private void onFirstPlaceTeamWin() {
        stop();
        colossalColosseumGame.onFirstPlaceWinRound();
    }
    
    private void onSecondPlaceTeamWin() {
        stop();
        colossalColosseumGame.onSecondPlaceWinRound();
    }
    
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        closeFirstGate();
        closeSecondGate();
        for (Player participant : firstPlaceParticipants) {
            resetParticipant(participant);
        }
        firstPlaceParticipants.clear();
        for (Player participant : secondPlaceParticipants) {
            resetParticipant(participant);
        }
        secondPlaceParticipants.clear();
        for (Player participant : spectators) {
            resetParticipant(participant);
        }
        spectators.clear();
        Bukkit.getLogger().info("Stopping Colossal Colosseum round");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskId);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        if (!firstPlaceParticipants.contains(killed) && !secondPlaceParticipants.contains(killed)) {
            return;
        }
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
    }
    
    private void onParticipantDeath(Player killed) {
        killed.setGameMode(GameMode.SPECTATOR);
        killed.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        if (firstPlaceParticipants.contains(killed)) {
            firstPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            if (allParticipantsAreDead(firstPlaceParticipantsAlive)) {
                onSecondPlaceTeamWin();
            }
            return;
        }
        if (secondPlaceParticipants.contains(killed)) {
            secondPlaceParticipantsAlive.put(killed.getUniqueId(), false);
            if (allParticipantsAreDead(secondPlaceParticipantsAlive)) {
                onFirstPlaceTeamWin();
            }
        }
    }
    
    /**
     * @param participantsAreAlive the mapping of a participants UUID to their alive status. True is alive, false is dead.
     * @return True if all participants on the given map are dead, false if even one is alive
     */
    private boolean allParticipantsAreDead(Map<UUID, Boolean> participantsAreAlive) {
        for (boolean isAlive : participantsAreAlive.values()) {
            if (isAlive) {
                return false;
            }
        }
        return true;
    }
    
    private void startRound() {
        openGates();
        messageAllParticipants(Component.text("Let it begin!")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD));
        for (Player participant : firstPlaceParticipants) {
            giveParticipantEquipment(participant);
        }
        for (Player participant : secondPlaceParticipants) {
            giveParticipantEquipment(participant);
        }
    }
    
    private void giveParticipantEquipment(Player participant) {
        //stone sword, bow, 16 arrows, leather chest and boots, 16 steak
        participant.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        participant.getInventory().addItem(new ItemStack(Material.BOW));
        participant.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        participant.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }
    
    private void startRoundStartingCountDown() {
        this.startCountDownTaskId = new BukkitRunnable() {
            private int count = 10;
    
            @Override
            public void run() {
                if (count <= 0) {
                    for (Player participant : firstPlaceParticipants) {
                        hideCountDownFastBoard(participant);
                    }
                    for (Player participant : secondPlaceParticipants) {
                        hideCountDownFastBoard(participant);
                    }
                    for (Player participant : spectators) {
                        hideCountDownFastBoard(participant);
                    }
                    startRound();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                for (Player participant : firstPlaceParticipants) {
                    updateCountDownFastBoard(participant, timeLeft);
                }
                for (Player participant : secondPlaceParticipants) {
                    updateCountDownFastBoard(participant, timeLeft);
                }
                for (Player participant : spectators) {
                    updateCountDownFastBoard(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                7,
                ChatColor.BOLD+"Starting:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                8,
                ""
        );
    }
    
    private void updateCountDownFastBoard(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                7,
                ChatColor.BOLD+"Starting:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                8,
                ChatColor.BOLD+timeLeft
        );
    }
    
    private void hideCountDownFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                7,
                ""
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                8,
                ""
        );
    }
    
    private void openGates() {
        //first
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, -19, 5, 1, 1, Material.AIR);
        //second
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, 19, 5, 1, 1, Material.AIR);
    }
    
    private void closeFirstGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, -3, -19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, -19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(firstTeamName);
        BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, 2, -19, 5, 4, 1, Material.AIR, teamPowderColor);
    }
    
    private void closeSecondGate() {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, -3, 19, 5, 10, 1, powderColor, Material.AIR);
        }
        //place stone under
        BlockPlacementUtils.createCube(colossalColosseumWorld, -1002, 1, 19, 5, 1, 1, Material.STONE);
        //place team color sand
        Material teamPowderColor = gameManager.getTeamPowderColor(secondTeamName);
        BlockPlacementUtils.createCubeReplace(colossalColosseumWorld, -1002, 2, 19, 5, 4, 1, Material.AIR, teamPowderColor);
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
    
    private void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : firstPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : secondPlaceParticipants) {
            participant.sendMessage(message);
        }
        for (Player participant : spectators) {
            participant.sendMessage(message);
        }
    }
    
}
