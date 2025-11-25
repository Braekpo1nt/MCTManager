package org.braekpo1nt.mctmanager.games.game.finalgame;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.base.Affiliated;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class FinalTeam extends ScoredTeamData<FinalParticipant> implements Affiliated {
    
    private final @NotNull Affiliation affiliation;
    /**
     * How many rounds this team has won
     */
    private int wins;
    
    public FinalTeam(
            @NotNull Team team,
            @NotNull Affiliation affiliation,
            int score
    ) {
        super(team, score);
        this.affiliation = affiliation;
    }
    
    public FinalTeam(
            @NotNull Team team,
            @NotNull QuitData quitData
    ) {
        this(
                team,
                quitData.getAffiliation(),
                quitData.getScore()
        );
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                this.affiliation,
                this.getScore()
        );
    }
    
    public int getAlive() {
        return (int) getParticipants().stream()
                .filter(FinalParticipant::isAlive)
                .count();
    }
    
    public boolean isAlive() {
        return getParticipants().stream()
                .anyMatch(FinalParticipant::isAlive);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final @NotNull Affiliation affiliation;
        private final int score;
    }
}
