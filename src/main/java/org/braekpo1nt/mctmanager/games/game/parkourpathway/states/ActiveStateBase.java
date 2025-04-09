package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Shared functionality for the states where participants are
 * actively performing parkour
 */
class ActiveStateBase extends ParkourPathwayStateBase {
    protected final ParkourPathwayConfig config;
    
    public ActiveStateBase(@NotNull ParkourPathwayGame context) {
        super(context);
        this.config = context.getConfig();
    }
    
    @Override
    public void onParticipantRejoin(ParkourParticipant participant, ParkourTeam team) {
        Location respawn = context.getConfig()
                .getPuzzle(participant.getCurrentPuzzle())
                .checkPoints().get(participant.getCurrentPuzzleCheckpoint())
                .respawn();
        participant.teleport(respawn);
        context.giveBoots(participant);
        context.updateCheckpointSidebar(participant);
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        super.onNewParticipantJoin(participant, team);
        context.giveSkipItem(participant, config.getNumOfSkips());
    }
}
