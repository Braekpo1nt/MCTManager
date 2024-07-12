package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
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
    }
    
    private void startTimerRefreshTask() {
        context.setTimerRefreshTaskId(new BukkitRunnable(){
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
                Component timeComponent = TimeStringUtils.getTimeComponentMillis(elapsedTime);
                for (Player participant : context.getParticipants()) {
                    if (!completedRace(participant)) {
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
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            initializeParticipant(participant);
        }
        sidebar.updateLine(participant.getUniqueId(), "title", context.getTitle());
        
        Integer currentLap = context.getLaps().get(participant.getUniqueId());
        if (currentLap > FootRaceGame.MAX_LAPS) {
            showRaceCompleteFastBoard(participant.getUniqueId());
        } else {
            sidebar.updateLine(participant.getUniqueId(), "lap", String.format("Lap: %d/%d", currentLap, FootRaceGame.MAX_LAPS));
        }
    }
    
    /**
     * Checks if the participant was previously in the game, and should thus rejoin
     * @param participant The participant to check
     * @return True if the participant was in the game before, and should rejoin. False
     * if the participant wasn't in the game before. 
     */
    private boolean participantShouldRejoin(Player participant) {
        return completedRace(participant) 
                || context.getLaps().containsKey(participant.getUniqueId());
    }
    
    private void rejoinParticipant(Player participant) {
        context.getParticipants().add(participant);
        sidebar.addPlayer(participant);
        if (completedRace(participant)) {
            showRaceCompleteFastBoard(participant.getUniqueId());
        }
        context.giveBoots(participant);
    }
    
    /**
     * @param participant the participant
     * @return true if the given participant has already completed the race
     */
    private boolean completedRace(Player participant) {
        return context.getFinishedParticipants().contains(participant.getUniqueId());
    }
    
    private void showRaceCompleteFastBoard(UUID playerUUID) {
        long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
        sidebar.updateLines(playerUUID,
                new KeyLine("elapsedTime", TimeStringUtils.getTimeComponentMillis(elapsedTime)),
                new KeyLine("lap", Component.empty()
                        .append(Component.text("Finished "))
                        .append(GameManagerUtils.getPlacementTitle(
                                context.getFinishedParticipants().indexOf(playerUUID) + 1))
                        .append(Component.text("!")))
        );
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        context.getParticipants().remove(participant);
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        context.initializeParticipant(participant);
    }
    
    @Override
    public void resetParticipant(Player participant) {
        context.resetParticipant(participant);
    }
    
    private void startStandingsUpdateTask() {
        context.setStandingsDisplayTaskId(new BukkitRunnable() {
            @Override
            public void run() {
                
            }
        }.runTaskTimer(context.getPlugin(), 0L, 1L).getTaskId());
    }
    
    @Override
    public void onParticipantMove(Player participant) {
        UUID uuid = participant.getUniqueId();
        if (context.getFinishedParticipants().contains(uuid)) {
            return;
        }
        int currentCheckpointIndex = context.getCheckpointIndexes().get(uuid);
        int nextCheckpointIndex = currentCheckpointIndex + 1;
        if (nextCheckpointIndex >= config.getCheckpoints().size()) {
            // should not occur because of the above check
            return;
        }
        BoundingBox nextCheckpoint = config.getCheckpoints().get(nextCheckpointIndex);
        if (nextCheckpoint.contains(participant.getLocation().toVector())) {
            onParticipantReachCheckpoint(participant, nextCheckpointIndex);
        }
    }
    
    private void onParticipantReachCheckpoint(Player participant, int nextCheckpointIndex) {
        UUID uuid = participant.getUniqueId();
        context.getCheckpointIndexes().put(uuid, nextCheckpointIndex);
        if (nextCheckpointIndex >= context.getCheckpointIndexes().size() - 1) {
            onParticipantCrossFinishLine(participant);
        }
        context.updateStandings();
    }
    
    private void onParticipantCrossFinishLine(Player participant) {
        UUID uuid = participant.getUniqueId();
        int currentLap = context.getLaps().get(uuid);
        if (currentLap < FootRaceGame.MAX_LAPS) {
            int newLap = currentLap + 1;
            context.getLaps().put(uuid, newLap);
            sidebar.updateLine(
                    uuid,
                    "lap",
                    String.format("Lap: %d/%d", context.getLaps().get(uuid), FootRaceGame.MAX_LAPS)
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
        if (currentLap == FootRaceGame.MAX_LAPS) {
            context.getLaps().put(uuid, currentLap + 1);
            onPlayerFinishedRace(participant);
        }
    }
    
    /**
     * Code to run when a single participant crosses the finish line for the last time
     * @param participant The participant who crossed the finish line
     */
    private void onPlayerFinishedRace(Player participant) {
        long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
        context.getFinishedParticipants().add(participant.getUniqueId());
        showRaceCompleteFastBoard(participant.getUniqueId());
        int placement = context.getFinishedParticipants().indexOf(participant.getUniqueId()) + 1;
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
        if (context.getFinishedParticipants().size() == 1) {
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
            Audience.audience(context.getParticipants().stream()
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
        if (context.getFinishedParticipants().size() == context.getParticipants().size()) {
            if (endRaceTimer != null) {
                endRaceTimer.cancel();
            }
            context.setState(new GameOverState(context));
        }
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
                .onCompletion(() -> {
                    context.setState(new GameOverState(context));
                })
                .build());
    }
}
