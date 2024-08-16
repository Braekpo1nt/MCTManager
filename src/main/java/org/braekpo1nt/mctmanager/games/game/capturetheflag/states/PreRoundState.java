package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagRoundOld;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PreRoundState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final BattleTopbar topbar;
    private @Nullable CaptureTheFlagRoundOld currentRound;
    
    public PreRoundState(CaptureTheFlagGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        this.topbar = context.getTopbar();
        List<String> teams = gameManager.getTeamNames(context.getParticipants());
        context.getRoundManager().start(teams, this::startNextRound, context::stop);
    }
    
    public void startNextRound(List<String> participantTeams, List<MatchPairing> roundMatchPairings) {
        
        //TODO: null capture the flag game
        currentRound = new CaptureTheFlagRoundOld(null, context.getPlugin(), context.getGameManager(), context.getConfig(), roundMatchPairings, context.getSidebar(), context.getAdminSidebar(), context.getTopbar());
        List<Player> roundParticipants = new ArrayList<>();
        List<Player> onDeckParticipants = new ArrayList<>();
        for (Player participant : context.getParticipants()) {
            String teamId = gameManager.getTeamName(participant.getUniqueId());
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
            Component roundDisplay = Component.empty()
                    .append(Component.text("Round "))
                    .append(Component.text(context.getRoundManager().getPlayedRounds() + 1))
                    .append(Component.text(":"));
            if (participantTeams.contains(teamId)) {
                String oppositeTeamId = currentRound.getOppositeTeam(teamId);
                announceMatchToParticipant(participant, teamId, teamDisplayName, roundDisplay, oppositeTeamId);
                roundParticipants.add(participant);
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
                onDeckParticipants.add(participant);
            }
        }
        setUpTopbarForRound(roundMatchPairings);
        currentRound.start(roundParticipants, onDeckParticipants);
        String round = String.format("Round %d/%d", context.getRoundManager().getPlayedRounds() + 1, context.getRoundManager().getMaxRounds());
        sidebar.updateLine("round", round);
        adminSidebar.updateLine("round", round);
    }
    
    /**
     * Send a message to the participant who they are fighting against in the current match
     * @param participant the participant to send the message to
     * @param teamId the team that the participant is on
     */
    private void announceMatchToParticipant(Player participant, String teamId, Component teamDisplayName, Component roundDisplay, String oppositeTeamId) {
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
    }
    
    private void setUpTopbarForRound(List<MatchPairing> roundMatchPairings) {
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
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        
    }
    
    @Override
    public void resetParticipant(Player participant) {
        
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
