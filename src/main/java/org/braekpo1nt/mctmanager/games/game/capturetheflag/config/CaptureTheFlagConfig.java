package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.BattleClass;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.InventoryContentsDTO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

record CaptureTheFlagConfig(String version, String world, Vector spawnObservatory, List<ArenaDTO> arenas, Map<BattleClass, Loadout> loadouts, BoundingBoxDTO spectatorArea, Scores scores, Durations durations, JsonElement description) {
    
    record ArenaDTO(Vector northSpawn, Vector southSpawn, Vector northFlag, Vector southFlag, Vector northBarrier, Vector southBarrier, Arena.BarrierSize barrierSize, BoundingBoxDTO boundingBox) {
    }
    
    /**
     * Represents a loadout for a BattleClass
     * @param menuItem the item Material Type to use in the ClassPicker menu to represent the BattleClass
     * @param menuLore the description to show on the menu item when you hover over it (it's just an item lore)
     * @param inventory the player's inventory when they select this class
     */
    record Loadout(Material menuItem, List<JsonElement> menuLore, InventoryContentsDTO inventory) {
    }
    
    /**
     * Holds the scores for the game
     * @param kill the number of points to award for getting a kill
     * @param win the number of points to award for winning a match
     */
    record Scores(int kill, int win) {
    }
    
    /**
     * Holds durations for the game
     * @param matchesStarting the duration (in seconds) for the "matches starting" period (i.e. waiting in the lobby for the match to start)
     * @param classSelection the duration (in seconds) of the class selection period
     * @param roundTimer the duration (in seconds) of each round
     */
    record Durations(int matchesStarting, int classSelection, int roundTimer) {
    }
    
}
