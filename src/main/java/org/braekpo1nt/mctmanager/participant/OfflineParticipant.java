package org.braekpo1nt.mctmanager.participant;

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
    /**
     * the IGN of the player this represents
     */
    protected final @NotNull String name;
    protected final @NotNull Component displayName;
    
    /**
     * Create a new OfflineParticipant 
     * @param uniqueId the UUID of the player this participant represents
     * @param name the IGN of the player this represents
     * @param displayName the display name (usually the color of their team) of this participant
     * @param teamId the teamId of the participant
     */
    public OfflineParticipant(@NotNull UUID uniqueId, @NotNull String name, @NotNull Component displayName, @NotNull String teamId) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.displayName = displayName;
        this.teamId = teamId;
    }
    
    /**
     * Create a new OfflineParticipant
     * @param player the player this participant represents. The UUID, the name, the display name
     * @param teamId the teamId of the participant
     */
    public OfflineParticipant(@NotNull Player player, @NotNull String teamId) {
        this(player.getUniqueId(), player.getName(), player.displayName(), teamId);
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
     * @return the IGN of the player this represents
     */
    public @NotNull String getName() {
        return name;
    }
    
    /**
     * @return this participant's displayName
     */
    public @NotNull Component displayName() {
        return displayName;
    }
    
    
}
