package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import lombok.Getter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
public class CTFMatchTeam extends TeamData<CTFMatchParticipant> {
    
    @Getter
    private final @NotNull CaptureTheFlagMatch.Affiliation affiliation;
    
    public CTFMatchTeam(Team team, @NotNull CaptureTheFlagMatch.Affiliation affiliation) {
        super(team);
        this.affiliation = affiliation;
    }
}
