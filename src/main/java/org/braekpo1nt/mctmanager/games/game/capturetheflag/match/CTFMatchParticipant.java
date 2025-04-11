package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
public class CTFMatchParticipant extends ParticipantData {
    
    @Getter
    @Setter
    private int kills;
    @Getter
    @Setter
    private int deaths;
    @Getter
    @Setter
    private boolean alive;
    /**
     * which side of the arena the participant is on
     */
    @Getter
    private final @NotNull CaptureTheFlagMatch.Affiliation affiliation;
    private boolean hasFlag;
    
    public CTFMatchParticipant(@NotNull CTFParticipant ctfParticipant, @NotNull CaptureTheFlagMatch.Affiliation affiliation, boolean alive) {
        super(ctfParticipant, ctfParticipant.getScore());
        this.kills = ctfParticipant.getKills();
        this.deaths = ctfParticipant.getDeaths();
        this.alive = alive;
        this.affiliation = affiliation;
        this.hasFlag = false;
    }
    
    /**
     * @return true if this player has the opposite team's flag
     */
    public boolean hasFlag() {
        return hasFlag;
    }
    
    /**
     * @param hasFlag true if the player has the opposite team's flag, false otherwise
     */
    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }
}
