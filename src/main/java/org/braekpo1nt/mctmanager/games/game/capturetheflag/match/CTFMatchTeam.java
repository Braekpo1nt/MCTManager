package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import lombok.Getter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
public class CTFMatchTeam extends ScoredTeamData<CTFMatchParticipant> {
    
    @Getter
    private final @NotNull CaptureTheFlagMatch.Affiliation affiliation;
    
    public CTFMatchTeam(@NotNull CTFTeam ctfTeam, @NotNull CaptureTheFlagMatch.Affiliation affiliation) {
        super(ctfTeam, ctfTeam.getScore());
        this.affiliation = affiliation;
    }
}
