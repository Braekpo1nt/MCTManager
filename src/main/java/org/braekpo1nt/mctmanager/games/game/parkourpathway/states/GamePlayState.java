package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Shared functionality for the states where participants are
 * actively performing parkour
 */
abstract class GamePlayState extends ParkourPathwayStateBase {
    protected final ParkourPathwayConfig config;
    protected final int skipCooldownTaskId;
    
    public GamePlayState(@NotNull ParkourPathwayGame context) {
        super(context);
        this.config = context.getConfig();
        skipCooldownTaskId = context.getPlugin().getServer().getScheduler()
                .runTaskTimer(context.getPlugin(), () ->
                        context.getParticipants().values().forEach(participant -> {
                                    if (participant.getSkipCooldown() > 0) {
                                        participant.setSkipCooldown(participant.getSkipCooldown() - 1);
                                    }
                                }
                        ), 0L, 20L).getTaskId();
    }
    
    @Override
    public void onParticipantRejoin(ParkourParticipant participant, ParkourTeam team) {
        context.giveSkipItem(participant, participant.getUnusedSkips());
        Location respawn = context.getConfig()
                .getPuzzle(participant.getCurrentPuzzle())
                .getCheckPoints().get(participant.getCurrentPuzzleCheckpoint())
                .getRespawn();
        participant.teleport(respawn);
        context.giveBoots(participant);
        context.updateCheckpointSidebar(participant);
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        super.onNewParticipantJoin(participant, team);
        context.giveSkipItem(participant, config.getNumOfSkips());
        participant.setUnusedSkips(config.getNumOfSkips());
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ParkourParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        int currentPuzzleIndex = participant.getCurrentPuzzle();
        int nextPuzzleIndex = currentPuzzleIndex + 1;
        if (nextPuzzleIndex >= config.getPuzzlesSize()) {
            // should not occur because of above check
            return;
        }
        Puzzle currentPuzzle = config.getPuzzle(currentPuzzleIndex);
        if (!currentPuzzle.isInBounds(participant.getLocation().toVector())) {
            onParticipantOutOfBounds(participant, currentPuzzle);
            return;
        }
        Puzzle nextPuzzle = config.getPuzzle(nextPuzzleIndex);
        int nextPuzzleCheckPointIndex = participantReachedCheckPoint(participant.getLocation().toVector(), nextPuzzle);
        if (nextPuzzleCheckPointIndex >= 0) {
            onParticipantReachCheckpoint(participant, nextPuzzleIndex, nextPuzzleCheckPointIndex);
            return;
        }
        int parallelCheckPointIndex = participantReachedCheckPoint(participant.getLocation().toVector(), currentPuzzle);
        if (parallelCheckPointIndex >= 0) {
            int currentCheckpoint = participant.getCurrentPuzzleCheckpoint();
            if (parallelCheckPointIndex == currentCheckpoint) {
                return;
            }
            participant.setCurrentPuzzleCheckpoint(parallelCheckPointIndex);
        }
    }
    
    private void onParticipantOutOfBounds(ParkourParticipant participant, Puzzle currentPuzzle) {
        CheckPoint currentCheckPoint = currentPuzzle.getCheckPoints().get(participant.getCurrentPuzzleCheckpoint());
        Location respawn = currentCheckPoint.getRespawn().setDirection(participant.getLocation().getDirection());
        participant.teleport(respawn);
    }
    
    /**
     * Check if the given location is inside the given puzzle's check points.
     * @param v the location to check if it's inside the puzzle's detection areas or not.
     * @param puzzle the puzzle to check if the player reached
     * @return -1 if v isn't inside the given puzzle's detection areas. Otherwise, returns the index of the puzzle's
     * CheckPoint that v is inside.
     */
    private int participantReachedCheckPoint(Vector v, Puzzle puzzle) {
        for (int i = 0; i < puzzle.getCheckPoints().size(); i++) {
            CheckPoint nextCheckPoint = puzzle.getCheckPoints().get(i);
            if (nextCheckPoint.getDetectionArea().contains(v)) {
                return i;
            }
        }
        return -1;
    }
    
