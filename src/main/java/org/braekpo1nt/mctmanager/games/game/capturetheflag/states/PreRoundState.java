package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.RoundManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class PreRoundState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    private final GameManager gameManager;
    private final BattleTopbar topbar;
    private final RoundManager roundManager;
    
    public PreRoundState(CaptureTheFlagGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.topbar = context.getTopbar();
        this.roundManager = context.getRoundManager();
        
        context.updateRoundLine();
        for (Participant participant : context.getParticipants().values()) {
            announceMatchToParticipant(participant);
        }
        setUpTopbarForRound();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getMatchesStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> {
                    context.setState(new RoundActiveState(context));
                })
                .build());
    }
    
    private void announceMatchToParticipant(Participant participant) {
        String teamId = participant.getTeamId();
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
        List<MatchPairing> currentRound = roundManager.getCurrentRound();
        String oppositeTeamId = RoundManager.getOppositeTeam(teamId, currentRound);
        Component roundDisplay = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text(":"));
        if (oppositeTeamId != null) {
            Component oppositeTeamDisplayName = gameManager.getFormattedTeamDisplayName(oppositeTeamId);
            participant.sendMessage(Component.empty()
                    .append(teamDisplayName)
                    .append(Component.text(" is competing against "))
                    .append(oppositeTeamDisplayName)
                    .append(Component.text(" this round."))
                    .color(NamedTextColor.YELLOW));
            participant.showTitle(UIUtils.defaultTitle(
                    roundDisplay,
                    Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" vs "))
                            .append(oppositeTeamDisplayName)
            ));
        } else {
            participant.sendMessage(Component.empty()
                    .append(teamDisplayName)
                    .append(Component.text(" is on-deck this round."))
                    .color(NamedTextColor.YELLOW));
            participant.showTitle(UIUtils.defaultTitle(
                    roundDisplay,
                    Component.empty()
                            .append(teamDisplayName)
                            .append(Component.text(" is on-deck"))));
        }
    }
    
    private void setUpTopbarForRound() {
        List<MatchPairing> roundMatchPairings = roundManager.getCurrentRound();
        topbar.removeAllTeamPairs();
        for (MatchPairing mp : roundMatchPairings) {
            TextColor northColor = gameManager.getTeamColor(mp.northTeam());
            TextColor southColor = gameManager.getTeamColor(mp.southTeam());
            topbar.addTeam(mp.northTeam(), northColor);
            topbar.addTeam(mp.southTeam(), southColor);
            topbar.linkTeamPair(mp.northTeam(), mp.southTeam());
            int northAlive = 0;
            int southAlive = 0;
            for (Participant participant : context.getParticipants().values()) {
                String teamId = participant.getTeamId();
                if (mp.northTeam().equals(teamId)) {
                    topbar.linkToTeam(participant.getUniqueId(), teamId);
                    northAlive++;
                } else if (mp.southTeam().equals(teamId)) {
                    topbar.linkToTeam(participant.getUniqueId(), teamId);
                    southAlive++;
                }
            }
            topbar.setMembers(mp.northTeam(), northAlive, 0);
            topbar.setMembers(mp.southTeam(), southAlive, 0);
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.initializeParticipant(participant);
        if (!context.getRoundManager().containsTeamId(participant.getTeamId())) {
            List<String> teamIds = Participant.getTeamIds(context.getParticipants());
            context.getRoundManager().regenerateRounds(teamIds, context.getConfig().getArenas().size());
        }
        context.updateRoundLine();
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        announceMatchToParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        List<String> teamIds = Participant.getTeamIds(context.getParticipants());
        if (!teamIds.contains(participant.getTeamId())) {
            context.getRoundManager().regenerateRounds(teamIds, context.getConfig().getArenas().size());
            context.updateRoundLine();
        }
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagGame.PreRoundState.onPlayerDamage() cancelled");
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
}
