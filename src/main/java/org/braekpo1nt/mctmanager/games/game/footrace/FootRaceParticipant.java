package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class FootRaceParticipant extends Participant {
    
    /**
     * The lap cooldown for the participant crossing the finish line
     */
    private long lapCooldown;
    /**
     * What lap the participant is on
     */
    private int lap;
    /**
     * Whether the participant finished the race or not
     */
    private boolean finishedRace;
    /**
     * The participant's current standing
     */
    private int standing;
    
    public FootRaceParticipant(@NotNull Participant participant) {
        super(participant);
    }
}
