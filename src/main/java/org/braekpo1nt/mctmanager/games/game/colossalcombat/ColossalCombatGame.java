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
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
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
public class ColossalCombatGame extends DuoGameBase<ColossalParticipant, ColossalTeam, ColossalParticipant.QuitData, ColossalTeam.QuitData, ColossalCombatState> {
    private final @NotNull ColossalCombatConfig config;
    
    public ColossalCombatGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ColossalCombatConfig config,
            @NotNull Team newNorth,
            @NotNull Team newSouth,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(
                GameType.FINAL, 
                plugin, 
                gameManager, 
                title, 
                new InitialState(), 
                new ColossalTeam(newNorth, 0, Affiliation.NORTH), 
                new ColossalTeam(newSouth, 0, Affiliation.SOUTH));
        this.config = config;
        start(newTeams, newParticipants, newAdmins);
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
        Affiliation affiliation = getAffiliation(participant.getTeamId());
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
        switch (participant.getAffiliation()) {
            case NORTH -> participant.teleport(config.getNorthSpawn());
            case SOUTH -> participant.teleport(config.getSouthSpawn());
            case SPECTATOR -> participant.teleport(config.getSpectatorSpawn());
        }
    }
    
    @Override
    protected void initializeTeam(ColossalTeam team) {
        
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team) {
        Affiliation affiliation = getAffiliation(team.getTeamId());
        return new ColossalTeam(team, 0, affiliation);
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team, ColossalTeam.QuitData quitData) {
        return new ColossalTeam(team, quitData);
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
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getSpectatorSpawn());
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
        return config.getPreventInteractions().contains(type);
    }
    
    public void giveLoadout(ColossalParticipant participant) {
        participant.getInventory().setContents(config.getLoadout());
        GameManagerUtils.colorLeatherArmor(gameManager, participant);
    }
}
