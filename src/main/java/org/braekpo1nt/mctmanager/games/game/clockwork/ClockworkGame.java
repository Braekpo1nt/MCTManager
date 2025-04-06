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
import org.braekpo1nt.mctmanager.games.game.clockwork.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ClockworkGame extends GameBase<ClockworkParticipant, ClockworkTeam, ClockworkParticipant.QuitData, ClockworkTeam.QuitData, ClockworkState> {
    
    @NotNull
    private final ClockworkConfig config;
    
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
        start(newTeams, newParticipants, newAdmins);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull ClockworkState getStartState() {
        return null;
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected ClockworkParticipant createParticipant(Participant participant) {
        return null;
    }
    
    @Override
    protected ClockworkParticipant createParticipant(Participant participant, ClockworkParticipant.QuitData quitData) {
        return null;
    }
    
    @Override
    protected ClockworkParticipant.QuitData getQuitData(ClockworkParticipant participant) {
        return null;
    }
    
    @Override
    protected void initializeParticipant(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    protected void initializeTeam(ClockworkTeam team) {
        
    }
    
    @Override
    protected ClockworkTeam createTeam(Team team) {
        return null;
    }
    
    @Override
    protected ClockworkTeam createTeam(Team team, ClockworkTeam.QuitData quitData) {
        return null;
    }
    
    @Override
    protected ClockworkTeam.QuitData getQuitData(ClockworkTeam team) {
        return null;
    }
    
    @Override
    protected void resetParticipant(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ClockworkTeam team) {
        
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeAdminSidebar() {
        
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        
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
