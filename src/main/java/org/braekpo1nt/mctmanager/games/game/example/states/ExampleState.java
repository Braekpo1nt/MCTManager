package org.braekpo1nt.mctmanager.games.game.example.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.experimental.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.games.game.example.ExampleTeam;

public abstract class ExampleState extends GameStateBase<ExampleParticipant, ExampleTeam, ExampleParticipant.QuitData, ExampleTeam.QuitData, GameBase<ExampleParticipant, ExampleTeam, ExampleParticipant.QuitData, ExampleTeam.QuitData>> {
    
    public ExampleState(ExampleGame context) {
        super(context);
    }
    
    @Override
    public void cleanup() {
        // custom stop code
        Main.logger().info("Cleaning up the current state");
    }
    
    @Override
    protected void onTeamRejoin(ExampleTeam team) {
        // custom team rejoin code
        Main.logf("First team member of re-joining team %s joined", team.getTeamId());
    }
    
    @Override
    protected void onNewTeamJoin(ExampleTeam team) {
        // custom team join code
        Main.logf("First team member of new team %s joined", team.getTeamId());
    }
    
    @Override
    protected void onParticipantRejoin(ExampleParticipant participant, ExampleTeam team) {
        // custom participant rejoin code
        Main.logf("%s re-joined the game, after quitting", participant.getName());
    }
    
    @Override
    protected void onNewParticipantJoin(ExampleParticipant participant, ExampleTeam team) {
        // custom participant join code
        Main.logf("%s joined the game for the first time", participant.getName());
    }
    
    @Override
    protected void onParticipantQuit(ExampleParticipant participant, ExampleTeam team) {
        // custom participant quit code
        Main.logf("%s quit the game", participant.getName());
    }
    
    @Override
    protected void onTeamQuit(ExampleTeam team) {
        // custom team quit code
        Main.logf("Last team member of %s quit", team.getTeamId());
    }
}
