package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class DescriptionState extends FarmRushStateBase {
    
    public DescriptionState(@NotNull FarmRushGame context) {
        super(context);
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        if (context.getConfig().shouldEnforceMaxScore()) {
            context.messageAllParticipants(Component.empty()
                    .append(Component.text("The first team to reach "))
                    .append(Component.text((int) (context.getConfig().getMaxScore() * context.getGameManager().getMultiplier()))
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" points wins!"))
            );
        }
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void onParticipantRejoin(FarmRushParticipant participant, FarmRushTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onNewParticipantJoin(FarmRushParticipant participant, FarmRushTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FarmRushParticipant participant) {
        super.onParticipantMove(event, participant);
        Arena arena = context.getTeams().get(participant.getTeamId()).getArena();
        if (!arena.getBarn().contains(event.getFrom().toVector())) {
            participant.teleport(arena.getSpawn());
            return;
        }
        if (!arena.getBarn().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
}
