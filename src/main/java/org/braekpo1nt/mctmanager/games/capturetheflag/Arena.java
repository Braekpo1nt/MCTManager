package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.bukkit.Location;

/**
 * Represents an individual arena for Capture the Flag
 * @param northSpawn The spawn location at the north of the arena
 * @param southSpawn The spawn location at the south of the arena
 * @param northFlag The flag spawn/goal location for the north of the arena
 * @param southFlag The flag spawn/goal location for the south of the arena
 * @param northBarrier The origin location for the glass barrier for the north of the arena
 * @param southBarrier The origin location for the glass barrier for the south of the arena
 */
public record Arena(Location northSpawn, Location southSpawn, Location northFlag, Location southFlag,
                           Location northBarrier, Location southBarrier) {
}