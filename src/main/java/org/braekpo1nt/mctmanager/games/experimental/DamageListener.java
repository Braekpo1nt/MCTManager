package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public abstract class DamageListener<P> extends GameListener<P> {
    public DamageListener(@NotNull GameData<P> gameData) {
        super(gameData);
    }
    
    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        P participant = gameData.getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        entityDamageEvent(event, participant);
    }
    
    protected abstract void entityDamageEvent(EntityDamageEvent event, @NotNull P participant);
}
