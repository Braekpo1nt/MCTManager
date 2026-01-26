package org.braekpo1nt.mctmanager.games.base.listeners;

import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Similar to {@link PreventHungerLoss}, but restricted to specific participants (such as spectators)
 * as determined by a provided {@link #shouldPreventHungerLoss} method
 * @param <P>
 */
public class PreventHungerLossSpecific<P extends ParticipantData> extends GameListener<P> {
    private final Function<P, Boolean> shouldPreventHungerLoss;
    
    /**
     * @param context the context
     * @param shouldPreventHungerLoss returns true if the given participant should not lose
     * their hunger, false otherwise
     */
    public PreventHungerLossSpecific(@NotNull GameBase<P, ?, ?, ?, ?> context, Function<P, Boolean> shouldPreventHungerLoss) {
        super(context);
        this.shouldPreventHungerLoss = shouldPreventHungerLoss;
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        P participant = context.getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        if (!shouldPreventHungerLoss.apply(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
}
