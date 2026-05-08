package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ActiveState extends FootRaceStateBase {
    
    private final FootRaceConfig config;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final TimerManager timerManager;
    private @Nullable Timer endRaceTimer;
    private int timerRefreshTaskId;
    private int standingsDisplayTaskId;
    
    public ActiveState(@NotNull FootRaceGame context) {
        super(context);
        this.config = context.getConfig();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        this.timerManager = context.getTimerManager();
    }
    
    @Override
    public void enter() {
        context.openGlassBarrier();
        context.setRaceStartTime(System.currentTimeMillis());
        startTimerRefreshTask();
        startStandingsUpdateTask();
    }
    
    @Override
    public void exit() {
        context.getPlugin().getServer().getScheduler().cancelTask(timerRefreshTaskId);
        context.getPlugin().getServer().getScheduler().cancelTask(standingsDisplayTaskId);
    }
    
    @Override
    public void cleanup() {
        context.getPlugin().getServer().getScheduler().cancelTask(timerRefreshTaskId);
        context.getPlugin().getServer().getScheduler().cancelTask(standingsDisplayTaskId);
    }
    
    private void startTimerRefreshTask() {
        timerRefreshTaskId = new BukkitRunnable() {
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
        }.runTaskTimer(context.getPlugin(), 0, 1).getTaskId();
    }
    
    @Override
    public void onParticipantRejoin(FootRaceParticipant participant, FootRaceTeam team) {
        super.onParticipantRejoin(participant, team);
        if (participant.isFinished()) {
            showRaceCompleteFastBoard(participant);
        }
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
    
    private void startStandingsUpdateTask() {
        standingsDisplayTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                context.updateStandings();
                context.displayStandings();
            }
        }.runTaskTimer(context.getPlugin(), 0L, 1L).getTaskId();
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FootRaceParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        
        Vector to = event.getTo().toVector();
        int currentCheckpointIndex = participant.getCurrentCheckpoint();
        int nextCheckpointIndex = MathUtils.wrapIndex(currentCheckpointIndex + 1, config.getCheckpoints().size());
        List<BoundingBox> checkpoints = config.getCheckpoints();
        
        // 1. Check for reaching the next checkpoint
        if (checkpoints.get(nextCheckpointIndex).contains(to)) {
            resetWrongWayLogic(participant);
            onParticipantReachCheckpoint(participant, nextCheckpointIndex);
            return;
        }
        
        // 2. Check for crossing the previous checkpoint (current is physical previous)
//        int previousCheckpoint = participant.getCurrentCheckpoint();
        int previousCheckpoint = MathUtils.wrapIndex(
                participant.getCurrentCheckpoint() - 1,
                checkpoints.size()
        );
        if (checkpoints.get(previousCheckpoint).contains(to)) {
            // they've reached the previous checkpoint
            participant.setCurrentCheckpoint(previousCheckpoint);
            participant.setShowingWrongWayAlert(true);
            showWrongWayTitle(participant);
            return;
        }
        
        // 3. Distance-based wrong way detection
        handleWrongWayDistanceLogic(participant, to);
    }
    
    private void handleWrongWayDistanceLogic(FootRaceParticipant participant, Vector to) {
        long now = System.currentTimeMillis();
        List<BoundingBox> checkpoints = config.getCheckpoints();
        int currentCheckpointIndex = participant.getCurrentCheckpoint();
        int nextCheckpointIndex = MathUtils.wrapIndex(currentCheckpointIndex + 1, checkpoints.size());
        
        double distToNext = MathUtils.getMinimumDistance(checkpoints.get(nextCheckpointIndex), to);
        
        /*
        Prioritize moving toward the correct checkpoint, otherwise "Wrong Way" shows when
        heading in the correct direction. If you're not moving closer to the next checkpoint, 
        then you're going the wrong way. There's a buffer of x milliseconds to reduce flickering,
        and then a message is displayed. 
         */
        if (distToNext < participant.getLastDistToNext() - 0.01) {
            // If moving closer to next checkpoint
            if (participant.getRightWayCounterStart() == -1) {
                participant.setRightWayCounterStart(now);
            }
            if (participant.isShowingWrongWayAlert() && now - participant.getRightWayCounterStart() > 1000) {
                participant.setShowingWrongWayAlert(false);
                resetWrongWayLogic(participant);
            }
        } else {
            participant.setRightWayCounterStart(-1);
            if (participant.getWrongWayCounterStart() == -1) {
                participant.setWrongWayCounterStart(now);
            }
            if (!participant.isShowingWrongWayAlert() && now - participant.getWrongWayCounterStart() > 2000) {
                participant.setShowingWrongWayAlert(true);
            }
        }
        
        participant.setLastDistToNext(distToNext);
        
        if (participant.isShowingWrongWayAlert()) {
            showWrongWayTitle(participant);
        }
    }
    
    private void resetWrongWayLogic(FootRaceParticipant participant) {
        participant.showTitle(UIUtils.EMPTY_TITLE);
        participant.setWrongWayCounterStart(-1);
        participant.setRightWayCounterStart(-1);
        participant.setShowingWrongWayAlert(false);
        participant.setLastDistToNext(Double.MAX_VALUE);
    }
    
    private void showWrongWayTitle(FootRaceParticipant participant) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - participant.getLastWrongWayTitleTime() < 2000) {
            return;
        }
        participant.setLastWrongWayTitleTime(currentTime);
        participant.showTitle(UIUtils.defaultTitle(
                Component.text("Wrong Way!")
                        .color(NamedTextColor.RED)
        ));
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
                            .append(Component.text(currentLap + 1))
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
            context.awardPoints(participant, config.getCompleteLapScore(), String.format("Finished lap %s", currentLap));
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
        participant.setPlacement(placement);
        showRaceCompleteFastBoard(participant);
        int points = calculatePointsForPlacement(placement);
        context.awardPoints(participant, points, String.format("Finished race in %s place", placement));
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
                    .append(participant.displayName())
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
        context.messageAllParticipants(participant.displayName()
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
     * Calculates the points to be awarded for the given placement. This is based on user-configured values. Returns a
     * set number of values for placement less than or equal to x, and a detriment of 10 points for each successive
     * placement greater than x
     * @param placement the placement number (1=1st place, 2=2nd place, 300=300th place) to get the points for. Must be
     * 1 or more.
     * @return The number of points to award for the placement, no less than 0.
     */
    private int calculatePointsForPlacement(int placement) {
        if (placement < 1) {
            throw new IllegalArgumentException("placement can't be less than 1");
        }
        int[] placementPoints = config.getPlacementPoints();
        if (placement <= placementPoints.length) {
            return placementPoints[placement - 1];
        }
        int minPlacementPoints = placementPoints[placementPoints.length - 1];
        int points = minPlacementPoints - ((placement - placementPoints.length) * config.getDetriment());
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
