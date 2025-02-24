package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class CTFParticipant extends Participant {
    
    private int kills;
    private int deaths;
    
    public CTFParticipant(@NotNull Participant participant) {
        super(participant);
        kills = 0;
        deaths = 0;
    }
    
}
