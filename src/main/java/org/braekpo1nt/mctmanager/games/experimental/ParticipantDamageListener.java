package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ParticipantDamageListener<P> extends GameListener<P> {
    public ParticipantDamageListener(@NotNull GameData<P> gameData) {
        super(gameData);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        P participant = gameData.getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        onParticipantDamage(event, participant);
    }
    
    protected abstract void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull P participant);
}
