package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.jetbrains.annotations.NotNull;

public class FightingState extends GameplayState {
    
    public FightingState(@NotNull ColossalCombatGame context) {
        super(context);
    }
    
    @Override
    protected void onParticipantDeath(@NotNull ColossalParticipant participant) {
        super.onParticipantDeath(participant);
        if (context.getTeams().get(participant.getTeamId()).isDead()) {
            switch (participant.getAffiliation()) {
                case NORTH -> onTeamWinRound(southTeam);
                case SOUTH -> onTeamWinRound(northTeam);
            }
        } else if (config.shouldStartCaptureTheFlag() && suddenDeathThresholdReached()) {
            context.setState(new SuddenDeathCountdownState(context));
        }
    }
    
    @Override
    public void enter() {
        // do nothing
    }
    
    @Override
    public void exit() {
        // do nothing
    }
}
