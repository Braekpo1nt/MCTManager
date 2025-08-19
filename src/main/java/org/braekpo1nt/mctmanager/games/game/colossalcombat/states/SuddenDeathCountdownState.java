package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuddenDeathCountdownState extends GameplayState {
    
    private @Nullable Timer suddenDeathTimer;
    
    public SuddenDeathCountdownState(@NotNull ColossalCombatGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.messageAdmins(Component.empty()
                .append(Component.text(config.getCaptureTheFlagMaximumPlayers()))
                .append(Component.text(" players are alive on each team. Triggering sudden death countdown. Only admins can see this message."))
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC));
        suddenDeathTimer = context.getTimerManager().start(Timer.builder()
                .duration(config.getCaptureTheFlagDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Sudden Death: "))
                .onCompletion(() -> context.setState(new SuddenDeathState(context)))
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(suddenDeathTimer);
    }
    
    @Override
    protected void onParticipantDeath(@NotNull ColossalParticipant participant) {
        super.onParticipantDeath(participant);
        if (context.getTeams().get(participant.getTeamId()).isDead()) {
            switch (participant.getAffiliation()) {
                case NORTH -> onTeamWinRound(southTeam);
                case SOUTH -> onTeamWinRound(northTeam);
            }
        }
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        Timer.cancel(suddenDeathTimer);
    }
}
