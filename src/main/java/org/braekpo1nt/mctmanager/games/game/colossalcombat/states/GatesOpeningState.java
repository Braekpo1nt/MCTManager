package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class GatesOpeningState extends GameplayState {
    public GatesOpeningState(@NotNull ColossalCombatGame context) {
        super(context);
        context.openGates();
        context.getPlugin().getServer().getScheduler().runTaskLater(context.getPlugin(), 
                () -> context.setState(new FightingState(context)), 
                config.getAntiSuffocationDuration());
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ColossalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                if (config.getNorthGate()
                        .getAntiSuffocationArea().contains(event.getTo().toVector())) {
                    event.setCancelled(true);
                }
            }
            case SOUTH -> {
                if (config.getSouthGate()
                        .getAntiSuffocationArea().contains(event.getTo().toVector())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
