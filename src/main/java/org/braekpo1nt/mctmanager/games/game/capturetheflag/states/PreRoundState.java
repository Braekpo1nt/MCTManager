package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.RoundManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PreRoundState extends CaptureTheFlagStateBase {
    
    private final BattleTopbar topbar;
    private final RoundManager roundManager;
    private @Nullable Timer timer;
    
    public PreRoundState(CaptureTheFlagGame context) {
        super(context);
        this.topbar = context.getTopbar();
        this.roundManager = context.getRoundManager();
    }
    
    @Override
    public void enter() {
        context.updateRoundLine();
        for (Participant participant : context.getParticipants().values()) {
            announceMatchToParticipant(participant);
        }
        setUpTopbarForRound();
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getMatchesStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> {
                    context.setState(new RoundActiveState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "PreRoundState.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
    
    private void announceMatchToParticipant(Participant participant) {
        List<MatchPairing> currentRound = roundManager.getCurrentRound();
        TeamData<CTFParticipant> team = context.getTeams().get(participant.getTeamId());
        String oppositeTeamId = RoundManager.getOppositeTeam(team.getTeamId(), currentRound);
        Component roundDisplay = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text(":"));
        if (oppositeTeamId != null) {
            TeamData<CTFParticipant> oppositeTeam = context.getTeamOrQuitTeam(oppositeTeamId);
            participant.sendMessage(Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is competing against "))
                    .append(oppositeTeam.getFormattedDisplayName())
                    .append(Component.text(" this round."))
                    .color(NamedTextColor.YELLOW));
            participant.showTitle(UIUtils.defaultTitle(
                    roundDisplay,
                    Component.empty()
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" vs "))
                            .append(oppositeTeam.getFormattedDisplayName())
            ));
        } else {
            participant.sendMessage(Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is on-deck this round."))
                    .color(NamedTextColor.YELLOW));
            participant.showTitle(UIUtils.defaultTitle(
                    roundDisplay,
                    Component.empty()
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" is on-deck"))));
        }
    }
    
    private void setUpTopbarForRound() {
        List<MatchPairing> roundMatchPairings = roundManager.getCurrentRound();
        topbar.removeAllTeamPairs();
        for (MatchPairing mp : roundMatchPairings) {
            CTFTeam northTeam = context.getTeamOrQuitTeam(mp.northTeam());
            CTFTeam southTeam = context.getTeamOrQuitTeam(mp.southTeam());
            topbar.addTeam(mp.northTeam(), northTeam.getColor());
            topbar.addTeam(mp.southTeam(), southTeam.getColor());
            topbar.linkTeamPair(mp.northTeam(), mp.southTeam());
            int northAlive = 0;
            int southAlive = 0;
            for (CTFParticipant participant : context.getParticipants().values()) {
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
}
