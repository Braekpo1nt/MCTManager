package org.braekpo1nt.mctmanager.games.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.entity.Player;

import java.util.List;

public class ClockWorkGame implements MCTGame {
    private final Main plugin;
    private final GameManager gameManager;

    public ClockWorkGame(Main plugin, GameManager gameManager) {

        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public MCTGames getType() {
        return MCTGames.CLOCKWORK;
    }

    @Override
    public void start(List<Player> newParticipants) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onParticipantJoin(Player participant) {

    }

    @Override
    public void onParticipantQuit(Player participant) {

    }
}
