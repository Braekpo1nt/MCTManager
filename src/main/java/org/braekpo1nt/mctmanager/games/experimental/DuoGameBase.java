package org.braekpo1nt.mctmanager.games.experimental;

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
        if (northTeam.getAffiliation() != Affiliated.Affiliation.NORTH) {
            throw new IllegalArgumentException("northTeam.getAffiliation() must be NORTH");
        }
        if (southTeam.getAffiliation() != Affiliated.Affiliation.SOUTH) {
            throw new IllegalArgumentException("southTeam.getAffiliation() must be SOUTH");
        }
        this.northTeam = northTeam;
        this.southTeam = southTeam;
    }
    
    public @NotNull Affiliated.Affiliation getAffiliation(@NotNull String teamId) {
        if (northTeam.getTeamId().equals(teamId)) {
            return Affiliated.Affiliation.NORTH;
        } else if (southTeam.getTeamId().equals(teamId)) {
            return Affiliated.Affiliation.SOUTH;
        }
        return Affiliated.Affiliation.SPECTATOR;
    }
    
    /**
     * Convenience method for implementations. Calls {@link #start(Collection, Collection, List)}, but makes a list of two elements (the north and south teams)
     * @param newParticipants the newParticipants
     * @param newAdmins the newAdmins
     */
    protected void start(@NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        listeners.forEach(listener -> listener.register(plugin));
        
        teams.put(northTeam.getTeamId(), northTeam);
        teams.put(southTeam.getTeamId(), southTeam);
        setupTeamOptions(northTeam);
        setupTeamOptions(southTeam);
        tabList.addTeam(northTeam.getTeamId(), northTeam.getDisplayName(), northTeam.getColor());
        tabList.addTeam(southTeam.getTeamId(), southTeam.getDisplayName(), southTeam.getColor());
        initializeTeam(northTeam);
        initializeTeam(southTeam);
        
        for (Participant newParticipant : newParticipants) {
            P participant = createParticipant(newParticipant);
            T team = teams.get(participant.getTeamId());
            tabList.joinParticipant(participant.getParticipantID(), participant.getName(), participant.getTeamId(), false);
            addParticipant(participant, team);
            participant.setGameMode(GameMode.ADVENTURE);
            ParticipantInitializer.clearStatusEffects(participant);
            ParticipantInitializer.clearInventory(participant);
            ParticipantInitializer.resetHealthAndHunger(participant);
            initializeParticipant(participant, team);
        }
        _initializeSidebar();
        
        // admin start
        for (Player admin : newAdmins) {
            _initializeAdmin(admin);
        }
        _initializeAdminSidebar();
        // admin end
        this.state = getStartState();
    }
    
    @Override
    protected void start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        throw new UnsupportedOperationException("don't use this in DuoGameBase implementations, use start(T,T,Collection,List)");
    }
    
    @Override
    protected @NotNull T createTeam(Team team) {
        throw new UnsupportedOperationException("don't use createTeam() in DuoGameBase");
    }

    @Override
    protected @NotNull T createTeam(Team team, QT quitData) {
        throw new UnsupportedOperationException("don't use createTeam() in DuoGameBase");
    }
    
    @Override
    public void onTeamJoin(Team newTeam) {
        if (teams.containsKey(newTeam.getTeamId())) {
            return;
        }
        Affiliated.Affiliation affiliation = getAffiliation(newTeam.getTeamId());
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
        Affiliated.Affiliation affiliation = getAffiliation(teamId);
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
