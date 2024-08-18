package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import io.papermc.paper.entity.LookAnchor;
import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.*;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.CaptureTheFlagMatchState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.ClassSelectionState;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Data
public class CaptureTheFlagMatch {
    
    
    private @Nullable CaptureTheFlagMatchState state;
    
    private final CaptureTheFlagGame parentContext;
    /**
     * To be called when this match is over
     */
    private final Consumer<CaptureTheFlagMatch> matchIsOver;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private final Main plugin;
    private final GameManager gameManager;
    private final CaptureTheFlagConfig config;
    private final BattleTopbar topbar;
    private final Sidebar adminSidebar;
    private final TimerManager timerManager;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    private final List<Player> northParticipants = new ArrayList<>();
    private final List<Player> southParticipants = new ArrayList<>();
    private final List<Player> allParticipants = new ArrayList<>();
    private final Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    /**
     * The position of the north flag, if it has been dropped and can be picked up. Null if not
     */
    private Location northFlagPosition;
    /**
     * The position of the south flag, if it has been dropped and can be picked up. Null if not
     */
    private Location southFlagPosition;
    /**
     * The player who has the north flag, if it is picked up. Null if not.
     */
    private Player hasNorthFlag;
    /**
     * The player who has the south flag, if it is picked up. Null if not.
     */
    private Player hasSouthFlag;
    private Material northBanner;
    private Material southBanner;
    
    public CaptureTheFlagMatch(CaptureTheFlagGame parentContext, Consumer<CaptureTheFlagMatch> matchIsOver, MatchPairing matchPairing, Arena arena) {
        this.parentContext = parentContext;
        this.matchIsOver = matchIsOver;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.plugin = parentContext.getPlugin();
        this.gameManager = parentContext.getGameManager();
        this.config = parentContext.getConfig();
        this.topbar = parentContext.getTopbar();
        this.northClassPicker = new ClassPicker(gameManager);
        this.southClassPicker = new ClassPicker(gameManager);
        this.adminSidebar = parentContext.getAdminSidebar();
        this.timerManager = parentContext.getTimerManager();
    }
    
    public void start(List<Player> newParticipants) {
        placeFlags();
        closeGlassBarriers();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setupTeamOptions();
        state = new ClassSelectionState(this);
        Main.logger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    public void initializeParticipant(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        allParticipants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        int alive = 0;
        int dead = 0;
        if (matchPairing.northTeam().equals(teamId)) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
            participant.setRespawnLocation(arena.northSpawn(), true);
            participant.lookAt(
                    arena.southSpawn().getX(), 
                    arena.southSpawn().getY(), 
                    arena.southSpawn().getZ(), 
                    LookAnchor.EYES);
            alive = countAlive(northParticipants);
            dead = northParticipants.size() - alive;
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.southSpawn());
            participant.setRespawnLocation(arena.southSpawn(), true);
            participant.lookAt(
                    arena.northSpawn().getX(), 
                    arena.northSpawn().getY(), 
                    arena.northSpawn().getZ(), 
                    LookAnchor.EYES);
            alive = countAlive(southParticipants);
            dead = southParticipants.size() - alive;
        }
        topbar.setMembers(teamId, alive, dead);
        allParticipants.add(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    public void onParticipantJoin(Player participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(Player participant) {
        if (state == null) {
            return;
        }
        state.onParticipantQuit(participant);
    }
    
    private int countAlive(List<Player> participants) {
        int living = 0;
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                living++;
            }
        }
        return living;
    }
    
    private void initializeSidebar() {
        adminSidebar.updateLine("timer", "Round: ");
        topbar.setMiddle(Component.empty());
    }
    
    private void placeFlags() {
        northBanner = gameManager.getTeamBannerColor(matchPairing.northTeam());
        northFlagPosition = arena.northFlag();
        BlockPlacementUtils.placeFlag(northBanner, northFlagPosition, BlockFace.SOUTH);
        
        southBanner = gameManager.getTeamBannerColor(matchPairing.southTeam());
        southFlagPosition = arena.southFlag();
        BlockPlacementUtils.placeFlag(southBanner, southFlagPosition, BlockFace.NORTH);
    }
    
    /**
     * Closes the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    private void closeGlassBarriers() {
        Arena.BarrierSize size = arena.barrierSize();
        BlockPlacementUtils.createCube(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.GLASS_PANE);
        BlockPlacementUtils.updateDirection(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize());
        BlockPlacementUtils.createCube(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.GLASS_PANE);
        BlockPlacementUtils.updateDirection(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize());
    }
    
    /**
     * Opens the glass barriers for the {@link CaptureTheFlagMatch#arena}
     */
    public void openGlassBarriers() {
        Arena.BarrierSize size = arena.barrierSize();
        BlockPlacementUtils.createCube(arena.northBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.AIR);
        BlockPlacementUtils.createCube(arena.southBarrier(), size.xSize(), size.ySize(), size.zSize(), Material.AIR);
    }
    
    /**
     * Sets up the team options for the teams in this match
     */
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            if (team.getName().matches(matchPairing.northTeam()) || team.getName().matches(matchPairing.southTeam())) {
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(true);
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
    }
    
    public void stop() {
        if (state == null) {
            return;
        }
        cancelAllTasks();
        // TODO: consider making this (and other certain things) state-dependant stop behaviors
        northClassPicker.stop(false);
        southClassPicker.stop(false);
        hasNorthFlag = null;
        hasSouthFlag = null;
        resetArena();
        for (Player participant : allParticipants) {
            resetParticipant(participant);
        }
        allParticipants.clear();
        northParticipants.clear();
        southParticipants.clear();
        state = null;
        Main.logger().info("Stopping capture the flag match " + matchPairing);
    }
    
    private void resetArena() {
        openGlassBarriers();
        if (northFlagPosition != null) {
            northFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        if (southFlagPosition != null) {
            southFlagPosition.getBlock().setType(Material.AIR);
            northFlagPosition = null;
        }
        // remove items/arrows on the ground
        BoundingBox removeArea = arena.boundingBox();
        for (Arrow arrow : config.getWorld().getEntitiesByClass(Arrow.class)) {
            if (removeArea.contains(arrow.getLocation().toVector())) {
                arrow.remove();
            }
        }
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (removeArea.contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void cancelAllTasks() {
        if (state == null) {
            return;
        }
        state.cancelAllTasks();
    }
    
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (state == null) {
            return;
        }
        state.onPlayerLoseHunger(event);
    }
    
    public void onPlayerMove(PlayerMoveEvent event) {
        if (state == null) {
            return;
        }
        state.onPlayerMove(event);
    }
    
    public void onClickInventory(InventoryClickEvent event) {
        if (state == null) {
            return;
        }
        state.onClickInventory(event);
    }
    
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == null) {
            return;
        }
        state.onPlayerDeath(event);
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(
                allParticipants
        ).sendMessage(message);
    }
}
