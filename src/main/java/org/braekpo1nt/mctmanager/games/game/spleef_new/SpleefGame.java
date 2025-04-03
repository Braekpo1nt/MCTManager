package org.braekpo1nt.mctmanager.games.game.spleef_new;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.experimental.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.experimental.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef_new.state.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.spleef_new.state.InitialState;
import org.braekpo1nt.mctmanager.games.game.spleef_new.state.SpleefState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class SpleefGame extends GameBase<SpleefParticipant, SpleefTeam, SpleefParticipant.QuitData, SpleefTeam.QuitData, SpleefState> {
    
    private final @NotNull SpleefConfig config;
    
    public SpleefGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull SpleefConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.SPLEEF, plugin, gameManager, title, new InitialState());
        this.config = config;
        addListener(new PreventHungerLoss<>(this));
        addListener(new PreventItemDrop<>(this, true));
        start(newTeams, newParticipants, newAdmins);
    }
    
    @Override
    protected @NotNull SpleefState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected SpleefParticipant createParticipant(Participant newParticipant) {
        return null;
    }
    
    @Override
    protected SpleefParticipant createParticipant(Participant participant, SpleefParticipant.QuitData quitData) {
        return null;
    }
    
    @Override
    protected SpleefParticipant.QuitData getQuitData(SpleefParticipant participant) {
        return null;
    }
    
    @Override
    protected void initializeParticipant(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    protected void initializeTeam(SpleefTeam team) {
        
    }
    
    @Override
    protected SpleefTeam createTeam(Team team) {
        return null;
    }
    
    @Override
    protected SpleefTeam createTeam(Team team, SpleefTeam.QuitData quitData) {
        return null;
    }
    
    @Override
    protected SpleefTeam.QuitData getQuitData(SpleefTeam team) {
        return null;
    }
    
    @Override
    protected void resetParticipant(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLine("round", Component.empty());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        adminSidebar.addLine("round", Component.empty());
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return null;
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
}
