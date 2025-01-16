package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a team of {@link Participant}s
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team {
    /**
     * The unique ID of the team
     */
    @EqualsAndHashCode.Include
    protected final @NotNull String teamId;
    /**
     * The display name of the team
     */
    protected final @NotNull String displayName;
    /**
     * The color associated with the team
     */
    protected final @NotNull TextColor color;
    /**
     * The UUIDs of the {@link Participant}s on this team
     */
    protected final @NotNull Set<UUID> members = new HashSet<>();
    /**
     * The formatted display name of the team for use in chat messages.
     * The {@link #displayName} in {@link #color} and bold.
     */
    protected final @NotNull Component formattedDisplayName;
    
    public Team(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.color = color;
        this.formattedDisplayName = Component.text(displayName, color, TextDecoration.BOLD);
    }
}
