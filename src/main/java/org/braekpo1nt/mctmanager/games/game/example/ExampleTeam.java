package org.braekpo1nt.mctmanager.games.game.example;

import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class ExampleTeam extends ScoredTeamData<ExampleParticipant> {
    public ExampleTeam(Team team, int score) {
        super(team, score);
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    public static class QuitData extends QuitDataBase {
        public QuitData(int score) {
            super(score);
        }
    }
}
