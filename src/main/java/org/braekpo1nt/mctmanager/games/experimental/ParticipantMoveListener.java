package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ParticipantMoveListener<P> extends GameListener<P> {
    public ParticipantMoveListener(@NotNull GameData<P> gameData) {
        super(gameData);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        P participant = gameData.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        onParticipantMove(event, participant);
    }
    
    /**
     * @param event the {@link PlayerMoveEvent} event which was triggered by the given participant's movement
     * @param participant the participant who moved
     */
    protected abstract void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull P participant);
}
