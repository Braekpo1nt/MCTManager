package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ActiveState implements FootRaceState {
    
    private final @NotNull FootRaceGame context;
    private final FootRaceConfig config;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final TimerManager timerManager;
    private @Nullable Timer endRaceTimer;
    
    public ActiveState(@NotNull FootRaceGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.config = context.getConfig();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        this.timerManager = context.getTimerManager();
        startRace();
    }
    
    private void startRace() {
        context.openGlassBarrier();
        context.setRaceStartTime(System.currentTimeMillis());
        startTimerRefreshTask();
        startStandingsUpdateTask();
    }
    
    private void startTimerRefreshTask() {
        context.setTimerRefreshTaskId(new BukkitRunnable(){
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
                Component timeComponent = TimeStringUtils.getTimeComponentMillis(elapsedTime);
                for (FootRaceParticipant participant : context.getParticipants().values()) {
                    if (!participant.isFinished()) {
                        sidebar.updateLine(
                                participant.getUniqueId(),
                                "elapsedTime",
                                timeComponent
                        );
                    }
                }
                for (Player admin : context.getAdmins()) {
                    adminSidebar.updateLine(
                            admin.getUniqueId(),
                            "elapsedTime",
                            timeComponent
                    );
                }
            }
        }.runTaskTimer(context.getPlugin(), 0, 1).getTaskId());
    }
    
    @Override
    public void onParticipantJoin(Participant newParticipant) {
        QuitParticipant quitData = context.getQuitParticipants().get(newParticipant.getUniqueId());
        if (quitData != null) {
            FootRaceParticipant rejoinedParticipant = new FootRaceParticipant(newParticipant, quitData);
            rejoinParticipant(rejoinedParticipant);
        } else {
            initializeParticipant(newParticipant);
        }
        FootRaceParticipant participant = context.getParticipants().get(newParticipant.getUniqueId());
        sidebar.updateLine(participant.getUniqueId(), "title", context.getTitle());
        
        if (participant.getLap() > context.getConfig().getLaps()) {
            showRaceCompleteFastBoard(participant);
        } else {
            sidebar.updateLine(participant.getUniqueId(), "lap", 
                    Component.empty()
                        .append(Component.text("Lap: "))
                        .append(Component.text(participant.getLap()))
                        .append(Component.text("/"))
                        .append(Component.text(config.getLaps())));
        }
        context.updateStandings();
        context.displayStandings();
    }
    
    private void rejoinParticipant(FootRaceParticipant participant) {
        context.getParticipants().put(participant.getUniqueId(), participant);
        sidebar.addPlayer(participant);
        context.getStandings().add(participant);
        if (participant.isFinished()) {
            showRaceCompleteFastBoard(participant);
        }
        context.giveBoots(participant);
    }
    
    private void showRaceCompleteFastBoard(FootRaceParticipant participant) {
        long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("elapsedTime", TimeStringUtils.getTimeComponentMillis(elapsedTime)),
                new KeyLine("lap", Component.empty()
                        .append(Component.text("Finished "))
                        .append(GameManagerUtils.getPlacementTitle(
                                participant.getPlacement()))
                        .append(Component.text("!")))
        );
        context.updateStandings();
        context.displayStandings();
    }
    
    @Override
    public void onParticipantQuit(FootRaceParticipant participant) {
        resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.getStandings().remove(participant);
        context.updateStandings();
        context.displayStandings();
    }
    
    @Override
    public void initializeParticipant(Participant participant) {
        context.initializeParticipant(participant);
    }
    
    @Override
    public void resetParticipant(FootRaceParticipant participant) {
        context.resetParticipant(participant);
    }
    
    private void startStandingsUpdateTask() {
        context.setStandingsDisplayTaskId(new BukkitRunnable() {
            @Override
            public void run() {
                context.updateStandings();
                context.displayStandings();
            }
        }.runTaskTimer(context.getPlugin(), 0L, 1L).getTaskId());
    }
    
    @Override
    public void onParticipantMove(FootRaceParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        int currentCheckpointIndex = participant.getCurrentCheckpoint();
        int nextCheckpointIndex = MathUtils.wrapIndex(currentCheckpointIndex + 1, config.getCheckpoints().size());
        BoundingBox nextCheckpoint = config.getCheckpoints().get(nextCheckpointIndex);
        if (nextCheckpoint.contains(participant.getLocation().toVector())) {
            onParticipantReachCheckpoint(participant, nextCheckpointIndex);
        }
    }
    
    private void onParticipantReachCheckpoint(FootRaceParticipant participant, int reachedCheckpointIndex) {
        participant.setCurrentCheckpoint(reachedCheckpointIndex);
        if (reachedCheckpointIndex == config.getCheckpoints().size() - 1) {
            onParticipantCrossFinishLine(participant);
        }
    }
    
    private void onParticipantCrossFinishLine(FootRaceParticipant participant) {
        UUID uuid = participant.getUniqueId();
        int currentLap = participant.getLap();
        int newLap = currentLap + 1;
        participant.setLap(newLap);
        if (currentLap < context.getConfig().getLaps()) {
            sidebar.updateLine(
                    uuid,
                    "lap",
                    Component.empty()
                            .append(Component.text("Lap: "))
                            .append(Component.text(newLap))
                            .append(Component.text("/"))
                            .append(Component.text(config.getLaps()))
            );
            participant.showTitle(UIUtils.defaultTitle(
                    Component.empty(),
                    Component.empty()
                            .append(Component.text("Lap "))
                            .append(Component.text(currentLap+1))
                            .color(NamedTextColor.YELLOW)
            ));
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - context.getRaceStartTime();
            context.messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" finished lap "))
                    .append(Component.text(currentLap))
                    .append(Component.text(" in "))
                    .append(TimeStringUtils.getTimeComponentMillis(elapsedTime)));
            gameManager.awardPointsToParticipant(participant, config.getCompleteLapScore());
            return;
        }
        if (currentLap == context.getConfig().getLaps()) {
            onPlayerFinishedRace(participant);
        }
    }
    
    /**
     * Code to run when a single participant crosses the finish line for the last time
     * @param participant The participant who crossed the finish line
     */
    private void onPlayerFinishedRace(FootRaceParticipant participant) {
        long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
        participant.setFinished(true);
        int placement = context.getNumOfFinishedParticipants() + 1;
        context.setNumOfFinishedParticipants(placement);
        showRaceCompleteFastBoard(participant);
        int points = calculatePointsForPlacement(placement);
        gameManager.awardPointsToParticipant(participant, points);
        Component timeComponent = TimeStringUtils.getTimeComponentMillis(elapsedTime);
        Component endCountDown = TimeStringUtils.getTimeComponent(config.getRaceEndCountdownDuration());
        Component placementComponent = GameManagerUtils.getPlacementTitle(placement);
        participant.showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("Finished "))
                        .append(placementComponent)
                        .color(NamedTextColor.GREEN),
                Component.empty()
                        .append(Component.text("Well done"))
                        .color(NamedTextColor.GREEN)
        ));
        if (context.getNumOfFinishedParticipants() == 1) {
            context.messageAllParticipants(Component.empty()
                    .append(Component.text(participant.getName()))
                    .append(Component.text(" finished 1st in "))
                    .append(timeComponent)
                    .append(Component.text("! "))
                    .append(Component.text("Only ")
                            .append(endCountDown)
                            .append(Component.text(" remain!"))
                            .color(NamedTextColor.RED))
                    .color(NamedTextColor.GREEN));
            Audience.audience(context.getParticipants().values().stream()
                            .filter(p -> !p.equals(participant)).toList())
                    .showTitle(UIUtils.defaultTitle(
                            Component.empty(),
                            Component.empty()
                                    .append(endCountDown)
                                    .append(Component.text(" left"))
                                    .color(NamedTextColor.RED)
                    ));
            startEndRaceCountDown();
            return;
        }
        context.messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" finished "))
                .append(placementComponent)
                .append(Component.text(" in "))
                .append(timeComponent));
        if (allParticipantsHaveFinished()) {
            if (endRaceTimer != null) {
                endRaceTimer.cancel();
            }
            context.setState(new GameOverState(context));
        }
    }
    
    private boolean allParticipantsHaveFinished() {
        for (FootRaceParticipant participant : context.getParticipants().values()) {
            if (!participant.isFinished()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculates the points to be awarded for the given placement. This is based on user-configured values. Returns a set number of values for placement less than or equal to x, and a detriment of 10 points for each successive placement greater than x
     * @param placement the placement number (1=1st place, 2=2nd place, 300=300th place) to get the points for. Must be 1 or more.
     * @return The number of points to award for the placement, no less than 0.
     */
    private int calculatePointsForPlacement(int placement) {
        if (placement < 1) {
            throw new IllegalArgumentException("placement can't be less than 1");
        }
        int[] placementPoints = config.getPlacementPoints();
        if (placement <= placementPoints.length) {
            return placementPoints[placement-1];
        }
        int minPlacementPoints = placementPoints[placementPoints.length-1];
        int points = minPlacementPoints - ((placement-placementPoints.length) * config.getDetriment());
        return Math.max(points, 0);
    }
    
    private void startEndRaceCountDown() {
        endRaceTimer = timerManager.start(Timer.builder()
                .duration(config.getRaceEndCountdownDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Ending: "))
                .onCompletion(() -> context.setState(new GameOverState(context)))
                .build());
    }
}
