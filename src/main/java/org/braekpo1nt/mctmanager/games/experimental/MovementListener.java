package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public abstract class MovementListener<P> extends GameListener<P> {
    public MovementListener(@NotNull GameData<P> gameData) {
        super(gameData);
    }
    
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {
        P participant = gameData.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        playerMoveEvent(event, participant);
    }
    
    /**
     * @param event the {@link PlayerMoveEvent} event which was triggered by the given participant's movement
     * @param participant the participant who moved
     */
    protected abstract void playerMoveEvent(PlayerMoveEvent event, @NotNull P participant);
}
