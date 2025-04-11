package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import io.papermc.paper.entity.LookAnchor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.*;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.CaptureTheFlagMatchState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states.ClassSelectionState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.CaptureTheFlagState;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class CaptureTheFlagMatch implements CaptureTheFlagState {
    
    public enum Affiliation {
        NORTH,
        SOUTH
    }
    
    private @NotNull CaptureTheFlagMatchState state;
    
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
    private final @NotNull CTFMatchTeam northTeam;
    private final @NotNull CTFMatchTeam southTeam;
    private final Map<UUID, CTFMatchParticipant> participants = new HashMap<>();
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
    
    public CaptureTheFlagMatch(
            CaptureTheFlagGame parentContext, 
            Runnable matchIsOver, 
            MatchPairing matchPairing, 
            Arena arena,
            @NotNull CTFTeam newNorthTeam, 
            @NotNull CTFTeam newSouthTeam, 
            Collection<CTFParticipant> newParticipants) {
        this.parentContext = parentContext;
        this.matchIsOver = matchIsOver;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.plugin = parentContext.getPlugin();
        this.gameManager = parentContext.getGameManager();
        this.config = parentContext.getConfig();
        this.topbar = parentContext.getTopbar();
        this.adminSidebar = parentContext.getAdminSidebar();
        this.northTeam = new CTFMatchTeam(newNorthTeam, Affiliation.NORTH);
        this.southTeam = new CTFMatchTeam(newSouthTeam, Affiliation.SOUTH);
        placeFlags();
        closeGlassBarriers();
        start(newParticipants);
    }
    
    public void nextState() {
        state.nextState();
    }
    
    private void start(Collection<CTFParticipant> newParticipants) {
        for (CTFParticipant newParticipant : newParticipants) {
            CTFMatchParticipant participant = createParticipant(newParticipant);
            CTFMatchTeam team = getTeam(participant.getAffiliation());
            addParticipant(participant, team);
            participant.setGameMode(GameMode.ADVENTURE);
            ParticipantInitializer.clearStatusEffects(participant);
            ParticipantInitializer.clearInventory(participant);
            ParticipantInitializer.resetHealthAndHunger(participant);
            initializeParticipant(participant);
        }
        initializeSidebar();
        updateAliveStatus(Affiliation.NORTH);
        updateAliveStatus(Affiliation.SOUTH);
        state = new ClassSelectionState(this);
        Main.logger().info(String.format("Starting capture the flag match %s", matchPairing));
    }
    
    protected void addParticipant(CTFMatchParticipant participant, CTFMatchTeam team) {
        participants.put(participant.getUniqueId(), participant);
        team.addParticipant(participant);
    }
    
    private CTFMatchParticipant createParticipant(CTFParticipant newParticipant) {
        Affiliation affiliation = Objects.requireNonNull(getAffiliation(newParticipant.getTeamId()), "tried to create CTFMatchParticipant from participant whose team is not in match");
        return new CTFMatchParticipant(newParticipant, affiliation, true);
    }
    
    protected void initializeParticipant(CTFMatchParticipant participant) {
        if (participant.getAffiliation() == Affiliation.NORTH) {
            participant.teleport(arena.northSpawn());
            participant.lookAt(
                    arena.southSpawn().getX(), 
                    arena.southSpawn().getY(), 
                    arena.southSpawn().getZ(), 
                    LookAnchor.EYES);
        } else {
            participant.teleport(arena.southSpawn());
            participant.lookAt(
                    arena.northSpawn().getX(), 
                    arena.northSpawn().getY(), 
                    arena.northSpawn().getZ(), 
                    LookAnchor.EYES);
        }
    }
    
    public void updateAliveStatus(Affiliation affiliation) {
        if (affiliation == Affiliation.NORTH) {
            int alive = northTeam.getAlive();
            int dead = northTeam.size() - alive;
            topbar.setMembers(matchPairing.northTeam(), alive, dead);
        } else {
            int alive = southTeam.getAlive();
            int dead = southTeam.size() - alive;
            topbar.setMembers(matchPairing.southTeam(), alive, dead);
        }
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
    
    @Override
    public void cleanup() {
        hasNorthFlag = null;
        hasSouthFlag = null;
        resetArena();
        for (CTFMatchParticipant participant : participants.values()) {
            resetParticipant(participant);
        }
        participants.clear();
        Main.logger().info("Stopping capture the flag match " + matchPairing);
    }
    
    private @Nullable CTFMatchParticipant getParticipant(CTFParticipant participant) {
        return participants.get(participant.getUniqueId());
    }
    
    private @NotNull CTFMatchTeam getTeam(CTFTeam team) {
        return getTeam(getAffiliation(team.getTeamId()));
    }
    
    private @NotNull CTFMatchTeam getTeam(Affiliation affiliation) {
        return affiliation == Affiliation.NORTH ? northTeam : southTeam;
    }
    
    /**
     * @param teamId the teamId
     * @return {@link Affiliation#NORTH} if teamId is the north team, {@link Affiliation#SOUTH} otherwise
     */
    private @NotNull Affiliation getAffiliation(@NotNull String teamId) {
        if (matchPairing.northTeam().equals(teamId)) {
            return Affiliation.NORTH;
        }
        return Affiliation.SOUTH;
    }
    
    @Override
    public void onTeamRejoin(CTFTeam team) {
        CTFMatchTeam matchTeam = getTeam(team);
        state.onTeamRejoin(matchTeam);
    }
    
    @Override
    public void onNewTeamJoin(CTFTeam team) {
        // not applicable
    }
    
    @Override
    public void onParticipantRejoin(CTFParticipant participant, CTFTeam team) {
        CTFMatchParticipant matchParticipant = createParticipant(participant);
        CTFMatchTeam matchTeam = getTeam(team);
        addParticipant(matchParticipant, matchTeam);
        state.onParticipantRejoin(matchParticipant, matchTeam);
    }
    
    @Override
    public void onNewParticipantJoin(CTFParticipant participant, CTFTeam team) {
        CTFMatchParticipant matchParticipant = createParticipant(participant);
        CTFMatchTeam matchTeam = getTeam(team);
        addParticipant(matchParticipant, matchTeam);
        state.onNewParticipantJoin(matchParticipant, matchTeam);
    }
    
    @Override
    public void onParticipantQuit(CTFParticipant participant, CTFTeam team) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        CTFMatchTeam matchTeam = getTeam(team);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantQuit(matchParticipant, matchTeam);
        UUID participantUUID = matchParticipant.getUniqueId();
        participants.remove(participantUUID);
        team.removeParticipant(participantUUID);
        resetParticipant(matchParticipant);
    }
    
    @Override
    public void onTeamQuit(CTFTeam team) {
        CTFMatchTeam matchTeam = getTeam(team);
        state.onTeamQuit(matchTeam);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantMove(event, matchParticipant);
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantTeleport(event, matchParticipant);
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantInteract(event, matchParticipant);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantDamage(event, matchParticipant);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantDeath(event, matchParticipant);
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantRespawn(event, matchParticipant);
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantFoodLevelChange(event, matchParticipant);
    }
    
    @Override
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFParticipant participant) {
        CTFMatchParticipant matchParticipant = getParticipant(participant);
        if (matchParticipant == null) {
            return;
        }
        state.onParticipantClickInventory(event, matchParticipant);
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
        ParticipantInitializer.clearInventory(participant);
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
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
    
    public void awardPoints(CTFMatchParticipant participant, int points) {
        CTFParticipant ctfParticipant = parentContext.getParticipants().get(participant.getUniqueId());
        parentContext.awardPoints(ctfParticipant, points);
        participant.setScore(ctfParticipant.getScore());
        int newScore = parentContext.getTeams().get(participant.getTeamId()).getScore();
        if (participant.getAffiliation() == Affiliation.NORTH) {
            northTeam.setScore(newScore);
        } else {
            southTeam.setScore(newScore);
        }
    }
    
    public void awardPoints(CTFMatchTeam team, int points) {
        CTFTeam ctfTeam = parentContext.getTeams().get(team.getTeamId());
        parentContext.awardPoints(ctfTeam, points);
        team.setScore(ctfTeam.getScore());
    }
    
    public void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        Audience.audience(
                participants.values()
        ).sendMessage(message);
    }
    
}
