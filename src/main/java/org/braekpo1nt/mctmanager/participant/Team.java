package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a team of {@link Participant}s
 */
public interface Team {
    
    /**
     * @return The unique ID of the team
     */
    @NotNull String getTeamId();
    
    /**
     * @return The display name of the team
     */
    @NotNull String getDisplayName();
    
    /**
     * @return The formatted display name of the team for use in chat messages.
     * The {@link #getDisplayName()} in {@link #getColor()} and bold.
     */
    @NotNull Component getFormattedDisplayName();
    
    /**
     * @return The {@link TextColor} color associated with the team
     */
    @NotNull TextColor getColor();
    
    /**
     * @return this team's {@link ColorAttributes}
     */
    @NotNull ColorAttributes getColorAttributes();
    
    /**
     * @return the team's score
     */
    int getScore();
    
    /**
     * @return The {@link Color} associated with the team
     */
    @NotNull Color getBukkitColor();
    
    /**
     * @param teams the map of Teams to get the teamIds of
     * @return a set of all the teamIds of the given map's values
     */
    static <T extends Team> Set<String> getTeamIds(Map<String, T> teams) {
        return getTeamIds(teams.values());
    }
    
    /**
     * @param teams the Teams to get the teamIds of
     * @return a set of all the teamIds of the given teams
     */
    static <T extends Team> Set<String> getTeamIds(Collection<T> teams) {
        return teams.stream().map(Team::getTeamId).collect(Collectors.toSet());
    }
    
    static String toString(Collection<? extends Team> teams) {
        return teams.stream()
                .map(Team::getTeamId)
                .toList()
                .toString();
    }
    
}
