package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.PlayerInventoryDTO;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @param version
 * @param world
 * @param spectatorArea
 * @param firstPlaceSpawn
 * @param secondPlaceSpawn
 * @param spectatorSpawn
 * @param requiredWins
 * @param firstPlaceGate
 * @param secondPlaceGate
 * @param removeArea the area to remove all items from in between rounds
 * @param firstPlaceSupport
 * @param secondPlaceSupport
 * @param durations
 * @param description
 */
record ColossalCombatConfig(String version, String world, BoundingBoxDTO spectatorArea, LocationDTO firstPlaceSpawn, LocationDTO secondPlaceSpawn, LocationDTO spectatorSpawn, int requiredWins, @Nullable PlayerInventoryDTO loadout, Gate firstPlaceGate, Gate secondPlaceGate, BoundingBoxDTO removeArea, BoundingBoxDTO firstPlaceSupport, BoundingBoxDTO secondPlaceSupport, Durations durations, JsonElement description) {
    record Gate(BoundingBoxDTO clearArea, BoundingBoxDTO placeArea, BoundingBoxDTO stone, BoundingBoxDTO antiSuffocationArea) {
    }
    
    /**
     * @param roundStarting the duration (in seconds) to count down before the gates drop and the match starts
     * @param antiSuffocation the duration (in ticks) to prevent players from walking over the area that would cause them to suffocate in the concrete powder wall as the blocks fall. Careful, if this is not long enough the players will suffocate, and if it's too long they'll get frustrated. TODO: implement a more automated version of this. 
     */
    record Durations(int roundStarting, long antiSuffocation) {
    }
}
