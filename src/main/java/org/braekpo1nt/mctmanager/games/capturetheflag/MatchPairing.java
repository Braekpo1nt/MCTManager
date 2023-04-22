package org.braekpo1nt.mctmanager.games.capturetheflag;

/**
 * A class representing two teams that will fight each other in a match
 * @param northTeam
 * @param southTeam
 */
public record MatchPairing(String northTeam, String southTeam) {
    
    public boolean containsTeam(String teamName) {
        return northTeam.equals(teamName) || southTeam.equals(teamName);
    }
}
