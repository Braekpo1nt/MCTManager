package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SuddenDeathState extends GameplayState {
    
    private @Nullable UUID hasFlag;
    private @Nullable Location flagPosition;
    
    public SuddenDeathState(@NotNull ColossalCombatGame context) {
        super(context);
        context.titleAllParticipants(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("Sudden Death"))
                        .color(NamedTextColor.DARK_PURPLE),
                Component.empty()
                        .append(Component.text("Capture the Flag"))
                        .color(NamedTextColor.DARK_PURPLE)
        ));
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Sudden death has begun. Capture the flag to win!"))
                .color(NamedTextColor.DARK_PURPLE));
        flagPosition = config.getFlagLocation();
        BlockPlacementUtils.placeFlag(config.getFlagMaterial(), flagPosition, config.getInitialFlagDirection());
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        if (flagPosition != null) {
            flagPosition.getBlock().setType(Material.AIR);
        }
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ColossalParticipant participant) {
        if (hasFlag(participant.getUniqueId())) {
            dropFlag(participant);
        }
        super.onParticipantDeath(event, participant);
    }
    
    /**
     * @param uuid the UUID of the participant to check
     * @return true if the given participant UUID has the flag
     */
    private boolean hasFlag(@NotNull UUID uuid) {
        return Objects.equals(uuid, hasFlag);
    }
    
    /**
     * Place the flag in the location where the participant is, and announce
     * that the flag has been dropped.
     * @param participant the participant who dropped the flag.
     */
    private void dropFlag(ColossalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(" dropped the flag")));
            }
            case SOUTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(southTeam.getFormattedDisplayName())
                        .append(Component.text(" dropped the flag")));
            }
        }
        participant.getEquipment().setHelmet(null);
        flagPosition = BlockPlacementUtils.getBlockDropLocation(participant.getLocation());
        BlockPlacementUtils.placeFlag(config.getFlagMaterial(), flagPosition, participant.getFacing());
        hasFlag = null;
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        if (!participant.isAlive()) {
            return;
        }
        if (canPickUpFlag(participant.getLocation())) {
            pickupFlag(participant);
            return;
        }
        if (canDeliverFlag(participant)) {
            deliverFlag(participant);
        }
    }
    
    /**
     * @param participant the participant
     * @return true if the participant has the flag and is in the goal
     */
    private boolean canDeliverFlag(ColossalParticipant participant) {
        if (!hasFlag(participant.getUniqueId())) {
            return false;
        }
        switch (participant.getAffiliation()) {
            case NORTH -> {
                return config.getNorthGate()
                        .getFlagGoal()
                        .contains(participant.getLocation().toVector());
            }
            case SOUTH -> {
                return config.getSouthGate()
                        .getFlagGoal()
                        .contains(participant.getLocation().toVector());
            }
        }
        return false;
    }
    
    private void deliverFlag(ColossalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(" captured the flag!")));
                onTeamWinRound(northTeam);
            }
            case SOUTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(southTeam.getFormattedDisplayName())
                        .append(Component.text(" captured the flag!")));
                onTeamWinRound(southTeam);
            }
        }
    }
    
    /**
     * @param location the location to check
     * @return true if the flag is on the ground, and the given location's blockLocation is equal to the flag's current position
     */
    private boolean canPickUpFlag(Location location) {
        if (flagPosition == null) {
            return false;
        }
        return flagPosition.getBlockX() == location.getBlockX() && flagPosition.getBlockY() == location.getBlockY() && flagPosition.getBlockZ() == location.getBlockZ();
    }
    
    private void pickupFlag(ColossalParticipant participant) {
        if (flagPosition == null) {
            return;
        }
        switch (participant.getAffiliation()) {
            case NORTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(" has the flag!")));
            }
            case SOUTH -> {
                context.messageAllParticipants(Component.empty()
                        .append(southTeam.getFormattedDisplayName())
                        .append(Component.text(" has the flag!")));
            }
            case SPECTATOR -> {
                return;
            }
        }
        participant.getEquipment().setHelmet(new ItemStack(config.getFlagMaterial()));
        flagPosition.getBlock().setType(Material.AIR);
        flagPosition = null;
        hasFlag = participant.getUniqueId();
    }
}
