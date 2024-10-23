package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
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
        if (context.getNorthParticipants().isEmpty()) {
            onTeamForfeit(matchPairing.northTeam());
        } else if (context.getSouthParticipants().isEmpty()) {
            onTeamForfeit(matchPairing.southTeam());
        }
    }
    
    private void onTeamForfeit(String forfeit) {
        Component displayName = gameManager.getFormattedTeamDisplayName(forfeit);
        context.messageAllParticipants(Component.empty()
                .append(displayName)
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
        Audience.audience(context.getAllParticipants()).showTitle(
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
    
    private void onTeamWin(String winningTeam) {
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
        
        showWinLoseTitles(winningTeam);
        context.setState(new MatchOverState(context));
    }
    
    private void showWinLoseTitles(String winningTeam) {
        for (Player participant : context.getAllParticipants()) {
            String teamId = gameManager.getTeamId(participant.getUniqueId());
            if (winningTeam.equals(teamId)) {
                participant.showTitle(
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
            } else {
                participant.showTitle(
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
        }
    }
    
    private void onParticipantGetKill(@NotNull Player killer, @NotNull Player killed) {
        if (!context.getAllParticipants().contains(killer)) {
            return;
        }
        context.getParentContext().addKill(killer.getUniqueId());
        UIUtils.showKillTitle(killer, killed);
        gameManager.awardPointsToParticipant(killer, context.getConfig().getKillScore());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.getParticipantsAreAlive().put(participant.getUniqueId(), false);
        context.initializeParticipant(participant);
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
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
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant,
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        context.resetParticipant(participant);
        context.getAllParticipants().remove(participant);
        if (matchPairing.northTeam().equals(teamId)) {
            context.getNorthParticipants().remove(participant);
        } else {
            context.getSouthParticipants().remove(participant);
        }
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagMatch.MatchActiveState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        // don't let them drop items from their inventory
        if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
            event.setCancelled(true);
            return;
        }
        // don't let them remove their armor
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        if (!context.getAllParticipants().contains(killed)) {
            return;
        }
        killed.getInventory().clear();
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "CaptureTheFlagMatch.MatchActiveState.onPlayerDeath() cancelled");
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed.getKiller(), killed);
        }
        if (allParticipantsAreDead()) {
            onBothTeamsLose(Component.text("Both teams are dead."));
        }
    }
    
    /**
     * Checks if all participants are dead.
     * @return True if all participants are dead, false otherwise
     */
    private boolean allParticipantsAreDead() {
        return !context.getParticipantsAreAlive().containsValue(true);
    }
    
    private void onParticipantDeath(Player killed) {
        context.getParticipantsAreAlive().put(killed.getUniqueId(), false);
        int alive = 0;
        int dead = 0;
        if (context.getNorthParticipants().contains(killed)) {
            if (hasSouthFlag(killed)) {
                dropSouthFlag(killed);
            }
            alive = countAlive(context.getNorthParticipants());
            dead = context.getNorthParticipants().size() - alive;
        } else if (context.getSouthParticipants().contains(killed)) {
            if (hasNorthFlag(killed)){
                dropNorthFlag(killed);
            }
            alive = countAlive(context.getSouthParticipants());
            dead = context.getSouthParticipants().size() - alive;
        }
        
        ParticipantInitializer.resetHealthAndHunger(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        killed.teleport(context.getConfig().getSpawnObservatory());
        killed.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        killed.lookAt(arena.northFlag().getX(), arena.northFlag().getY(), arena.northFlag().getZ(), LookAnchor.EYES);
        
        String teamId = gameManager.getTeamId(killed.getUniqueId());
        context.getTopbar().setMembers(teamId, alive, dead);
        context.getParentContext().addDeath(killed.getUniqueId());
    }
    
    private int countAlive(List<Player> participants) {
        int living = 0;
        for (Player participant : participants) {
            if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
                living++;
            }
        }
        return living;
    }
    
    private void dropSouthFlag(Player northParticipant) {
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        context.titleSouthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.GREEN)
        ));
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        context.titleNorthParticipants(UIUtils.defaultTitle(
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
    
    private void dropNorthFlag(Player southParticipant) {
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was dropped"))
                .color(NamedTextColor.GREEN));
        context.titleNorthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag dropped"))
                        .color(NamedTextColor.GREEN)
        ));
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("You dropped the flag"))
                .color(NamedTextColor.DARK_RED));
        context.titleSouthParticipants(UIUtils.defaultTitle(
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
    public void onPlayerMove(PlayerMoveEvent event) {
        Player participant = event.getPlayer();
        if (!context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            return;
        }
        if (context.getNorthParticipants().contains(participant)) {
            onNorthParticipantMove(participant);
        } else {
            onSouthParticipantMove(participant);
        }
    }
    
    // North participant move start
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
            recoverNorthFlag();
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
        String winningTeam = gameManager.getTeamId(northParticipant.getUniqueId());
        onTeamWin(winningTeam);
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
    
    private void recoverNorthFlag() {
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
    
    // North participant move end
    // South participant move start
    
    private void onSouthParticipantMove(Player southParticipant) {
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
     * @param location The location to check
     * @return Whether the north flag is dropped and the location is on the north flag
     */
    private boolean canPickUpNorthFlag(Location location) {
        if (context.getNorthFlagPosition() == null) {
            return false;
        }
        return context.getNorthFlagPosition().getBlockX() == location.getBlockX() && context.getNorthFlagPosition().getBlockY() == location.getBlockY() && context.getNorthFlagPosition().getBlockZ() == location.getBlockZ();
    }
    
    private synchronized void pickUpNorthFlag(Player southParticipant) {
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("Your flag was captured!"))
                .color(NamedTextColor.DARK_RED));
        context.titleNorthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag captured"))
                        .color(NamedTextColor.DARK_RED)
        ));
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("You captured the flag!"))
                .color(NamedTextColor.GREEN));
        context.titleSouthParticipants(UIUtils.defaultTitle(
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
    
    private boolean canDeliverNorthFlag(Player southParticipant) {
        if (!hasNorthFlag(southParticipant)) {
            return false;
        }
        Location location = southParticipant.getLocation();
        return arena.southFlag().getBlockX() == location.getBlockX() && arena.southFlag().getBlockY() == location.getBlockY() && arena.southFlag().getBlockZ() == location.getBlockZ();
    }
    
    private boolean hasNorthFlag(Player southParticipant) {
        return Objects.equals(context.getHasNorthFlag(), southParticipant);
    }
    
    private void deliverNorthFlag(Player southParticipant) {
        BlockPlacementUtils.placeFlag(context.getNorthBanner(), arena.southFlag(), BlockFace.SOUTH);
        southParticipant.getInventory().remove(context.getNorthBanner());
        String winningTeam = gameManager.getTeamId(southParticipant.getUniqueId());
        onTeamWin(winningTeam);
    }
    
    private boolean canRecoverSouthFlag(Location location) {
        if (context.getSouthFlagPosition() == null) {
            return false;
        }
        boolean alreadyRecovered = context.getSouthFlagPosition().getBlockX() == arena.southFlag().getBlockX() && context.getSouthFlagPosition().getBlockY() == arena.southFlag().getBlockY() && context.getSouthFlagPosition().getBlockZ() == arena.southFlag().getBlockZ();
        return !alreadyRecovered && canPickUpSouthFlag(location);
    }
    
    private void recoverSouthFlag() {
        context.messageSouthParticipants(Component.empty()
                .append(Component.text("Your flag was recovered"))
                .color(NamedTextColor.GREEN));
        context.titleSouthParticipants(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("flag recovered"))
                        .color(NamedTextColor.GREEN)
        ));
        context.messageNorthParticipants(Component.empty()
                .append(Component.text("Opponents' flag was recovered"))
                .color(NamedTextColor.DARK_RED));
        context.titleNorthParticipants(UIUtils.defaultTitle(
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
