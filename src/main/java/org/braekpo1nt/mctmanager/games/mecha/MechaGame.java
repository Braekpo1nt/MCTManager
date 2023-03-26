package org.braekpo1nt.mctmanager.games.mecha;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class MechaGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;

    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }


    @Override
    public void start(List<Player> participants) {
        Bukkit.getLogger().info("Started mecha");
    }

    @Override
    public void stop() {
        Bukkit.getLogger().info("Stopped mecha");
    }
}
