package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
     * The ID of the team
     */
    @EqualsAndHashCode.Include
    protected final @NotNull String teamId;
    /**
     * The display name of the team for chat messages
     */
    protected final @NotNull Component displayName;
    protected final @NotNull TextColor color;
    protected final @NotNull Set<UUID> members = new HashSet<>();
}
