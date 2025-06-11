package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GatesOpeningState extends GameplayState {
    public GatesOpeningState(@NotNull ColossalCombatGame context) {
        super(context);
        context.openGates();
        spawnItemDrops();
        context.getPlugin().getServer().getScheduler().runTaskLater(context.getPlugin(), 
                () -> {
                    if (context.getConfig().shouldStartCaptureTheFlag() && suddenDeathThresholdReached()) {
                        context.setState(new SuddenDeathCountdownState(context));
                    } else {
                        context.setState(new FightingState(context));
                    }
                }, 
                config.getAntiSuffocationDuration());
    }
    
    private void spawnItemDrops() {
        if (config.getItemDropLocations() == null
                || config.getItemDropLocations().isEmpty()
                || config.getItemDrops() == null
                || config.getItemDrops().isEmpty()) {
            return;
        }
        for (int i = 0; i < config.getItemDropLocations().size(); i++) {
            Location location = config.getItemDropLocations().get(i);
            ItemStack item = config.getItemDrops().get(i);
            boolean glowing = config.getGlowingItemDrops().get(i);
            Item itemEntity = config.getWorld().dropItem(location, item);
            itemEntity.setGlowing(glowing);
        }
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
