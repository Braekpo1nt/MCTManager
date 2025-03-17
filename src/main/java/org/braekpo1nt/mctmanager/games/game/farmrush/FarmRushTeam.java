package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class FarmRushTeam extends ScoredTeamData<FarmRushParticipant> {
    private final Arena arena;
    /**
     * Used to keep track of the physical lineup of arenas, so that
     * when a new team is added you can always add it on to the end
     * of the line
     */
    private final int arenaOrder;
    private final List<Location> cropGrowers = new ArrayList<>();
    
    public FarmRushTeam(@NotNull Team team, @NotNull Arena arena, int arenaOrder, int score) {
        super(team, score);
        this.arena = arena;
        this.arenaOrder = arenaOrder;
    }
}
