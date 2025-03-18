package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface Topbar {
    
    /**
     * Show this Topbar to the given participant
     * @param participant the participant to show this Topbar to
     */
    default void showPlayer(@NotNull Participant participant) {
        showPlayer(participant.getPlayer());
    }
    
    /**
     * Show this Topbar to the given player
     * @param player the player to show this Topbar to
     */
    void showPlayer(@NotNull Player player);
    
    /**
     * Make the given player no longer see this Topbar
     * @param playerUUID the UUID of the player to hide this Topbar from. Must be the UUID of a valid player in this Topbar. 
     */
    void hidePlayer(@NotNull UUID playerUUID);
    
    /**
     * A bulk operation version of {@link Topbar#hidePlayer(UUID)}.
     * @param playerUUIDs a list of the UUIDs of each player to hide this Topbar from. Each UUID must be a valid player in this Topbar.
     */
    void hidePlayers(@NotNull List<@NotNull UUID> playerUUIDs);
    
    /**
     * Hides this Topbar from all players who are viewing it. 
     */
    void hideAllPlayers();
    
    /**
     * Set the left display of the Topbar to the given {@link Component}
     * @param left the component to set the left section to
     */
    void setLeft(@NotNull Component left);
    
    /**
     * Set the left display of this Topbar to the given {@link Component}, but only for the given player
     * @param playerUUID the UUID of the player to set the left section for. Must be a valid player in this Topbar
     * @param left the component to set the left section to
     */
    void setLeft(@NotNull UUID playerUUID, @NotNull Component left);
    
    /**
     * Set the middle display of the Topbar to the given {@link Component}
     * @param middle the component to set the middle section to
     */
    void setMiddle(@NotNull Component middle);
    
    /**
     * Set the middle display of this Topbar to the given {@link Component}, but only for the given player
     * @param playerUUID the UUID of the player to set the middle section for. Must be a valid player in this Topbar
     * @param middle the component to set the middle section to
     */
    void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle);
    
    /**
     * Set the right display of the Topbar to the given {@link Component}
     * @param right the component to set the right section to
     */
    void setRight(@NotNull Component right);
    
    /**
     * Set the right display of this Topbar to the given {@link Component}, but only for the given player
     * @param playerUUID the UUID of the player to set the right section for. Must be a valid player in this Topbar
     * @param right the component to set the right section to
     */
    void setRight(@NotNull UUID playerUUID, @NotNull Component right);
}
