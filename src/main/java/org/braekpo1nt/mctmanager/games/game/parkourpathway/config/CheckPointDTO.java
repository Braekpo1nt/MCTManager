package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import org.braekpo1nt.mctmanager.games.game.parkourpathway.CheckPoint;
import org.bukkit.util.Vector;

/**
 * The Data Transfer Object holding the necessary information to create a {@link CheckPoint}. 
 * For more info, look up the DAO (Data Access Object) and DTO (Data Transfer Object) pattern
 * @param yValue
 * @param min
 * @param max
 * @param respawn
 */
public record CheckPointDTO(int yValue, Vector min, Vector max, Vector respawn) {
}
