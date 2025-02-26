package org.braekpo1nt.mctmanager.games.colossalcombat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class ColossalRoundParticipant extends Participant {
    
    private int kills;
    private int deaths;
    private boolean alive;
    private final @NotNull ColossalCombatRound.Affiliation affiliation;
    
    public ColossalRoundParticipant(@NotNull Participant participant, int kills, int deaths, boolean alive, @NotNull ColossalCombatRound.Affiliation affiliation) {
        super(participant);
        this.kills = kills;
        this.deaths = deaths;
        this.alive = alive;
        this.affiliation = affiliation;
    }
    
    public boolean isDead() {
        return !alive;
    }
}
