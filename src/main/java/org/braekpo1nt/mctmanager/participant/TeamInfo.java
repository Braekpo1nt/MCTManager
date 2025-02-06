package org.braekpo1nt.mctmanager.participant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

/**
 * An intermediary helper abstract implementation of {@link Team} whose job
 * is to reduce boilerplate of new Team implementations. 
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class TeamInfo implements Team {
    /**
     * The unique ID of the team
     */
    @EqualsAndHashCode.Include
    @Getter
    private final @NotNull String teamId;
    /**
     * The display name of the team
     */
    @Getter
    private final @NotNull String displayName;
    /**
     * The {@link TextColor} color associated with the team
     */
    @Getter
    private final @NotNull TextColor color;
    /**
     * The {@link Color} associated with the team
     */
    @Getter
    private final @NotNull Color bukkitColor;
    /**
     * The formatted display name of the team for use in chat messages.
     * The {@link #displayName} in {@link #color} and bold.
     */
    @Getter
    private final @NotNull Component formattedDisplayName;
    
    /**
     * @param teamId the unique ID of the team
     * @param displayName the display name of the team
     * @param color the {@link TextColor} associated with the team
     * @param bukkitColor The {@link Color} associated with the team
     * @param formattedDisplayName The formatted display name of the team for use in chat messages.
     */
    public TeamInfo(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, @NotNull Color bukkitColor, @NotNull Component formattedDisplayName) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.color = color;
        this.bukkitColor = bukkitColor;
        this.formattedDisplayName = formattedDisplayName;
    }
    
    /**
     * Create the info using the given name and color
     * @param teamId the unique ID of the team
     * @param displayName the display name of the team
     * @param color the {@link TextColor} associated with the team
     */
    public TeamInfo(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color) {
        this(teamId, displayName, color, Color.fromARGB(255, color.red(), color.green(), color.blue()), Component.text(displayName, color, TextDecoration.BOLD));
    }
    
    /**
     * Copy the immutable info from the given team
     * @param team the team to copy the info from
     */
    public TeamInfo(Team team) {
        this.teamId = team.getTeamId();
        this.displayName = team.getDisplayName();
        this.color = team.getColor();
        this.bukkitColor = team.getBukkitColor();
        this.formattedDisplayName = team.getFormattedDisplayName();
    }
}
