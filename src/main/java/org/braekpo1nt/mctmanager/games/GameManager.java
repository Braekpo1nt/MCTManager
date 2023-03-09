package org.braekpo1nt.mctmanager.games;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager {
    
    private final FootRaceGame footRaceGame;
    
    public GameManager(Main plugin) {
        this.footRaceGame = new FootRaceGame(plugin);
    }
    
    public void startFootRace() {
        footRaceGame.start();
    }
    
}
