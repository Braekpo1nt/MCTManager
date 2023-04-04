package org.braekpo1nt.mctmanager.games.capturetheflag;

/**
 * A class representing two teams that will fight each other
 */
public class TeamPairing {
    private final String northTeam;
    private final String southTeam;
    
    public TeamPairing(String northTeam, String southTeam) {
        this.northTeam = northTeam;
        this.southTeam = southTeam;
    }
    
    public String getNorthTeam() {
        return northTeam;
    }
    
    public String getSouthTeam() {
        return southTeam;
    }
    
}
