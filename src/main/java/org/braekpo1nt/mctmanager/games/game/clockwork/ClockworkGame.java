package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.ClockworkState;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.clockwork.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
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
    
    private int currentRound;
    private long chimeInterval;
    
    public ClockworkGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ClockworkConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.CLOCKWORK, plugin, gameManager, title, new InitialState());
        this.config = config;
        this.chaosManager = new ChaosManager(plugin, config);
        start(newTeams, newParticipants, newAdmins);
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
        
    }
    
    @Override
    protected ClockworkParticipant createParticipant(Participant participant) {
        return new ClockworkParticipant(participant, 0, true);
    }
    
    @Override
    protected ClockworkParticipant createParticipant(Participant participant, ClockworkParticipant.QuitData quitData) {
        return new ClockworkParticipant(participant, quitData);
    }
    
    @Override
    protected ClockworkParticipant.QuitData getQuitData(ClockworkParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    protected void initializeTeam(ClockworkTeam team) {
        
    }
    
    @Override
    protected ClockworkTeam createTeam(Team team) {
        return new ClockworkTeam(team, 0);
    }
    
    @Override
    protected ClockworkTeam createTeam(Team team, ClockworkTeam.QuitData quitData) {
        return new ClockworkTeam(team, quitData.getScore());
    }
    
    @Override
    protected ClockworkTeam.QuitData getQuitData(ClockworkTeam team) {
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
    
    public void setTeamOption(@NotNull ClockworkTeam team, @NotNull org.bukkit.scoreboard.Team.Option option, @NotNull org.bukkit.scoreboard.Team.OptionStatus status) {
        org.bukkit.scoreboard.Team scoreboardTeam = gameManager.getMctScoreboard().getTeam(team.getTeamId());
        if (scoreboardTeam != null) {
            scoreboardTeam.setOption(option, status);
        } else {
            Main.logger().log(Level.SEVERE, String.format("Could not find scoreboard team with teamId %s", team.getTeamId()), new IllegalStateException("Scoreboard team does not exist"));
        }
    }
}
