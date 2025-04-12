package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.ColossalCombatState;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.InitialState;
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

public class ColossalCombatGame extends GameBase<ColossalParticipant, ColossalTeam, ColossalParticipant.QuitData, ColossalTeam.QuitData, ColossalCombatState> {
    public ColossalCombatGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ColossalCombatConfig config,
            @NotNull Team newFirst,
            @NotNull Team newSecond,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.FINAL, plugin, gameManager, title, new InitialState());
    }
    
    @Override
    protected @NotNull World getWorld() {
        return null;
    }
    
    @Override
    protected @NotNull ColossalCombatState getStartState() {
        return null;
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant) {
        return null;
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant, ColossalParticipant.QuitData quitData) {
        return null;
    }
    
    @Override
    protected @NotNull ColossalParticipant.QuitData getQuitData(ColossalParticipant participant) {
        return null;
    }
    
    @Override
    protected void initializeParticipant(ColossalParticipant participant, ColossalTeam team) {
        
    }
    
    @Override
    protected void initializeTeam(ColossalTeam team) {
        
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team) {
        return null;
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team, ColossalTeam.QuitData quitData) {
        return null;
    }
    
    @Override
    protected @NotNull ColossalTeam.QuitData getQuitData(ColossalTeam team) {
        return null;
    }
    
    @Override
    protected void resetParticipant(ColossalParticipant participant, ColossalTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ColossalTeam team) {
        
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
        return null;
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return false;
    }
}
