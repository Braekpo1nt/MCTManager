package org.braekpo1nt.mctmanager.games.game.example.states;

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
        
    }
    
    @Override
    protected void onTeamRejoin(ExampleTeam team) {
        
    }
    
    @Override
    protected void onNewTeamJoin(ExampleTeam team) {
        
    }
    
    @Override
    protected void onParticipantRejoin(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    protected void onNewParticipantJoin(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    protected void onParticipantQuit(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    protected void onTeamQuit(ExampleTeam team) {
        
    }
}
