package org.braekpo1nt.mctmanager.games.gamemanager;

import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MCTParticipant extends Participant {
    
    public MCTParticipant(@NotNull OfflineParticipant participant, @NotNull Player player) {
        super(participant, player, participant.getScore());
    }
    
    public MCTParticipant(@NotNull MCTParticipant participant, int score) {
        super(participant, score);
    }
}
