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
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
public class CaptureTheFlagMatch {
    
    public enum Affiliation {
        NORTH,
        SOUTH
    }
    
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
    private CTFMatchTeam northTeam;
    private CTFMatchTeam southTeam;
    // TODO: replace north and south lists with team.getParticipants() (above)
    private final Map<UUID, CTFMatchParticipant> northParticipants = new HashMap<>();
    private final Map<UUID, CTFMatchParticipant> southParticipants = new HashMap<>();
    private final Map<UUID, CTFMatchParticipant> allParticipants = new HashMap<>();
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
    
    public void start(CTFTeam newNorthTeam, CTFTeam newSouthTeam, Collection<CTFParticipant> newParticipants) {
        placeFlags();
        closeGlassBarriers();
        this.northTeam = new CTFMatchTeam(newNorthTeam, Affiliation.NORTH);
        this.southTeam = new CTFMatchTeam(newSouthTeam, Affiliation.SOUTH);
        for (CTFParticipant participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        setupTeamOptions();
        state = new ClassSelectionState(this);
        Main.logger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    public void initializeParticipant(CTFParticipant newParticipant) {
        initializeParticipant(newParticipant, true);
    }
    
    public void initializeParticipant(CTFParticipant newParticipant, boolean isAlive) {
        Affiliation affiliation = 
                matchPairing.northTeam().equals(newParticipant.getTeamId()) ? 
                        Affiliation.NORTH : Affiliation.SOUTH;
        CTFMatchParticipant participant = new CTFMatchParticipant(newParticipant, affiliation, isAlive);
        allParticipants.put(participant.getUniqueId(), participant);
        int alive;
        int dead;
        if (affiliation == Affiliation.NORTH) {
            northParticipants.put(participant.getUniqueId(), participant);
            northTeam.addParticipant(participant);
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
            southTeam.addParticipant(participant);
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
    
    public void onParticipantJoin(CTFParticipant participant) {
        if (state == null) {
            return;
        }
        state.onParticipantJoin(participant);
    }
    
    public void onParticipantQuit(CTFParticipant participant) {
        if (state == null) {
            return;
        }
        CTFMatchParticipant ctfMatchParticipant = allParticipants.get(participant.getUniqueId());
        if (ctfMatchParticipant == null) {
            return;
        }
        state.onParticipantQuit(ctfMatchParticipant);
    }
    
    public static int countAlive(Collection<CTFMatchParticipant> participants) {
        return (int) participants.stream().filter(CTFMatchParticipant::isAlive).count();
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
        for (org.bukkit.scoreboard.Team team : mctScoreboard.getTeams()) {
            if (team.getName().matches(matchPairing.northTeam()) || team.getName().matches(matchPairing.southTeam())) {
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(true);
                team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                team.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
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
        for (CTFMatchParticipant participant : allParticipants.values()) {
            resetParticipant(participant);
        }
        northTeam = null;
        southTeam = null;
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
    
    public void resetParticipant(CTFMatchParticipant participant) {
        if (participant.getAffiliation() == Affiliation.NORTH) {
            northTeam.removeParticipant(participant.getUniqueId());
        } else {
            southTeam.removeParticipant(participant.getUniqueId());
        }
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
    
    /**
     * @param participant the participant to add a kill to
     */
    public void addKill(@NotNull CTFMatchParticipant participant) {
        int oldKillCount = participant.getKills();
        int newKillCount = oldKillCount + 1;
        participant.setKills(newKillCount);
        parentContext.getParticipants().get(participant.getUniqueId()).setKills(newKillCount);
        topbar.setKills(participant.getUniqueId(), newKillCount);
    }
    
    /**
     * @param participant the participant to add a death to
     */
    public void addDeath(@NotNull CTFMatchParticipant participant) {
        int oldDeathCount = participant.getDeaths();
        int newDeathCount = oldDeathCount + 1;
        participant.setDeaths(newDeathCount);
        parentContext.getParticipants().get(participant.getUniqueId()).setDeaths(newDeathCount);
        topbar.setDeaths(participant.getUniqueId(), newDeathCount);
    }
    
    public void updateScore(CTFMatchTeam team) {
        CTFTeam ctfTeam = parentContext.getTeams().get(team.getTeamId());
        ctfTeam.setScore(team.getScore());
        parentContext.updateScore(ctfTeam);
    }
    
    public void updateScore(CTFMatchParticipant participant) {
        CTFParticipant ctfParticipant = parentContext.getParticipants().get(participant.getUniqueId());
        ctfParticipant.setScore(participant.getScore());
        parentContext.updateScore(ctfParticipant);
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(
                allParticipants.values()
        ).sendMessage(message);
    }
    
}
