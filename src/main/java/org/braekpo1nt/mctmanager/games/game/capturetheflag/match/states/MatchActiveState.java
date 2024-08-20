package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagMatchOld;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Objects;

public class MatchActiveState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    private final Arena arena;
    private final GameManager gameManager;
    private final MatchPairing matchPairing;
    
    public MatchActiveState(CaptureTheFlagMatch context) {
        this.context = context;
        this.arena = context.getArena();
        this.gameManager = context.getGameManager();
        this.matchPairing = context.getMatchPairing();
        for (Player participant : context.getAllParticipants()) {
            participant.closeInventory();
        }
        context.messageAllParticipants(Component.text("Begin!"));
        context.openGlassBarriers();
    }
    
    @Override
    public void nextState() {
        onBothTeamsLose(Component.text("Time ran out."));
    }
    
    private void onBothTeamsLose(Component reason) {
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Game over. "))
                .append(reason));
        context.setState(new MatchOverState(context));
    }
    
    private void onParticipantWin(Player participant) {
        String winningTeam = gameManager.getTeamName(participant.getUniqueId());
        String losingTeam = matchPairing.northTeam();
        if (winningTeam.equals(matchPairing.northTeam())) {
            losingTeam = matchPairing.southTeam();
        }
        Component winningTeamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        Component losingTeamDisplayName = gameManager.getFormattedTeamDisplayName(losingTeam);
        context.getParentContext().messageAllParticipants(Component.empty()
                .append(winningTeamDisplayName)
                .append(Component.text(" captured "))
                .append(losingTeamDisplayName)
                .append(Component.text("'s flag!"))
                .color(NamedTextColor.YELLOW));
        gameManager.awardPointsToTeam(winningTeam, context.getConfig().getWinScore());
        context.setState(new MatchOverState(context));
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.initializeParticipant(participant);
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        context.getParticipantsAreAlive().put(participant.getUniqueId(), false);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        Location lookLocation;
        if (matchPairing.northTeam().equals(teamId)) {
            lookLocation = arena.northFlag();
        } else {
            lookLocation = arena.southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        if (matchPairing.northTeam().equals(teamId)) {
            onNorthParticipantQuit(participant);
        } else {
            onSouthParticipantQuit(participant);
        }
    }
    
    private void onNorthParticipantQuit(Player participant) {
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant,
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        context.resetParticipant(participant);
        context.getNorthParticipants().remove(participant);
        context.getAllParticipants().remove(participant);
    }
    
    private void onSouthParticipantQuit(Player participant) {
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant,
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        context.resetParticipant(participant);
        context.getSouthParticipants().remove(participant);
        context.getAllParticipants().remove(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        // do nothing
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player participant = event.getPlayer();
        if (!context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            return;
        }
        if (context.getNorthParticipants().contains(participant)) {
            onNorthParticipantMove(participant);
        } else {
//            onSouthParticipantMove(participant);
        }
    }
    
    private void onNorthParticipantMove(Player participant) {
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
            recoverNorthFlag(participant);
            return;
        }
    }
    
    /**
     * Returns true if the south flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#getSouthFlagPosition()}}
     * @param location The location to check
     * @return Whether the south flag is dropped and the location is on the south flag
     */
    private boolean canPickUpSouthFlag(Location location) {
        if (context.getSouthFlagPosition() == null) {
            return false;
        }
        return context.getSouthFlagPosition().getBlockX() == location.getBlockX() && context.getSouthFlagPosition().getBlockY() == location.getBlockY() && context.getSouthFlagPosition().getBlockZ() == location.getBlockZ();
    }
    
    private synchronized void pickUpSouthFlag(Player northParticipant) {
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was captured"))
                .color(NamedTextColor.DARK_RED));
        context.titleSouthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("You captured the flag"))
                .color(NamedTextColor.GREEN));
        context.titleNorthParticipants(UIUtils.defaultTitle(
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
    
    private boolean canDeliverSouthFlag(Player northParticipant) {
        if (!hasSouthFlag(northParticipant)) {
            return false;
        }
        Location location = northParticipant.getLocation();
        return arena.northFlag().getBlockX() == location.getBlockX() && arena.northFlag().getBlockY() == location.getBlockY() && arena.northFlag().getBlockZ() == location.getBlockZ();
    }
    
    private void deliverSouthFlag(Player northParticipant) {
        BlockPlacementUtils.placeFlag(context.getSouthBanner(), arena.northFlag(), BlockFace.NORTH);
        northParticipant.getInventory().remove(context.getSouthBanner());
        onParticipantWin(northParticipant);
    }
    
    private boolean hasSouthFlag(Player northParticipant) {
        return Objects.equals(context.getHasSouthFlag(), northParticipant);
    }
    
    private boolean canRecoverNorthFlag(Location location) {
        if (context.getNorthFlagPosition() == null) {
            return false;
        }
        boolean alreadyRecovered = context.getNorthFlagPosition().getBlockX() == arena.northFlag().getBlockX() && context.getNorthFlagPosition().getBlockY() == arena.northFlag().getBlockY() && context.getNorthFlagPosition().getBlockZ() == arena.northFlag().getBlockZ();
        return !alreadyRecovered && canPickUpNorthFlag(location);
    }
    
    /**
     * Returns true if the north flag is dropped on the ground, and the given location's blockLocation is equal to {@link CaptureTheFlagMatch#getNorthFlagPosition()}
     * @param location The location to check
     * @return Whether the north flag is dropped and the location is on the north flag
     */
    private boolean canPickUpNorthFlag(Location location) {
        if (context.getNorthFlagPosition() == null) {
            return false;
        }
        return context.getNorthFlagPosition().getBlockX() == location.getBlockX() && context.getNorthFlagPosition().getBlockY() == location.getBlockY() && context.getNorthFlagPosition().getBlockZ() == location.getBlockZ();
    }
    
    private void recoverNorthFlag(Player northParticipant) {
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        context.titleNorthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.GREEN)
        ));
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        context.titleSouthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.getNorthFlagPosition().getBlock().setType(Material.AIR);
        context.setNorthFlagPosition(arena.northFlag());
        BlockPlacementUtils.placeFlag(context.getNorthBanner(), context.getNorthFlagPosition(), BlockFace.SOUTH);
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
}
