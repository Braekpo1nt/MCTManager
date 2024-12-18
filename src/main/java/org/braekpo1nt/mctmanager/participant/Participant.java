package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Participant extends AudienceDelegate {
    
    /**
     * The player object that this Participant represents
     */
    @EqualsAndHashCode.Include
    private final @NotNull Player player;
    /**
     * The teamId of the team this Participant belongs to
     */
    private final @NotNull String teamId;
    
    /**
     * @return the UUID of the player this Participant represents
     */
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Audience getAudience() {
        return player;
    }
}
