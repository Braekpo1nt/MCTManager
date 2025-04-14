package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.experimental.DuoGameBase;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.ColossalCombatState;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.DescriptionState;
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
import java.util.Objects;

@Getter
@Setter
public class ColossalCombatGame extends DuoGameBase<ColossalParticipant, ColossalTeam, ColossalParticipant.QuitData, ColossalTeam.QuitData, ColossalCombatState> {
    private final @NotNull ColossalCombatConfig config;
    
    public ColossalCombatGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ColossalCombatConfig config,
            @NotNull Team newFirst,
            @NotNull Team newSecond,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(
                GameType.FINAL, 
                plugin, 
                gameManager, 
                title, 
                new InitialState(), 
                new ColossalTeam(newFirst, 0, Affiliation.NORTH), 
                new ColossalTeam(newSecond, 0, Affiliation.SOUTH));
        this.config = config;
        start(newParticipants, newAdmins);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull ColossalCombatState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant) {
        Affiliation affiliation = Objects.requireNonNull(getAffiliation(participant.getTeamId()), "tried to make a ColossalParticipant out of a participant who's not on north or south teams");
        return new ColossalParticipant(participant, 0, affiliation);
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant, ColossalParticipant.QuitData quitData) {
        return new ColossalParticipant(participant, quitData);
    }
    
    @Override
    protected @NotNull ColossalParticipant.QuitData getQuitData(ColossalParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ColossalParticipant participant, ColossalTeam team) {
        if (participant.getAffiliation() == Affiliation.NORTH) {
            participant.teleport(config.getFirstPlaceSpawn());
        } else {
            participant.teleport(config.getSecondPlaceSpawn());
        }
    }
    
    @Override
    protected void initializeTeam(ColossalTeam team) {
        
    }
    
    @Override
    protected @NotNull ColossalTeam.QuitData getQuitData(ColossalTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ColossalParticipant participant, ColossalTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ColossalTeam team) {
        // TODO: set team options
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLine("timer", Component.empty());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLine("timer", Component.empty());
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return false;
    }
}
