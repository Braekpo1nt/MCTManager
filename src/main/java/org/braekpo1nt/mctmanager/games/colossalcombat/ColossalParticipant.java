package org.braekpo1nt.mctmanager.games.colossalcombat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
class ColossalParticipant extends Participant {
    
    private int kills;
    private int deaths;
    private final @NotNull ColossalCombatRound.Affiliation affiliation;
    
    public ColossalParticipant(@NotNull Participant participant, int kills, int deaths, @NotNull ColossalCombatRound.Affiliation affiliation) {
        super(participant);
        this.kills = kills;
        this.deaths = deaths;
        this.affiliation = affiliation;
    }
    
    public ColossalParticipant(@NotNull Participant participant, ColossalQuitData quitData, @NotNull ColossalCombatRound.Affiliation affiliation) {
        this(participant, quitData.getKills(), quitData.getDeaths(), affiliation);
    }
    
    public ColossalQuitData getQuitData() {
        return new ColossalQuitData(kills, deaths);
    }
    
}
