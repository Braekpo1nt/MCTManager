package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.Data;

@Data
public class ParkourQuitData {
    private final boolean finished;
    private final int currentPuzzle;
    private final int currentPuzzleCheckpoint;
    private final int numOfSkips;
}
