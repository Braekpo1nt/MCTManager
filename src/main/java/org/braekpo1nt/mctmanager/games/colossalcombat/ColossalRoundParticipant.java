package org.braekpo1nt.mctmanager.games.colossalcombat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
class ColossalRoundParticipant extends Participant {
    
    private int kills;
    private int deaths;
    private boolean alive;
    private final @NotNull ColossalCombatRound.Affiliation affiliation;
    
    public ColossalRoundParticipant(@NotNull ColossalParticipant participant, boolean alive) {
        super(participant);
        this.kills = participant.getKills();
        this.deaths = participant.getDeaths();
        this.alive = alive;
        this.affiliation = participant.getAffiliation();
    }
    
    public boolean isDead() {
        return !alive;
    }
}
