package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Location;
import org.bukkit.World;

public class ColossalColosseumRound {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final World colossalColosseumWorld;
    private final Location firstPlaceSpawn;
    private final Location secondPlaceSpawn;
    private final Location spectatorSpawn;
    
    public ColossalColosseumRound(Main plugin, GameManager gameManager, ColossalColosseumGame colossalColosseumGame) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.colossalColosseumWorld = worldManager.getMVWorld("FT").getCBWorld();
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.firstPlaceSpawn = anchorManager.getAnchorLocation("cc-first-place-spawn");
        this.secondPlaceSpawn = anchorManager.getAnchorLocation("cc-second-place-spawn");
        this.spectatorSpawn = anchorManager.getAnchorLocation("cc-spectator-spawn");
    }
    
}
