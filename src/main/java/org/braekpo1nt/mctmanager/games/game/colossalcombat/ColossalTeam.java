package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Data;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.experimental.Affiliated;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

@Getter
public class ColossalTeam extends ScoredTeamData<ColossalParticipant> implements Affiliated {
    
    private final @NotNull Affiliated.Affiliation affiliation;
    
    public ColossalTeam(@NotNull Team team, int score, @NotNull Affiliated.Affiliation affiliation) {
        super(team, score);
        this.affiliation = affiliation;
    }
    
    public ColossalTeam(@NotNull Team team, @NotNull QuitData quitData) {
        this(team, quitData.getScore(), quitData.getAffiliation());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), this.affiliation);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final @NotNull Affiliation affiliation;
    }
}
