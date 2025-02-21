package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Data;

@Data
public class SurvivalGamesQuitData {
    private final boolean alive;
    private final int kills;
    private final int deaths;
}
