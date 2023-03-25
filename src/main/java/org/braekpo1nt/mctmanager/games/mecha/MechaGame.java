package org.braekpo1nt.mctmanager.games.mecha;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;

public class MechaGame implements MCTGame {

    private final GameManager gameManager;

    public MechaGame(GameManager gameManager) {

        this.gameManager = gameManager;
    }
    
}
