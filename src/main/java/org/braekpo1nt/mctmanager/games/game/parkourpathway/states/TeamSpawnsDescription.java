package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamSpawnsDescription extends ParkourPathwayStateBase {
    
    /**
     * The available teamSpawns
     */
    private @NotNull final List<TeamSpawn> teamSpawns;
    /**
     * A map of teamIds to their {@link TeamSpawn}s
     */
    private @NotNull Map<String, TeamSpawn> teamsToSpawns;
    private @Nullable Timer timer;
    
    public TeamSpawnsDescription(@NotNull ParkourPathwayGame context, @NotNull List<TeamSpawn> teamSpawns) {
        super(context);
        this.teamSpawns = teamSpawns;
        this.teamsToSpawns = createTeamSpawns();
    }
    
    @Override
    public void enter() {
        for (ParkourParticipant participant : context.getParticipants().values()) {
            teamsToSpawns.get(participant.getTeamId()).teleport(participant);
        }
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Opening in: "))
                .onCompletion(() -> {
                    for (TeamSpawn teamSpawn : teamsToSpawns.values()) {
                        teamSpawn.open();
                    }
                    context.setState(new CountDownState(context));
                    teamsToSpawns.clear();
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
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
        Map<String, TeamSpawn> result = new HashMap<>(context.getTeams().size());
        int i = 0;
        for (ParkourTeam team : context.getTeams().values()) {
            int teamSpawnIndex = MathUtils.wrapIndex(i, teamSpawns.size());
            TeamSpawn teamSpawn = teamSpawns.get(teamSpawnIndex);
            teamSpawn.setBarrierMaterial(team.getColorAttributes().getStainedGlass());
            result.put(team.getTeamId(), teamSpawn);
            teamSpawn.close();
            i++;
        }
        return result;
    }
    
    @Override
    public void onNewTeamJoin(ParkourTeam team) {
        super.onNewTeamJoin(team);
        for (TeamSpawn teamSpawn : teamsToSpawns.values()) {
            teamSpawn.open();
        }
        this.teamsToSpawns = createTeamSpawns();
        for (ParkourParticipant participant : context.getParticipants().values()) {
            teamsToSpawns.get(participant.getTeamId()).teleport(participant);
        }
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
