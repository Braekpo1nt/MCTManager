package org.braekpo1nt.mctmanager.games.gamemanager;

import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MCTParticipant extends Participant {
    
    /**
     * Indicates whether the participant is in a game or not
     */
    @Getter
    @Setter
    private @Nullable GameType currentGame;
    
    public MCTParticipant(@NotNull OfflineParticipant participant, @NotNull Player player) {
        super(participant, player, participant.getScore());
        currentGame = null;
    }
    
    public MCTParticipant(@NotNull MCTParticipant participant, int score) {
        super(participant, score);
        currentGame = null;
    }
    
    public boolean isInGame() {
        return currentGame != null;
    }
}
