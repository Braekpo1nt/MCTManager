package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.bukkit.Location;

/**
 * A class representing two teams that will fight each other in a match
 * @param northTeam
 * @param southTeam
 */
public record MatchPairing(String northTeam, String southTeam) {
    
    public boolean containsTeam(String teamName) {
        return northTeam.equals(teamName) || southTeam.equals(teamName);
    }
    
    /**
     * Represents an individual arena for Capture the Flag
     * @param northSpawn The spawn location at the north of the arena
     * @param southSpawn The spawn location at the south of the arena
     * @param northFlag The flag spawn/goal location for the north of the arena
     * @param southFlag The flag spawn/goal location for the south of the arena
     * @param northBarrier The origin location for the glass barrier for the north of the arena
     * @param southBarrier The origin location for the glass barrier for the south of the arena
     */
    public static record Arena(Location northSpawn, Location southSpawn, Location northFlag, Location southFlag,
                               Location northBarrier, Location southBarrier) {
    }
}
