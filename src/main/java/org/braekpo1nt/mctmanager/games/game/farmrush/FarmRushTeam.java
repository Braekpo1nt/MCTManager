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
    private int sellPoints;
    
    public FarmRushTeam(
            @NotNull Team team, 
            @NotNull Arena arena, 
            int score,
            int sellPoints) {
        super(team, score);
        this.arena = arena;
        this.sellPoints = sellPoints;
    }
    
    public void addSellPoints(int points) {
        this.sellPoints += points;
    }
    
    public FarmRushTeam(
            @NotNull Team team,
            @NotNull QuitData quitData) {
        this(
                team, 
                quitData.getArena(), 
                quitData.getScore(),
                quitData.getSellPoints()
        );
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                this.arena,
                getScore(),
                this.sellPoints);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final Arena arena;
        private final int score;
        private final int sellPoints;
    }
}
