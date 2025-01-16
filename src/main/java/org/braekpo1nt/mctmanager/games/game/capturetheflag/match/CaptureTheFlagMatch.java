package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import io.papermc.paper.entity.LookAnchor;
import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.*;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.CaptureTheFlagMatchState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.ClassSelectionState;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
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

@Data
public class CaptureTheFlagMatch {
    
    private @Nullable CaptureTheFlagMatchState state;
    
    private final CaptureTheFlagGame parentContext;
    /**
     * To be called when this match is over
     */
    private final Runnable matchIsOver;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private final Main plugin;
    private final GameManager gameManager;
    private final CaptureTheFlagConfig config;
    private final BattleTopbar topbar;
    private final Sidebar adminSidebar;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    private final Map<UUID, Participant> northParticipants = new HashMap<>();
    private final Map<UUID, Participant> southParticipants = new HashMap<>();
    private final Map<UUID, Participant> allParticipants = new HashMap<>();
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
    private Participant hasNorthFlag;
    /**
     * The player who has the south flag, if it is picked up. Null if not.
     */
    private Participant hasSouthFlag;
    private Material northBanner;
    private Material southBanner;
    
    public CaptureTheFlagMatch(CaptureTheFlagGame parentContext, Runnable matchIsOver, MatchPairing matchPairing, Arena arena) {
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
    }
    
    public void nextState() {
        if (state == null) {
            return;
        }
        state.nextState();
    }
    
    public void start(Collection<Participant> newParticipants) {
        placeFlags();
        closeGlassBarriers();
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setupTeamOptions();
        state = new ClassSelectionState(this);
        Main.logger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    public void initializeParticipant(Participant participant) {
        allParticipants.put(participant.getUniqueId(), participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.putIfAbsent(participantUniqueId, true);
        int alive;
        int dead;
        if (matchPairing.northTeam().equals(participant.getTeamId())) {
            northParticipants.put(participant.getUniqueId(), participant);
            participant.teleport(arena.northSpawn());
            participant.setRespawnLocation(arena.northSpawn(), true);
            participant.lookAt(
                    arena.southSpawn().getX(), 
                    arena.southSpawn().getY(), 
                    arena.southSpawn().getZ(), 
                    LookAnchor.EYES);
            alive = countAlive(northParticipants.values());
            dead = northParticipants.size() - alive;
        } else {
            southParticipants.put(participant.getUniqueId(), participant);
            participant.teleport(arena.southSpawn());
            participant.setRespawnLocation(arena.southSpawn(), true);
            participant.lookAt(
                    arena.northSpawn().getX(), 
                    arena.northSpawn().getY(), 
                    arena.northSpawn().getZ(), 
                    LookAnchor.EYES);
            alive = countAlive(southParticipants.values());
            dead = southParticipants.size() - alive;
        }
        topbar.setMembers(participant.getTeamId(), alive, dead);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    public void onParticipantJoin(Participant participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(Participant participant) {
        if (state == null) {
            return;
        }
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onParticipantQuit(participant);
    }
    
    public int countAlive(Collection<Participant> participants) {
        int living = 0;
        for (Participant participant : participants) {
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
        if (state != null) {
            state.stop();
        }
        hasNorthFlag = null;
        hasSouthFlag = null;
        resetArena();
        for (Participant participant : allParticipants.values()) {
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
    
    public void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void onPlayerDamage(EntityDamageEvent event) {
        if (state == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onPlayerDamage(event);
    }
    
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (state == null) {
            return;
        }
        Player participant = (Player) event.getEntity();
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onPlayerLoseHunger(event);
    }
    
    public void onPlayerMove(PlayerMoveEvent event) {
        if (state == null) {
            return;
        }
        Player participant = event.getPlayer();
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onPlayerMove(event);
    }
    
    public void onClickInventory(InventoryClickEvent event) {
        if (state == null) {
            return;
        }
        Player participant = (Player) event.getWhoClicked();
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onClickInventory(event);
    }
    
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (state == null) {
            return;
        }
        Player participant = event.getPlayer();
        if (!allParticipants.containsKey(participant.getUniqueId())) {
            return;
        }
        state.onPlayerDeath(event);
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(
                allParticipants.values()
        ).sendMessage(message);
    }
    
    public void messageNorthParticipants(Component message) {
        Audience.audience(northParticipants.values()).sendMessage(message);
    }
    
    public void titleNorthParticipants(Title title) {
        Audience.audience(northParticipants.values()).showTitle(title);
    }
    
    public void messageSouthParticipants(Component message) {
        Audience.audience(southParticipants.values()).sendMessage(message);
    }
    
    public void titleSouthParticipants(Title title) {
        Audience.audience(southParticipants.values()).showTitle(title);
    }
    
}
