package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.RoundManager;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.entity.Player;
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
        
        announceMatchToParticipants();
        setUpTopbarForRound();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> {
                    context.setState(new RoundActiveState(context));
                })
                .build());
    }
    
    private void announceMatchToParticipants() {
        List<MatchPairing> currentRound = roundManager.getCurrentRound();
        Component roundDisplay = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getCurrentRoundIndex() + 1))
                .append(Component.text(":"));
        for (Player participant : context.getParticipants()) {
            String teamId = gameManager.getTeamName(participant.getUniqueId());
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
            String oppositeTeamId = RoundManager.getOppositeTeam(teamId, currentRound);
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
                                .append(Component.text(" is on-deck"))
                ));
            }
        }
    }
    
    private void setUpTopbarForRound() {
        List<MatchPairing> roundMatchPairings = roundManager.getCurrentRound();
        topbar.removeAllTeamPairs();
        for (MatchPairing mp : roundMatchPairings) {
            NamedTextColor northColor = gameManager.getTeamNamedTextColor(mp.northTeam());
            NamedTextColor southColor = gameManager.getTeamNamedTextColor(mp.southTeam());
            topbar.addTeam(mp.northTeam(), northColor);
            topbar.addTeam(mp.southTeam(), southColor);
            topbar.linkTeamPair(mp.northTeam(), mp.southTeam());
            int northAlive = 0;
            int southAlive = 0;
            for (Player participant : context.getParticipants()) {
                String teamId = gameManager.getTeamName(participant.getUniqueId());
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
        topbar.setNoTeamLeft(Component.text("On Deck")
                .color(NamedTextColor.GRAY));
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.initializeParticipant(participant);
        // TODO implement how roundManager handles new teams joining
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getCurrentRoundIndex() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        context.getSidebar().updateLine("round", roundLine);
        context.getAdminSidebar().updateLine("round", roundLine);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
}
