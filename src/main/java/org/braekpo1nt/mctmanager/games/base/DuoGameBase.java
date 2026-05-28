package org.braekpo1nt.mctmanager.games.base;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class DuoGameBase<P extends ParticipantData, T extends ScoredTeamData<P> & Affiliated, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>> extends GameBase<P, T, QP, QT, S> {
    
    protected final @NotNull T northTeam;
    protected final @NotNull T southTeam;
    
    /**
     * @param gameInstanceId the {@link GameInstanceId} associated with this game
     * @param plugin the plugin
     * @param gameManager the GameManager
     * @param title the game's initial title, displayed in the sidebar
     * @param initialState the initialization state, should not contain any game functionality.
     * The state must never be null, so this is what the state should be
     * as the game is being initialized to prevent null-pointer
     * exceptions.
     * @param northTeam the north team
     * @param southTeam the south team
     */
    public DuoGameBase(
            int gameSessionId,
            @NotNull GameInstanceId gameInstanceId,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState,
            @NotNull T northTeam,
            @NotNull T southTeam) {
        super(gameSessionId, gameInstanceId, plugin, gameManager, title, initialState);
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
     * {@inheritDoc}
     */
    @Override
    protected CompletableFuture<Void> start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
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
        return super.start(spectatorTeams, newParticipants, newAdmins);
    }
    
    @Override
    public CompletableFuture<Void> onJoin(@NotNull Team newTeam, @NotNull Participant participant) {
        if (teams.containsKey(newTeam.getTeamId())) {
            return CompletableFuture.completedFuture(null);
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
                return super.onJoin(newTeam, participant);
            }
        }
        // TODO: should onTeamRejoin return completableFuture?
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * QuitData is not stored for the north or south teams when they quit.
     * Otherwise, this is the same as {@link super#onTeamQuit(String)}
     * @param teamId the teamId that quit
     */
    @Override
    public CompletableFuture<Void> onTeamQuit(@NotNull String teamId) {
        T team = teams.get(teamId);
        if (team == null || team.size() > 0) {
            return CompletableFuture.completedFuture(null);
        }
        Affiliation affiliation = getAffiliation(teamId);
        // TODO: this operation, and the equivalent one for joining, makes it seem like some quit logic isn't being called properly for north and south team quitting
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
                return super.onTeamQuit(teamId);
            }
        }
        resetTeamOptions(team);
        return CompletableFuture.completedFuture(null);
    }
    
    
}
