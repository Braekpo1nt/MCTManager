package org.braekpo1nt.mctmanager.nonParticipant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OfflineNonParticipant implements AudienceDelegate{
    /**
     * The UUID of the player this Participant represents
     */
    @EqualsAndHashCode.Include
    protected final @NotNull UUID uniqueId;
    /**
     * the IGN of the player this represents
     */
    protected final @NotNull String name;
    
    protected final @NotNull Component displayName;
    
    /**
     * Create a new OfflineNonParticipant
     * @param uniqueId the UUID of the player this nonParticipant represents
     * @param name the IGN of the nonParticipant this represents
     * @param displayName the display name of this nonParticipant
     */
    public OfflineNonParticipant(@NotNull UUID uniqueId, @NotNull String name, @NotNull Component displayName) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.displayName = displayName;
    }
    
    /**
     * Create a new OfflineNonParticipant
     * @param player the player this nonParticipant represents. The UUID, the name, the display name
     */
    public OfflineNonParticipant(@NotNull Player player) {
        this(player.getUniqueId(), player.getName(), player.displayName());
    }
    
    /**
     * Copy everything about the given OfflineNonParticipant, but use the new name and display name
     * @param offlineNonParticipant copy everything about the offline nonParticipant
     * @param newName the name to use
     * @param displayName the display name to use
     */
    public OfflineNonParticipant(@NotNull OfflineNonParticipant offlineNonParticipant, @NotNull String newName, @NotNull Component displayName) {
        this(offlineNonParticipant.uniqueId, newName, displayName);
    }
    
    /**
     * @param offlineNonParticipant the OfflineNonParticipant to copy the info from
     */
    public OfflineNonParticipant(@NotNull OfflineNonParticipant offlineNonParticipant) {
        this(offlineNonParticipant.uniqueId, offlineNonParticipant.getName(), offlineNonParticipant.displayName);
    }
    
    @Override
    public @NotNull Audience getAudience() {
        return Audience.empty();
    }
    
    public @NotNull UUID getUniqueId() {return uniqueId;}
    
    public @NotNull NonParticipantID getNonParticipantID() {return new NonParticipantID(getUniqueId());}
    
    public @Nullable Player getPlayer() {
        return null;
    }
    
    public @NotNull String getName() {
        return name;
    }
    
    public @NotNull Component displayName() {
        return displayName;
    }
}
