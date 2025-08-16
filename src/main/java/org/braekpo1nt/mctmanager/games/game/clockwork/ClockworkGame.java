package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.ClockworkState;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@Getter
@Setter
public class ClockworkGame extends GameBase<ClockworkParticipant, ClockworkTeam, ClockworkParticipant.QuitData, ClockworkTeam.QuitData, ClockworkState> {
    
    private final @NotNull ClockworkConfig config;
    private final @NotNull ChaosManager chaosManager;
    private final @NotNull Random random = new Random();
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    
    /**
     * the task id of the status effect loop
     */
    private final int statusEffectTaskId;
    /**
     * True means participants are visible, false means they are invisible
     */
    private boolean participantsVisible;
    private int currentRound;
    /**
     * How many times the clock chimed, determining which wedge is correct
     */
    private int numberOfChimes;
    /**
     * How much time (in ticks) between chimes
     */
    private double chimeInterval;
    
    public ClockworkGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ClockworkConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(new GameInstanceId(GameType.CLOCKWORK, configFile), plugin, gameManager, title, new InitialState());
        this.config = config;
        this.currentRound = 1;
        this.chaosManager = new ChaosManager(plugin, config);
        this.chimeInterval = config.getInitialChimeInterval();
        addListener(new PreventItemDrop<>(this, true));
        addListener(new PreventHungerLoss<>(this));
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        this.participantsVisible = true;
        this.statusEffectTaskId = startStatusEffectTask();
        start(newTeams, newParticipants, newAdmins);
    }
    
    /**
     * Makes participants invisible if {@link #participantsVisible} is true
     * @return the task id of the BukkitRunnable
     */
    private int startStatusEffectTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!participantsVisible) {
                    participants.values().forEach(participant ->
                            participant.addPotionEffect(INVISIBILITY));
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    /**
     * Make participants invisible, and stay invisible until {@link #stopInvisible()} is
     * called
     */
    public void startInvisible() {
        participantsVisible = false;
        participants.values().forEach(participant ->
                participant.addPotionEffect(INVISIBILITY));
    }
    
    /**
     * Make participants visible, and stay visible until {@link #startInvisible()} is 
     * called
     */
    public void stopInvisible() {
        participantsVisible = true;
        participants.values().forEach(participant ->
                participant.removePotionEffect(INVISIBILITY.getType()));
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull ClockworkState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected void cleanup() {
        plugin.getServer().getScheduler().cancelTask(statusEffectTaskId);
        chaosManager.stop();
    }
    
    @Override
    protected @NotNull ClockworkParticipant createParticipant(Participant participant) {
        return new ClockworkParticipant(participant, 0, true);
    }
    
    @Override
    protected @NotNull ClockworkParticipant createParticipant(Participant participant, ClockworkParticipant.QuitData quitData) {
        return new ClockworkParticipant(participant, quitData);
    }
    
    @Override
    protected @NotNull ClockworkParticipant.QuitData getQuitData(ClockworkParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ClockworkParticipant participant, ClockworkTeam team) {
        participant.teleport(config.getStartingLocation());
    }
    
    @Override
    protected void initializeTeam(ClockworkTeam team) {
        
    }
    
    @Override
    protected @NotNull ClockworkTeam createTeam(Team team) {
        return new ClockworkTeam(team, 0);
    }
    
    @Override
    protected @NotNull ClockworkTeam createTeam(Team team, ClockworkTeam.QuitData quitData) {
        return new ClockworkTeam(team, quitData.getScore());
    }
    
    @Override
    protected @NotNull ClockworkTeam.QuitData getQuitData(ClockworkTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ClockworkTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round 1/"))
                        .append(Component.text(config.getRounds()))),
                new KeyLine("playerCount", ""),
                new KeyLine("timer", "")
        );
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round 1/"))
                        .append(Component.text(config.getRounds()))),
                new KeyLine("playerCount", ""),
                new KeyLine("timer", "")
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
    
    public void incrementChaos() {
        chimeInterval -= config.getChimeIntervalDecrement();
        if (chimeInterval < 0) {
            chimeInterval = 0;
        }
        chaosManager.incrementChaos();
    }
    
    public void setTeamOption(@NotNull ClockworkTeam team, @NotNull org.bukkit.scoreboard.Team.Option option, @NotNull org.bukkit.scoreboard.Team.OptionStatus status) {
        org.bukkit.scoreboard.Team scoreboardTeam = gameManager.getMctScoreboard().getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.setOption(option, status);
        } else {
            Main.logger().log(Level.SEVERE, String.format("Could not find scoreboard team with teamId %s", team.getTeamId()), new IllegalStateException("Scoreboard team does not exist"));
        }
    }
}
