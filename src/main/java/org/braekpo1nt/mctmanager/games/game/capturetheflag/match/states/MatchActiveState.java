package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

public class MatchActiveState extends CaptureTheFlagMatchStateBase {
    
    private final Arena arena;
    
    public MatchActiveState(CaptureTheFlagMatch context) {
        super(context);
        this.arena = context.getArena();
    }
    
    @Override
    public void enter() {
        for (CTFMatchParticipant participant : context.getParticipants().values()) {
            participant.closeInventory();
            participant.setAlive(true);
        }
        context.messageAllParticipants(Component.text("Begin!"));
        context.openGlassBarriers();
        if (context.getNorthTeam().size() == 0) {
            onTeamForfeit(context.getNorthTeam());
        } else if (context.getSouthTeam().size() == 0) {
            onTeamForfeit(context.getSouthTeam());
        }
    }
    
    @Override
    public void exit() {
        // do nothing
    }
    
    private void onTeamForfeit(@NotNull Team forfeit) {
        context.messageAllParticipants(Component.empty()
                .append(forfeit.getFormattedDisplayName())
                .append(Component.text(" is absent, match cancelled.")));
        context.setState(new MatchOverState(context));
    }
    
    @Override
    public void nextState() {
        onBothTeamsLose(Component.text("Time ran out."));
    }
    
