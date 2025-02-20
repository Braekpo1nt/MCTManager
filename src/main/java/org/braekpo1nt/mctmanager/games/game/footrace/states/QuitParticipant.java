package org.braekpo1nt.mctmanager.games.game.footrace.states;

import lombok.Data;

/**
 * Holds data for the participant when they leave the game
 */
@Data
public class QuitParticipant {
    /**
     * The participant's current lap
     */
    private final int lap;
    /**
     * The participant's current checkpoint
     */
    private final int currentCheckpoint;
    /**
     * Whether the participant finished the race or not
     */
    private final boolean finished;
    /**
     * The participant's placement upon finishing the race
     */
    private final int placement;
}
