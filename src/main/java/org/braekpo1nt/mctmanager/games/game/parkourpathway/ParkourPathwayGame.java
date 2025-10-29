package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.RegularDescriptionState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.ParkourPathwayState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.TeamSpawnsDescription;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class ParkourPathwayGame extends GameBase<ParkourParticipant, ParkourTeam, ParkourParticipant.QuitData, ParkourTeam.QuitData, ParkourPathwayState> {
    
    private final ParkourPathwayConfig config;
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private int statusEffectsTaskId;
    
    public ParkourPathwayGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ParkourPathwayConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(new GameInstanceId(GameType.PARKOUR_PATHWAY, configFile), plugin, gameManager, title, new InitialState());
        this.config = config;
        startStatusEffectsTask();
        addListener(new PreventHungerLoss<>(this));
        addListener(new PreventItemDrop<>(this, true));
        closeGlassBarrier();
        start(newTeams, newParticipants, newAdmins);
    }
    
    private void closeGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.AIR, Material.GLASS);
        BlockPlacementUtils.updateDirection(config.getWorld(), glassBarrier);
    }
    
    public void openGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.GLASS, Material.AIR);
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull ParkourPathwayState getStartState() {
        if (config.getTeamSpawns() == null) {
            return new RegularDescriptionState(this);
        } else {
            return new TeamSpawnsDescription(this, config.getTeamSpawns());
        }
    }
    
    @Override
    protected void cleanup() {
        plugin.getServer().getScheduler().cancelTask(statusEffectsTaskId);
        openGlassBarrier();
    }
    
    @Override
    protected @NotNull ParkourParticipant createParticipant(Participant participant) {
        return new ParkourParticipant(participant, 0);
    }
    
    @Override
    protected @NotNull ParkourParticipant createParticipant(Participant participant, ParkourParticipant.QuitData quitData) {
        return new ParkourParticipant(participant, quitData);
    }
    
    /**
     * Gives the appropriate number of skips to the given participant
     * @param participant the participant to receive skips
     */
    public void giveSkipItem(Participant participant, int numOfSkips) {
        if (numOfSkips <= 0) {
            return;
        }
        participant.getInventory().setItem(8, config.getSkipItem().asQuantity(numOfSkips));
    }
    
    @Override
    protected @NotNull ParkourParticipant.QuitData getQuitData(ParkourParticipant participant) {
        return participant.getQuitData();
    }
    
    /**
     * Checks how many skips the player has, and awards them points for any remaining
     * skips. Removes them from their inventory as well.
     */
    public void awardPointsForUnusedSkips(ParkourParticipant participant) {
        if (config.getUnusedSkipScore() <= 0.0) {
            return;
        }
        if (participant.getUnusedSkips() > 0) {
            participant.sendMessage(Component.empty()
                    .append(Component.text(participant.getUnusedSkips()))
                    .append(Component.text(" unused skips"))
                    .color(NamedTextColor.GREEN));
            this.awardPoints(participant,
                    participant.getUnusedSkips() * config.getUnusedSkipScore());
        }
        participant.setUnusedSkips(0);
        ParticipantInitializer.clearInventory(participant);
    }
    
    @Override
    protected void initializeParticipant(ParkourParticipant participant, ParkourTeam team) {
        giveBoots(participant);
    }
    
    public void giveBoots(ParkourParticipant participant) {
        Color teamColor = teams.get(participant.getTeamId()).getBukkitColor();
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
    }
    
    @Override
    protected void initializeTeam(ParkourTeam team) {
        
    }
    
    @Override
    protected @NotNull ParkourTeam createTeam(Team team) {
        return new ParkourTeam(team, 0);
    }
    
    @Override
    protected @NotNull ParkourTeam createTeam(Team team, ParkourTeam.QuitData quitData) {
        return new ParkourTeam(team, quitData.getScore());
    }
    
    @Override
    protected @NotNull ParkourTeam.QuitData getQuitData(ParkourTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ParkourParticipant participant, ParkourTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ParkourTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("timer", ""),
                new KeyLine("ending", "")
        );
    }
    
    public void updateCheckpointSidebar(ParkourParticipant participant) {
        int lastCheckpoint = config.getPuzzlesSize() - 1;
        sidebar.updateLine(participant.getUniqueId(), "checkpoint",
                Component.empty()
                        .append(Component.text(participant.getCurrentPuzzle()))
                        .append(Component.text("/"))
                        .append(Component.text(lastCheckpoint)));
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("timer", ""),
                new KeyLine("checkpoint", Component.empty()
                        .append(Component.text("0/"))
                        .append(Component.text(config.getPuzzlesSize() - 1))),
                new KeyLine("ending", "")
        );
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
}
