package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.braekpo1nt.mctmanager.games.game.parkourpathway_old.TeamSpawn;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TeamSpawnsDescription extends ParkourPathwayStateBase {
    
    /**
     * The available teamSpawns
     */
    private @NotNull final List<TeamSpawn> teamSpawns;
    /**
     * A map of teamIds to their {@link TeamSpawn}s
     */
    private @NotNull Map<String, TeamSpawn> teamsToSpawns;
    
    public TeamSpawnsDescription(@NotNull ParkourPathwayGame context, @NotNull List<TeamSpawn> teamSpawns) {
        super(context);
        this.teamSpawns = teamSpawns;
        this.teamsToSpawns = createTeamSpawns();
        for (ParkourParticipant participant : context.getParticipants().values()) {
            teamsToSpawns.get(participant.getTeamId()).teleport(participant);
        }
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Opening in: "))
                .onCompletion(() -> {
                    for (TeamSpawn teamSpawn : teamsToSpawns.values()) {
                        teamSpawn.open();
                    }
                    context.setState(new CountdownState(context));
                    teamsToSpawns.clear();
                })
                .build());
    }
    
    @Override
    public void cleanup() {
        for (TeamSpawn teamSpawn : teamsToSpawns.values()) {
            teamSpawn.open();
        }
        teamsToSpawns.clear();
    }
    
    /**
     * @return a map of teamIds to their {@link TeamSpawn}s
     */
    private @NotNull Map<String, TeamSpawn> createTeamSpawns() {
        Set<String> teamIds = Team.getTeamIds(context.getTeams());
        Map<String, TeamSpawn> result = new HashMap<>(teamIds.size());
        int i = 0;
        for (String teamId : teamIds) {
            int teamSpawnIndex = MathUtils.wrapIndex(i, teamSpawns.size());
            TeamSpawn teamSpawn = teamSpawns.get(teamSpawnIndex);
            teamSpawn.setBarrierMaterial(context.getGameManager().getTeamStainedGlassColor(teamId));
            result.put(teamId, teamSpawn);
            i++;
        }
        for (TeamSpawn teamSpawn : result.values()) {
            teamSpawn.close();
        }
        return result;
    }
    
    @Override
    public void onNewTeamJoin(ParkourTeam team) {
        super.onNewTeamJoin(team);
        this.teamsToSpawns = createTeamSpawns();
    }
    
    
    @Override
    public void onParticipantRejoin(ParkourParticipant participant, ParkourTeam team) {
        super.onParticipantRejoin(participant, team);
        teamsToSpawns.get(participant.getTeamId()).teleport(participant);
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        super.onNewParticipantJoin(participant, team);
        teamsToSpawns.get(participant.getTeamId()).teleport(participant);
    }
}
