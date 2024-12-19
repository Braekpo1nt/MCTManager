package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Participant. A participant is always a member of a {@link Team}.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Participant extends AudienceDelegate {
    
    /**
     * @param participants the participants list to get the list of players from
     * @return a list of the players contained in the given list of {@link Participant}s
     */
    public static List<Player> toPlayersList(Collection<Participant> participants) {
        return participants.stream().map(Participant::getPlayer).toList();
    }
    
    /**
     * The player object that this Participant represents
     */
    @EqualsAndHashCode.Include
    protected final @NotNull Player player;
    /**
     * The teamId of the team this Participant belongs to
     */
    protected final @NotNull String teamId;
    
    /**
     * @return the UUID of the player this Participant represents
     */
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
    
    /**
     * {@inheritDoc}
     * @return this Participant's {@link #player}. 
     */
    @Override
    public @NotNull Audience getAudience() {
        return player;
    }
    
    /**
     * Delegate for {@link Player#teleport(Location)}
     */
    public boolean teleport(@NotNull Location location) {
        return player.teleport(location);
    }
    
    /**
     * Delegate for {@link Player#setRespawnLocation(Location)}
     */
    public void setRespawnLocation(@Nullable Location location) {
        player.setRespawnLocation(location);
    }
    
    /**
     * Delegate for {@link Player#setRespawnLocation(Location,boolean)}
     */
    public void setRespawnLocation(@Nullable Location location, boolean force) {
        player.setRespawnLocation(location, force);
    }
    
    /**
     * Delegate for {@link Player#setGameMode(GameMode)}
     */
    public void setGameMode(@NotNull GameMode mode) {
        player.setGameMode(mode);
    }
    
}
