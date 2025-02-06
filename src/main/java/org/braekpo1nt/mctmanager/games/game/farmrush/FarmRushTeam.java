package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class FarmRushTeam extends TeamData<Participant> {
    private final Arena arena;
    /**
     * Used to keep track of the physical lineup of arenas, so that
     * when a new team is added you can always add it on to the end
     * of the line
     */
    private final int arenaOrder;
    private final List<Location> cropGrowers = new ArrayList<>();
    /**
     * keeps track of the total score accrued during this game. Used
     * for checking if the players have surpassed the configured maxScore
     * ({@link FarmRushConfig#getMaxScore()})
     */
    private int totalScore = 0;
    
    public FarmRushTeam(@NotNull Team team, @NotNull Arena arena, int arenaOrder) {
        super(team);
        this.arena = arena;
        this.arenaOrder = arenaOrder;
    }
}
