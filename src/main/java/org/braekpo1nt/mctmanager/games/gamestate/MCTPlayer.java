package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MCTPlayer {
    private UUID uniqueId;
    private String name;
    private int score;
    private String teamId;
}
