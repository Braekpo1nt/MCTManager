package org.braekpo1nt.mctmanager.games.mecha;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.util.*;

public class MechaGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private boolean gameActive = false;
    private List<Player> participants;
    private final World mechaWorld;
    private Map<UUID, FastBoard> boards = new HashMap<>();
    private int startMechaTaskId;
    /**
     * The coordinates of all the chests in the open world, not including spawn chests
     */
    private List<Vector> chestCoords;
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        setChestCoords();
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.mechaWorld = worldManager.getMVWorld("FT").getCBWorld();
    }


    @Override
    public void start(List<Player> participants) {
        this.participants = participants;
        placePlatforms();
        fillChests();
        teleportPlayersToStartingPositions();
        initializeFastboards();
        startStartMechaCountdownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started mecha");
    }

    @Override
    public void stop() {
        hideFastBoards();
        cancelTasks();
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped mecha");
    }
    
    private void cancelTasks() {
        Bukkit.getScheduler().cancelTask(startMechaTaskId);
    }
    
    private void startStartMechaCountdownTask() {
        startMechaTaskId = new BukkitRunnable() {
            int count = 10;
            
            @Override
            public void run() {
                if (count <= 0) {
                    startMecha();
                    this.cancel();
                    return;
                }
                for (Player participant : participants) {
                    participant.sendMessage(Component.text(count));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startMecha() {
        removePlatforms();
        for (Player participant : participants) {
            participant.sendMessage(Component.text("Go!"));
        }
    }
    
    private void initializeFastboards() {
        for (Player participant : participants) {
            FastBoard board = new FastBoard(participant);
            board.updateTitle(ChatColor.BLUE+"MECHA");
            board.updateLines(
                    "",
                    ChatColor.RED+"Kills: 0",
                    "",
                    ChatColor.DARK_PURPLE+"Boarder: 00:00"
            );
            boards.put(participant.getUniqueId(), board);
        }
    }
    
    private void hideFastBoards() {
        for (FastBoard board : boards.values()) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
    }
    
    private void teleportPlayersToStartingPositions() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        Map<String, Location> teamLocations = new HashMap<>();
        teamLocations.put("orange", anchorManager.getAnchorLocation("mecha-orange"));
        teamLocations.put("yellow", anchorManager.getAnchorLocation("mecha-yellow"));
        teamLocations.put("green", anchorManager.getAnchorLocation("mecha-green"));
        teamLocations.put("dark-green", anchorManager.getAnchorLocation("mecha-dark-green"));
        teamLocations.put("cyan", anchorManager.getAnchorLocation("mecha-cyan"));
        teamLocations.put("blue", anchorManager.getAnchorLocation("mecha-blue"));
        teamLocations.put("purple", anchorManager.getAnchorLocation("mecha-purple"));
        teamLocations.put("red", anchorManager.getAnchorLocation("mecha-red"));
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location teamLocation = teamLocations.getOrDefault(team, teamLocations.get("yellow"));
            participant.teleport(teamLocation);
        }
    }
    
    
    
    private void placePlatforms() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "mecha/platforms"));
        structure.place(new Location(this.mechaWorld, -13, -43, -13), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void removePlatforms() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "mecha/platforms_removed"));
        structure.place(new Location(this.mechaWorld, -13, -43, -13), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void fillChests() {
        for (Vector coords : chestCoords) {
            Block block = mechaWorld.getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillChest(((Chest) block.getState()));
        }
    }
    
    private void fillChest(Chest chest) {
        //randomly select a loot table from poor, good, better, excellent
    }
    
    private void setChestCoords() {
        this.chestCoords = new ArrayList<>(62);
        chestCoords.add(new Vector(-18, -45, -15));
        chestCoords.add(new Vector(-10, -37, -17));
        chestCoords.add(new Vector(-10, -31, -18));
        chestCoords.add(new Vector(-15, -28, -28));
        chestCoords.add(new Vector(-13, -28, -28));
        chestCoords.add(new Vector(-13, -34, -36));
        chestCoords.add(new Vector(-21, -34, -30));
        chestCoords.add(new Vector(-21, -40, -27));
        chestCoords.add(new Vector(-23, -45, -33));
        chestCoords.add(new Vector(-23, -45, 20));
        chestCoords.add(new Vector(-25, -44, 9));
        chestCoords.add(new Vector(-10, -45, 41));
        chestCoords.add(new Vector(-26, -44, 52));
        chestCoords.add(new Vector(-10, -38, 43));
        chestCoords.add(new Vector(-22, -30, 56));
        chestCoords.add(new Vector(-9, -31, 34));
        chestCoords.add(new Vector(37, -44, 19));
        chestCoords.add(new Vector(24, -51, 3));
        chestCoords.add(new Vector(38, -51, 23));
        chestCoords.add(new Vector(23, -51, 58));
        chestCoords.add(new Vector(-52, -51, 65));
        chestCoords.add(new Vector(-58, -51, -11));
        chestCoords.add(new Vector(-27, -45, -12));
        chestCoords.add(new Vector(-38, -39, -10));
        chestCoords.add(new Vector(-31, -33, -10));
        chestCoords.add(new Vector(-46, -43, 17));
        chestCoords.add(new Vector(-65, -42, 19));
        chestCoords.add(new Vector(-60, -43, 30));
        chestCoords.add(new Vector(-83, -43, 63));
        chestCoords.add(new Vector(-61, -43, 64));
        chestCoords.add(new Vector(-50, -43, 33));
        chestCoords.add(new Vector(22, -45, -23));
        chestCoords.add(new Vector(16, -45, -10));
        chestCoords.add(new Vector(30, -45, -44));
        chestCoords.add(new Vector(34, -43, -31));
        chestCoords.add(new Vector(22, -37, -45));
        chestCoords.add(new Vector(9, -27, -44));
        chestCoords.add(new Vector(16, -28, -13));
        chestCoords.add(new Vector(22, -40, -70));
        chestCoords.add(new Vector(8, -40, -81));
        chestCoords.add(new Vector(26, -45, 24));
        chestCoords.add(new Vector(-14, -45, -57));
        chestCoords.add(new Vector(-29, -45, -56));
        chestCoords.add(new Vector(-10, -51, -52));
        chestCoords.add(new Vector(-36, -51, -66));
        chestCoords.add(new Vector(-16, -39, -57));
        chestCoords.add(new Vector(-12, -33, -68));
        chestCoords.add(new Vector(-66, -45, -26));
        chestCoords.add(new Vector(-52, -48, -30));
        chestCoords.add(new Vector(-70, -27, -40));
        chestCoords.add(new Vector(-74, -44, -37));
        chestCoords.add(new Vector(-98, -45, -43));
        chestCoords.add(new Vector(-94, -40, -50));
        chestCoords.add(new Vector(-93, -44, -27));
        chestCoords.add(new Vector(-93, -39, -30));
        chestCoords.add(new Vector(-93, -34, -34));
        chestCoords.add(new Vector(-42, -45, -58));
        chestCoords.add(new Vector(-36, -39, -61));
        chestCoords.add(new Vector(-52, -33, -69));
        chestCoords.add(new Vector(-52, -27, -71));
        chestCoords.add(new Vector(-67, -51, -83));
        chestCoords.add(new Vector(-89, -50, -113));
    }
}