    private void onParticipantReachCheckpoint(ParkourParticipant participant, int puzzleIndex, int puzzleCheckPointIndex) {
        participant.setCurrentPuzzle(puzzleIndex);
        participant.setCurrentPuzzleCheckpoint(puzzleCheckPointIndex);
        context.updateCheckpointSidebar(participant);
        if (puzzleIndex >= config.getPuzzlesSize() - 1) {
            onParticipantFinish(participant, true);
        } else {
            Component checkpointNum = Component.empty()
                    .append(Component.text(puzzleIndex))
                    .append(Component.text("/"))
                    .append(Component.text(config.getPuzzlesSize() - 1));
            context.messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" reached checkpoint "))
                    .append(checkpointNum));
            participant.showTitle(UIUtils.defaultTitle(
                    Component.empty(),
                    Component.empty()
                            .append(Component.text("Checkpoint "))
                            .append(checkpointNum)
                            .color(NamedTextColor.YELLOW)
            ));
            context.awardPoints(participant, calculatePointsForPuzzle(puzzleIndex, config.getCheckpointScore()));
            
            if (config.getMaxSkipPuzzle() > 0) {
                if (puzzleIndex == config.getMaxSkipPuzzle()) {
                    participant.sendMessage(Component.empty()
                            .append(Component.text("Skips are not allowed after checkpoint "))
                            .append(Component.text(config.getMaxSkipPuzzle())));
                    context.awardPointsForUnusedSkips(participant);
                }
            }
        }
        if (allParticipantsHaveFinished()) {
            for (ParkourParticipant p : context.getParticipants().values()) {
                context.awardPointsForUnusedSkips(p);
                p.setGameMode(GameMode.SPECTATOR);
            }
            stop();
            return;
        }
        restartMercyRuleCountdown();
    }
    
    protected abstract void stop();
    
    protected abstract void restartMercyRuleCountdown();
    
    /**
     * Calculates the points for playersPuzzle based on how many players have reached or passed that playersPuzzle. If
     * puzzleScores has x elements, the nth player to arrive at playersPuzzle gets the puzzleScores[n-1], unless n is
     * greater than or equal to x, in which case they get puzzleScores[x-1]
     * @param playersPuzzle the index of the puzzle to get the points for
     * @param puzzleScores the scores to progress through. The last score is to give to everyone who didn't make the one
     * of the other specified scores.
     * @return the points for playersPuzzle
     */
    private int calculatePointsForPuzzle(int playersPuzzle, int[] puzzleScores) {
        int numWhoReachedOrPassedCheckpoint = 0;
        for (ParkourParticipant participant : context.getParticipants().values()) {
            int puzzleIndex = participant.getCurrentPuzzle();
            if (puzzleIndex >= playersPuzzle) {
                numWhoReachedOrPassedCheckpoint++;
            }
        }
        if (numWhoReachedOrPassedCheckpoint < puzzleScores.length) {
            return puzzleScores[numWhoReachedOrPassedCheckpoint - 1];
        } else {
            return puzzleScores[puzzleScores.length - 1];
        }
    }
    
    private boolean allParticipantsHaveFinished() {
        for (ParkourParticipant participant : context.getParticipants().values()) {
            int currentPuzzleIndex = participant.getCurrentPuzzle();
            if (currentPuzzleIndex < config.getPuzzlesSize() - 1) {
                //at least one player is still playing
                return false;
            }
        }
        //all players are at finish line
        return true;
    }
    
    private void onParticipantFinish(ParkourParticipant participant, boolean awardPoints) {
        participant.showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("You finished!"))
                        .color(NamedTextColor.GREEN),
                Component.empty()
                        .append(Component.text("Well done"))
                        .color(NamedTextColor.GREEN)
        ));
        context.messageAllParticipants(Component.empty()
                .append(Component.text(participant.getName()))
                .append(Component.text(" finished!"))
                .color(NamedTextColor.GREEN)
        );
        if (awardPoints) {
            context.awardPoints(participant, calculatePointsForWin(config.getWinScore()));
        }
        context.awardPointsForUnusedSkips(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        participant.setFinished(true);
    }
    
    /**
     * Calculates the number of points for a win, based on how many players have currently won. If winScores has x
     * elements, the nth player to win will get winScores[n-1] points, unless n is greater than or equal to x in which
     * case they get winScores[x-1]
     * @param winScores the scores to progress through. The last score is to give to everyone who didn't make one of the
     * other specified scores.
     * @return the points for the most recent player win
     */
    private int calculatePointsForWin(int[] winScores) {
        int numberOfWins = 0;
        for (ParkourParticipant participant : context.getParticipants().values()) {
            int puzzleIndex = participant.getCurrentPuzzle();
            if (puzzleIndex >= config.getPuzzlesSize() - 1) {
                numberOfWins++;
            }
        }
        if (numberOfWins < winScores.length) {
            return winScores[numberOfWins - 1];
        } else {
            return winScores[winScores.length - 1];
        }
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ParkourParticipant participant) {
        if (participant.getSkipCooldown() > 0) {
            participant.sendActionBar(Component.empty()
                    .append(Component.text("Skip cooldown for "))
                    .append(Component.text(participant.getSkipCooldown()))
                    .append(Component.text("s")));
            return;
        }
        if (participant.getUnusedSkips() <= 0) {
            return;
        }
        if (event.useItemInHand().equals(Event.Result.DENY)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getItemMeta().equals(config.getSkipItem().getItemMeta())) {
            performCheckpointSkip(participant);
        }
    }
    
    private void performCheckpointSkip(ParkourParticipant participant) {
        if (participant.isFinished()) {
            return;
        }
        int currentPuzzleIndex = participant.getCurrentPuzzle();
        int nextPuzzleIndex = currentPuzzleIndex + 1;
        if (nextPuzzleIndex >= config.getPuzzlesSize()) {
            // should not occur because of above check
            return;
        }
        participant.setSkipCooldown(config.getSkipCooldownDuration());
        participant.getInventory().removeItemAnySlot(config.getSkipItem());
        participant.setUnusedSkips(participant.getUnusedSkips() - 1);
        onParticipantSkippedToCheckpoint(participant, nextPuzzleIndex);
    }
    
    private void onParticipantSkippedToCheckpoint(ParkourParticipant participant, int puzzleIndex) {
        participant.setCurrentPuzzle(puzzleIndex);
        participant.setCurrentPuzzleCheckpoint(0);
        context.updateCheckpointSidebar(participant);
        Puzzle newPuzzle = config.getPuzzle(puzzleIndex);
        participant.teleport(newPuzzle.getCheckPoints().getFirst().getRespawn());
        if (puzzleIndex >= config.getPuzzlesSize() - 1) {
            onParticipantFinish(participant, false);
        } else {
            Component checkpointNum = Component.empty()
                    .append(Component.text(puzzleIndex))
                    .append(Component.text("/"))
                    .append(Component.text(config.getPuzzlesSize() - 1));
            context.messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" skipped to checkpoint "))
                    .append(checkpointNum));
            participant.showTitle(UIUtils.defaultTitle(
                    Component.empty(),
                    Component.empty()
                            .append(Component.text("Checkpoint "))
                            .append(checkpointNum)
                            .color(NamedTextColor.YELLOW)
            ));
            
            if (config.getMaxSkipPuzzle() > 0) {
                if (puzzleIndex == config.getMaxSkipPuzzle()) {
                    participant.sendMessage(Component.empty()
                            .append(Component.text("Skips are not allowed after checkpoint "))
                            .append(Component.text(config.getMaxSkipPuzzle())));
                    context.awardPointsForUnusedSkips(participant);
                }
            }
        }
        if (allParticipantsHaveFinished()) {
            for (ParkourParticipant p : context.getParticipants().values()) {
                context.awardPointsForUnusedSkips(p);
                p.setGameMode(GameMode.SPECTATOR);
            }
            stop();
            return;
        }
        restartMercyRuleCountdown();
    }
}
