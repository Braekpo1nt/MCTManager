package org.braekpo1nt.mctmanager.games.experimental;

public interface Affiliated {
    enum Affiliation {
        NORTH,
        SOUTH
    }
    
    Affiliation getAffiliation();
}
