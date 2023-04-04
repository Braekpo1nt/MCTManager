package org.braekpo1nt.mctmanager.games.capturetheflag;


import org.bukkit.Location;

/**
 * Represents an individual arena for Capture the Flag
 */
public class Arena {
    private final Location northSpawn;
    private final Location southSpawn;
    private final Location northFlag;
    private final Location southFlag;
    
    public Arena(Location northSpawn, Location southSpawn, Location northFlag, Location southFlag) {
        this.northSpawn = northSpawn;
        this.southSpawn = southSpawn;
        this.northFlag = northFlag;
        this.southFlag = southFlag;
    }
    
    public Location getNorthSpawn() {
        return northSpawn;
    }
    
    public Location getSouthSpawn() {
        return southSpawn;
    }
    
    public Location getNorthFlag() {
        return northFlag;
    }
    
    public Location getSouthFlag() {
        return southFlag;
    }
}
