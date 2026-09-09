package org.braekpo1nt.mctmanager.nonParticipant;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// Use this class to set the base permissions of all nonParticipantType
// If a player is undefined as either participant or nonParticipant the player will be set to this "base" type
public class NonParticipantType extends NonParticipant{
    
    private final @NotNull Player player;
    
    private final @NotNull Component displayName;
    
    public NonParticipantType(@NotNull Player player) {
        super(player);
        this.player = player;
        this.displayName = player.displayName();
    }
    
    /**
     * Gets the UUID of this NonParticipant
     * @return
     */
    public @NotNull UUID getUniqueId() {
        return player.getUniqueId();
    }
    
    public @NotNull Player getPlayer() {
        return player;
    }
    
    public @NotNull Component getDisplayName() {return displayName;}
    
}
