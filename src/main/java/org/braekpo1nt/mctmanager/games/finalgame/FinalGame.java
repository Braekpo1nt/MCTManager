package org.braekpo1nt.mctmanager.games.finalgame;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class FinalGame implements MCTGame, Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final World finalGameWorld;
    private boolean gameActive = false;

    public FinalGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.finalGameWorld = worldManager.getMVWorld("FT").getCBWorld();
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        gameActive = true;
        Bukkit.getLogger().info("Started final game");
    }

    @Override
    public void stop() {
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping final game");
    }

    @Override
    public void onParticipantJoin(Player participant) {

    }

    @Override
    public void onParticipantQuit(Player participant) {

    }
}
