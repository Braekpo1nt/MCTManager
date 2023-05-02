package org.braekpo1nt.mctmanager.games.capturetheflag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing two teams that will fight each other in a match
 * @param northTeam
 * @param southTeam
 */
public record MatchPairing(@NotNull String northTeam, @NotNull String southTeam) {
    
    public boolean containsTeam(@NotNull String teamName) {
        return northTeam.equals(teamName) || southTeam.equals(teamName);
    }

    /**
     * Gets the opposite team of the given team, if the MatchPairing contains the given team. 
     * If you give the team name of the northTeam, you get the southTeam, and vice versa.
     * @param teamName The team name to get the opposite team of.
     * @return The team name of the opposite team. Null if this MatchPairing does not contain the given teamName.
     */
    public @Nullable String oppositeTeam(@NotNull String teamName) {
        if (northTeam.equals(teamName)) {
            return southTeam;
        }
        if (southTeam.equals((teamName))) {
            return northTeam;
        }
        return null;
    }
}
