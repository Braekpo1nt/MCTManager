package org.braekpo1nt.mctmanager.games.game.footrace.states;

import lombok.Data;

/**
 * Holds data for the participant when they leave the game
 */
@Data
public class QuitParticipant {
    /**
     * The lap cooldown for the participant crossing the finish line
     */
    private long lapCooldown;
    /**
     * The participant's current lap
     */
    private int lap;
    /**
     * The participant's current checkpoint
     */
    private int currentCheckpoint;
    /**
     * Whether the participant finished the race or not
     */
    private boolean finished;
    /**
     * The participant's placement upon finishing the race
     */
    private int placement;
}
