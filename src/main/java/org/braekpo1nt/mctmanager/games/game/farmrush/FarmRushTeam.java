package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class FarmRushTeam extends ScoredTeamData<FarmRushParticipant> {
    private final Arena arena;
    
    public FarmRushTeam(
            @NotNull Team team, 
            @NotNull Arena arena, 
            int score) {
        super(team, score);
        this.arena = arena;
    }
    
    public FarmRushTeam(
            @NotNull Team team,
            @NotNull QuitData quitData) {
        this(
                team, 
                quitData.getArena(), 
                quitData.getScore()
        );
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                this.arena,
                getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final Arena arena;
        private final int score;
    }
}
