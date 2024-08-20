package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MatchOverState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    
    public MatchOverState(CaptureTheFlagMatch context) {
        this.context = context;
        Audience.audience(context.getAllParticipants()).showTitle(UIUtils.matchOverTitle());
        for (Player participant : context.getAllParticipants()) {
            if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
                participant.teleport(context.getConfig().getSpawnObservatory());
                participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
                participant.getInventory().clear();
                participant.closeInventory();
                ParticipantInitializer.resetHealthAndHunger(participant);
                ParticipantInitializer.clearStatusEffects(participant);
            }
        }
        context.getMatchIsOver().accept(context);
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
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            lookLocation = context.getArena().northFlag();
        } else {
            lookLocation = context.getArena().southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.resetParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            context.getNorthParticipants().remove(participant);
        } else if (context.getMatchPairing().southTeam().equals(teamId)) {
            context.getSouthParticipants().remove(participant);
        }
        context.getAllParticipants().remove(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        // do nothing
    }
    
    @Override
    public void nextState() {
        // do nothing
    }
}
