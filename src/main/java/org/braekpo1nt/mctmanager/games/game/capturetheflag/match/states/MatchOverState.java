package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;

public class MatchOverState extends CaptureTheFlagMatchStateBase {
    
    public MatchOverState(CaptureTheFlagMatch context) {
        super(context);
        for (CTFMatchParticipant participant : context.getParticipants().values()) {
            if (participant.isAlive()) {
                participant.teleport(context.getConfig().getSpawnObservatory());
                participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
                ParticipantInitializer.clearInventory(participant);
                participant.closeInventory();
                ParticipantInitializer.resetHealthAndHunger(participant);
                ParticipantInitializer.clearStatusEffects(participant);
            }
        }
        context.getMatchIsOver().run();
    }
}
