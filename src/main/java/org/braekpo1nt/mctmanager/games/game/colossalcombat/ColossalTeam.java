package org.braekpo1nt.mctmanager.games.game.colossalcombat;

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
public class ColossalTeam extends ScoredTeamData<ColossalParticipant> implements Affiliated {
    
    private final @NotNull Affiliation affiliation;
    /**
     * How many rounds this team has won
     */
    private int wins;
    
    public ColossalTeam(@NotNull Team team, int score, @NotNull Affiliation affiliation) {
        super(team, score);
        this.affiliation = affiliation;
        this.wins = 0;
    }
    
    public ColossalTeam(@NotNull Team team, @NotNull QuitData quitData) {
        this(team, quitData.getScore(), quitData.getAffiliation());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), this.affiliation);
    }
    
    /**
     * @return the number of members who are alive
     */
    public int getAlive() {
        return ((int) getParticipants().stream().filter(ColossalParticipant::isAlive).count());
    }
    
    /**
     * @return true if every member of the team is dead, false otherwise
     */
    public boolean isDead() {
        return getParticipants().stream().noneMatch(ColossalParticipant::isAlive);
    }
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final @NotNull Affiliation affiliation;
    }
}
