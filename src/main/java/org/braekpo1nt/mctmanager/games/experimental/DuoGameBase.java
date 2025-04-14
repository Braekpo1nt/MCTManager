package org.braekpo1nt.mctmanager.games.experimental;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public abstract class DuoGameBase<P extends ParticipantData, T extends ScoredTeamData<P> & Affiliated, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>> extends GameBase<P, T, QP, QT, S> {
    
    protected final @NotNull T northTeam;
    protected final @NotNull T southTeam;
    
    public DuoGameBase(
            @NotNull GameType type,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState,
            @NotNull T northTeam,
            @NotNull T southTeam) {
        super(type, plugin, gameManager, title, initialState);
        if (northTeam.getAffiliation() != Affiliation.NORTH) {
            throw new IllegalArgumentException("northTeam.getAffiliation() must be NORTH");
        }
        if (southTeam.getAffiliation() != Affiliation.SOUTH) {
            throw new IllegalArgumentException("southTeam.getAffiliation() must be SOUTH");
        }
        this.northTeam = northTeam;
        this.southTeam = southTeam;
    }
    
    public @NotNull Affiliation getAffiliation(@NotNull String teamId) {
        if (northTeam.getTeamId().equals(teamId)) {
            return Affiliation.NORTH;
        } else if (southTeam.getTeamId().equals(teamId)) {
            return Affiliation.SOUTH;
        }
        return Affiliation.SPECTATOR;
    }
    
    /**
     * 
     * @param newParticipants the newParticipants
     * @param newAdmins the newAdmins
     */
    @Override
    protected void start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        teams.put(northTeam.getTeamId(), northTeam);
        setupTeamOptions(northTeam);
        tabList.addTeam(northTeam.getTeamId(), northTeam.getDisplayName(), northTeam.getColor());
        initializeTeam(northTeam);
        
        teams.put(southTeam.getTeamId(), southTeam);
        setupTeamOptions(southTeam);
        tabList.addTeam(southTeam.getTeamId(), southTeam.getDisplayName(), southTeam.getColor());
        initializeTeam(southTeam);
        
        List<Team> spectatorTeams = newTeams.stream().filter(team -> {
            String teamId = team.getTeamId();
            return !teamId.equals(northTeam.getTeamId()) && !teamId.equals(southTeam.getTeamId());
        }).toList();
        super.start(spectatorTeams, newParticipants, newAdmins);
    }
    
    @Override
    public void onTeamJoin(Team newTeam) {
        if (teams.containsKey(newTeam.getTeamId())) {
            return;
        }
        Affiliation affiliation = getAffiliation(newTeam.getTeamId());
        switch (affiliation) {
            case NORTH -> {
                teams.put(northTeam.getTeamId(), northTeam);
                setupTeamOptions(northTeam);
                state.onTeamRejoin(northTeam);
            }
            case SOUTH -> {
                teams.put(southTeam.getTeamId(), southTeam);
                setupTeamOptions(southTeam);
                state.onTeamRejoin(southTeam);
            }
            case SPECTATOR -> {
                super.onTeamJoin(newTeam);
            }
        }
    }
    
    /**
     * QuitData is not stored for the north or south teams when they quit.
     * Otherwise, this is the same as {@link super#onTeamQuit(String)}
     * @param teamId the teamId that quit
     */
    @Override
    public void onTeamQuit(@NotNull String teamId) {
        T team = teams.get(teamId);
        if (team == null || team.size() > 0) {
            return;
        }
        Affiliation affiliation = getAffiliation(teamId);
        switch (affiliation) {
            case NORTH -> {
                state.onTeamQuit(northTeam);
                teams.remove(team.getTeamId());
            }
            case SOUTH -> {
                state.onTeamQuit(southTeam);
                teams.remove(team.getTeamId());
            }
            case SPECTATOR -> {
                super.onTeamQuit(teamId);
            }
        }
    }
    
    
}
