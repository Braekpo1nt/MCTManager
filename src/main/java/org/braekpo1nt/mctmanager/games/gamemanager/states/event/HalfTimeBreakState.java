package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class HalfTimeBreakState extends WaitingInHubState {
    public HalfTimeBreakState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
    }
    
    @Override
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        return CommandResult.failure("Can't start a game during the half-time break");
    }
    
    @Override
    protected Timer getTimer() {
        return context.getTimerManager().start(Timer.builder()
                .duration(eventData.getConfig().getHalftimeBreakDuration())
                .withSidebar(sidebar, "timer")
                .titleThreshold(20)
                .titleAudience(Audience.audience(onlineParticipants.values()))
                .sidebarPrefix(Component.text("Break: ").color(NamedTextColor.YELLOW))
                .onCompletion(() -> {
                    disableTips();
                    context.setState(new WaitingInHubState(context, contextReference, eventData));
                })
                .build());
    }
}