    private void onBothTeamsLose(Component reason) {
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Match over. "))
                .append(reason));
        Audience.audience(context.getParticipants().values()).showTitle(
                Title.title(
                        Component.empty()
                                .append(Component.text("Match Over!"))
                                .color(NamedTextColor.RED),
                        reason,
                        UIUtils.DEFAULT_TIMES
                )
        );
        context.setState(new MatchOverState(context));
    }
    
    private void onTeamWin(CTFMatchTeam winner, CTFMatchTeam loser) {
        context.getParentContext().messageAllParticipants(Component.empty()
                .append(winner.getFormattedDisplayName())
                .append(Component.text(" captured "))
                .append(loser.getFormattedDisplayName())
                .append(Component.text("'s flag!"))
                .color(NamedTextColor.YELLOW));
        context.awardPoints(winner, context.getConfig().getWinScore());
        
        showWinLoseTitles(winner, loser);
        context.setState(new MatchOverState(context));
    }
    
    private void showWinLoseTitles(CTFMatchTeam winner, CTFMatchTeam loser) {
        winner.showTitle(
                Title.title(
                        Component.empty()
                                .append(Component.text("Match Over!"))
                                .color(NamedTextColor.RED),
                        Component.empty()
                                .append(Component.text("You won!"))
                                .color(NamedTextColor.GREEN),
                        UIUtils.DEFAULT_TIMES
                )
        );
        loser.showTitle(
                Title.title(
                        Component.empty()
                                .append(Component.text("Match Over!"))
                                .color(NamedTextColor.RED),
                        Component.empty()
                                .append(Component.text("You lost"))
                                .color(NamedTextColor.RED),
                        UIUtils.DEFAULT_TIMES
                )
        );
    }
    
    private void onParticipantGetKill(@NotNull CTFMatchParticipant killer, @NotNull Participant killed) {
        if (!context.getParticipants().containsKey(killer.getUniqueId())) {
            return;
        }
        context.addKill(killer);
        UIUtils.showKillTitle(killer, killed);
        context.awardPoints(killer, context.getConfig().getKillScore());
    }
    
    @Override
    public void onParticipantRejoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setAlive(false);
        participant.teleport(context.getConfig().getSpawnObservatory());
        Location lookLocation;
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            lookLocation = arena.northFlag();
        } else {
            lookLocation = arena.southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onNewParticipantJoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(false);
        participant.teleport(context.getConfig().getSpawnObservatory());
        Location lookLocation;
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            lookLocation = arena.northFlag();
        } else {
            lookLocation = arena.southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onParticipantQuit(CTFMatchParticipant participant, CTFMatchTeam team) {
        CTFMatchParticipant ctfMatchParticipant = context.getParticipants().get(participant.getUniqueId());
        if (ctfMatchParticipant == null) {
            return;
        }
        if (ctfMatchParticipant.isAlive()) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(),
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, 0, 0, 0, deathMessage, true);
            this.onParticipantDeath(fakeDeathEvent, participant);
        }
        super.onParticipantQuit(participant, team);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFMatchParticipant participant) {
        if (participant.isAlive()) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagMatch.MatchActiveState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFMatchParticipant participant) {
        if (participant.isAlive()) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Checks if all participants are dead.
     *
     * @return True if all participants are dead, false if at least one participant is alive
     */
    private boolean allParticipantsAreDead() {
        return context.getParticipants().values().stream().noneMatch(CTFMatchParticipant::isAlive);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull CTFMatchParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        
        participant.setAlive(false);
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Handle flag dropping based on affiliation
        // new code start
        event.setShowDeathMessages(false);
        Component deathMessage = event.deathMessage();
//        event.deathMessage(null);
        if (deathMessage != null) {
            context.messageAllParticipants(deathMessage);
        }
        // new code stop
        
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            if (hasSouthFlag(participant)) {
                dropSouthFlag(participant);
            }
        } else {
            if (hasNorthFlag(participant)) {
                dropNorthFlag(participant);
            }
        }
        
        context.updateAliveStatus(participant.getAffiliation());
        context.addDeath(participant);
        
        // Handle killer logic
        Player killerPlayer = participant.getKiller();
        if (killerPlayer != null) {
            CTFMatchParticipant killer = context.getParticipants().get(killerPlayer.getUniqueId());
            if (killer != null) {
                onParticipantGetKill(killer, participant);
            }
        }
        
        // new code start
        event.setShowDeathMessages(false);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            context.messageAllParticipants(deathMessage);
            context.getParentContext().messageOnDeckParticipants(deathMessage);
        }
        // new code stop
        
        if (allParticipantsAreDead()) {
            onBothTeamsLose(Component.text("Both teams are dead."));
        }
    }
    
    private void dropSouthFlag(Participant northParticipant) {
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.GREEN)
        ));
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.DARK_RED)
        ));
        northParticipant.getEquipment().setHelmet(null);
        context.setSouthFlagPosition(BlockPlacementUtils.getBlockDropLocation(northParticipant.getLocation()));
        BlockPlacementUtils.placeFlag(context.getSouthBanner(), context.getSouthFlagPosition(), northParticipant.getFacing());
        context.setHasSouthFlag(null);
    }
    
    private void dropNorthFlag(Participant southParticipant) {
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.GREEN)
        ));
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.DARK_RED)
        ));
        southParticipant.getEquipment().setHelmet(null);
        context.setNorthFlagPosition(BlockPlacementUtils.getBlockDropLocation(southParticipant.getLocation()));
        BlockPlacementUtils.placeFlag(context.getNorthBanner(), context.getNorthFlagPosition(), southParticipant.getFacing());
        context.setHasNorthFlag(null);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull CTFMatchParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            onNorthParticipantMove(participant);
        } else {
            onSouthParticipantMove(participant);
        }
    }
    
    // North participant move start
    private void onNorthParticipantMove(Participant participant) {
        Location location = participant.getLocation();
        if (canPickUpSouthFlag(location)) {
            pickUpSouthFlag(participant);
            return;
        }
        if (canDeliverSouthFlag(participant)) {
            deliverSouthFlag(participant);
            return;
        }
        if (canRecoverNorthFlag(location)) {
            recoverNorthFlag();
        }
    }
    
    /**
     * Returns true if the south flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#getSouthFlagPosition()}}
     *
     * @param location The location to check
     * @return Whether the south flag is dropped and the location is on the south flag
     */
    private boolean canPickUpSouthFlag(Location location) {
        if (context.getSouthFlagPosition() == null) {
            return false;
        }
        return context.getSouthFlagPosition().getBlockX() == location.getBlockX() && context.getSouthFlagPosition().getBlockY() == location.getBlockY() && context.getSouthFlagPosition().getBlockZ() == location.getBlockZ();
    }
    
    private synchronized void pickUpSouthFlag(Participant northParticipant) {
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was captured"))
                .color(NamedTextColor.DARK_RED));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("You captured the flag"))
                .color(NamedTextColor.GREEN));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.GREEN)
        ));
        northParticipant.getEquipment().setHelmet(new ItemStack(context.getSouthBanner()));
        context.getSouthFlagPosition().getBlock().setType(Material.AIR);
        context.setSouthFlagPosition(null);
        context.setHasSouthFlag(northParticipant);
    }
    
    private boolean canDeliverSouthFlag(Participant northParticipant) {
        if (!hasSouthFlag(northParticipant)) {
            return false;
        }
        Location location = northParticipant.getLocation();
        return arena.northFlag().getBlockX() == location.getBlockX() && arena.northFlag().getBlockY() == location.getBlockY() && arena.northFlag().getBlockZ() == location.getBlockZ();
    }
    
    private void deliverSouthFlag(Participant northParticipant) {
        BlockPlacementUtils.placeFlag(context.getSouthBanner(), arena.northFlag(), BlockFace.NORTH);
        northParticipant.getInventory().remove(context.getSouthBanner());
        onTeamWin(context.getNorthTeam(), context.getSouthTeam());
    }
    
    private boolean hasSouthFlag(Participant northParticipant) {
        return Objects.equals(context.getHasSouthFlag(), northParticipant);
    }
    
    private boolean canRecoverNorthFlag(Location location) {
        if (context.getNorthFlagPosition() == null) {
            return false;
        }
        boolean alreadyRecovered = context.getNorthFlagPosition().getBlockX() == arena.northFlag().getBlockX() && context.getNorthFlagPosition().getBlockY() == arena.northFlag().getBlockY() && context.getNorthFlagPosition().getBlockZ() == arena.northFlag().getBlockZ();
        return !alreadyRecovered && canPickUpNorthFlag(location);
    }
    
    private void recoverNorthFlag() {
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.GREEN)
        ));
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.getNorthFlagPosition().getBlock().setType(Material.AIR);
        context.setNorthFlagPosition(arena.northFlag());
        BlockPlacementUtils.placeFlag(context.getNorthBanner(), context.getNorthFlagPosition(), BlockFace.SOUTH);
    }
    
    // North participant move end
    // South participant move start
    
    private void onSouthParticipantMove(Participant southParticipant) {
        Location location = southParticipant.getLocation();
        if (canPickUpNorthFlag(location)) {
            pickUpNorthFlag(southParticipant);
            return;
        }
        if (canDeliverNorthFlag(southParticipant)) {
            deliverNorthFlag(southParticipant);
            return;
        }
        if (canRecoverSouthFlag(location)) {
            recoverSouthFlag();
        }
    }
    
    /**
     * Returns true if the north flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#getNorthFlagPosition()}
     *
     * @param location The location to check
     * @return Whether the north flag is dropped and the location is on the north flag
     */
    private boolean canPickUpNorthFlag(Location location) {
        if (context.getNorthFlagPosition() == null) {
            return false;
        }
        return context.getNorthFlagPosition().getBlockX() == location.getBlockX() && context.getNorthFlagPosition().getBlockY() == location.getBlockY() && context.getNorthFlagPosition().getBlockZ() == location.getBlockZ();
    }
    
    private synchronized void pickUpNorthFlag(Participant southParticipant) {
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was captured!"))
                .color(NamedTextColor.DARK_RED));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("You captured the flag!"))
                .color(NamedTextColor.GREEN));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.GREEN)
        ));
        southParticipant.getEquipment().setHelmet(new ItemStack(context.getNorthBanner()));
        context.getNorthFlagPosition().getBlock().setType(Material.AIR);
        context.setNorthFlagPosition(null);
        context.setHasNorthFlag(southParticipant);
    }
    
    private boolean canDeliverNorthFlag(Participant southParticipant) {
        if (!hasNorthFlag(southParticipant)) {
            return false;
        }
        Location location = southParticipant.getLocation();
        return arena.southFlag().getBlockX() == location.getBlockX() && arena.southFlag().getBlockY() == location.getBlockY() && arena.southFlag().getBlockZ() == location.getBlockZ();
    }
    
    private boolean hasNorthFlag(Participant southParticipant) {
        return Objects.equals(context.getHasNorthFlag(), southParticipant);
    }
    
    private void deliverNorthFlag(Participant southParticipant) {
        BlockPlacementUtils.placeFlag(context.getNorthBanner(), arena.southFlag(), BlockFace.SOUTH);
        southParticipant.getInventory().remove(context.getNorthBanner());
        onTeamWin(context.getSouthTeam(), context.getNorthTeam());
    }
    
    private boolean canRecoverSouthFlag(Location location) {
        if (context.getSouthFlagPosition() == null) {
            return false;
        }
        boolean alreadyRecovered = context.getSouthFlagPosition().getBlockX() == arena.southFlag().getBlockX() && context.getSouthFlagPosition().getBlockY() == arena.southFlag().getBlockY() && context.getSouthFlagPosition().getBlockZ() == arena.southFlag().getBlockZ();
        return !alreadyRecovered && canPickUpSouthFlag(location);
    }
    
    private void recoverSouthFlag() {
        context.getSouthTeam().sendMessage(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        context.getSouthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.GREEN)
        ));
        context.getNorthTeam().sendMessage(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        context.getNorthTeam().showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.getSouthFlagPosition().getBlock().setType(Material.AIR);
        context.setSouthFlagPosition(arena.southFlag());
        BlockPlacementUtils.placeFlag(context.getSouthBanner(), context.getSouthFlagPosition(), BlockFace.NORTH);
    }
    
    // South participant move end
    
}
