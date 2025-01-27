package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OfflineParticipant implements AudienceDelegate {
    
    /**
     * The UUID of the player this Participant represents
     */
    @EqualsAndHashCode.Include
    protected final @NotNull UUID uniqueId;
    /**
     * The teamId of the team this Participant belongs to
     */
    @Getter
    protected final @NotNull String teamId;
    protected final @NotNull Component displayName;
    
    /**
     * Create a new OfflineParticipant 
     * @param uniqueId the UUID of the player this participant represents
     * @param teamId the teamId of the participant
     */
    public OfflineParticipant(@NotNull UUID uniqueId, @NotNull Component displayName, @NotNull String teamId) {
        this.uniqueId = uniqueId;
        this.displayName = displayName;
        this.teamId = teamId;
    }
    
    @Override
    public @NotNull Audience getAudience() {
        return Audience.empty();
    }
    
    /**
     * @return the UUID of the player this Participant represents
     */
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }
    
    /**
     * @return the Player associated with this participant if they are online, null if they are not
     */
    public @Nullable Player getPlayer() {
        return null;
    }
    
    /**
     * @return this participant's displayName
     */
    public @NotNull Component displayName() {
        return displayName;
    }
    
    
}
