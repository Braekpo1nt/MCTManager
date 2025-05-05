package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.event.Tip;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay.ToFinalGameDelayState;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class WaitingInHubState extends EventState {
    
    protected final Timer waitingInHubTimer;
    
    protected final Map<UUID, Component> playerTips = new HashMap<>();
    protected int updateTipsTaskId;
    protected int displayTipsTaskId;
    
    public WaitingInHubState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        Component message = Component.text("Score multiplier: ")
                .append(Component.text(eventData.getPointMultiplier()))
                .color(NamedTextColor.GOLD);
        context.messageOnlineParticipants(message);
        context.messageAdmins(message);
        sidebar.updateLine("currentGame", getCurrentGameLine());
        waitingInHubTimer = startTimer();
        startActionBarTips();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        waitingInHubTimer.cancel();
        disableTips();
    }
    
    @Override
    public void onSwitchMode() {
        waitingInHubTimer.cancel();
        disableTips();
    }
    
    protected Timer startTimer() {
        Component prefix;
        if (eventData.allGamesHaveBeenPlayed()) {
            prefix = Component.text("Final round: ");
        } else {
            prefix = Component.text("Vote starts in: ");
        }
        return context.getTimerManager().start(Timer.builder()
                .duration(eventData.getConfig().getWaitingInHubDuration())
                .withSidebar(sidebar, "timer")
                .sidebarPrefix(prefix)
                .onCompletion(() -> {
                    disableTips();
                    if (eventData.allGamesHaveBeenPlayed()) {
                        context.setState(new ToFinalGameDelayState(context, contextReference, eventData));
                    } else {
                        context.setState(new VotingState(context, contextReference, eventData));
                    }
                })
                .build());
    }
    
    protected void disableTips() {
        plugin.getServer().getScheduler().cancelTask(updateTipsTaskId);
        plugin.getServer().getScheduler().cancelTask(displayTipsTaskId);
        Audience.audience(onlineParticipants.values()).sendActionBar(Component.empty());
    }
    
    @Override
    public CommandResult startGame(Set<String> teamIds, @NotNull GameType gameType, @NotNull String configFile) {
        waitingInHubTimer.cancel();
        disableTips();
        context.setState(new StartingGameDelayState(context, contextReference, eventData, gameType, configFile));
        return CommandResult.success(Component.empty()
                .append(Component.text("Manually starting "))
                .append(Component.text(gameType.getTitle()))
                .append(Component.text(". Cancelling the vote.")));
    }
    
    public void startActionBarTips() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        
        // Task to compute and update tips
        updateTipsTaskId = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            Map<String, List<Participant>> teamMapping = getTeamPlayerMapping();
            playerTips.clear();
            
            for (List<Participant> teamPlayers : teamMapping.values()) {
                List<Tip> tips = Tip.selectMultipleWeightedRandomTips(eventData.getConfig().getTips(), teamPlayers.size());
                
                for (int i = 0; i < teamPlayers.size(); i++) {
                    Participant participant = teamPlayers.get(i);
                    Component tip = tips.get(i).getBody();
                    playerTips.put(participant.getUniqueId(), tip);
                }
            }
        }, 0L, eventData.getConfig().getTipsDisplayTime());
        
        // Task to display the tips
        displayTipsTaskId = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (MCTParticipant participant : onlineParticipants.values()) {
                Component text = playerTips.getOrDefault(participant.getUniqueId(), Component.empty());
                participant.sendActionBar(text);
            }
        }, 0L, 20L);
        
    }
    
    @Override
    public void onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
        super.onParticipantJoin(event, participant);
        participant.teleport(config.getSpawn());
    }
    
    /**
     * @return mapping from online team's ids to their online members
     */
    public Map<String, List<Participant>> getTeamPlayerMapping() {
        return onlineParticipants.values().stream()
                .collect(Collectors.groupingBy(
                        OfflineParticipant::getTeamId,
                        Collectors.toList()
                ));
    }
}
