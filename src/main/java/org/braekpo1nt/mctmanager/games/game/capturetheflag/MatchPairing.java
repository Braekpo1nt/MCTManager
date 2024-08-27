package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * A class representing two teams that will fight each other in a match
 * @param northTeam
 * @param southTeam
 */
public record MatchPairing(@NotNull String northTeam, @NotNull String southTeam) {
    
    public boolean containsTeam(@NotNull String teamId) {
        return northTeam.equals(teamId) || southTeam.equals(teamId);
    }
    
    public boolean containsEitherTeam(@NotNull MatchPairing other) {
        return containsTeam(other.northTeam) || containsTeam(other.southTeam);
    }
    
    public boolean isEquivalent(@NotNull MatchPairing other) {
        if (this.northTeam.equals(other.northTeam)) {
            return this.southTeam.equals(other.southTeam);
        } else if (this.northTeam.equals((other.southTeam))) {
            return this.southTeam.equals((other.northTeam));
        }
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchPairing other = (MatchPairing) o;
        return this.isEquivalent(other);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(Set.of(northTeam, southTeam));
    }
    
    /**
     * Gets the opposite team of the given team, if the MatchPairing contains the given team. 
     * If you give the team name of the northTeam, you get the southTeam, and vice versa.
     * @param teamId The team name to get the opposite team of.
     * @return The team name of the opposite team. Null if this MatchPairing does not contain the given teamId.
     */
    public @Nullable String oppositeTeam(@NotNull String teamId) {
        if (northTeam.equals(teamId)) {
            return southTeam;
        }
        if (southTeam.equals((teamId))) {
            return northTeam;
        }
        return null;
    }
    
    @Override
    public String toString() {
        if (northTeam.compareTo(southTeam) < 0) {
            return String.format("{%s vs %s}", northTeam, southTeam);
        } else {
            return String.format("{%s vs %s}", southTeam, northTeam);
        }
    }
}
