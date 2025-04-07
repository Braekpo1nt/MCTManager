package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class ClockChimeState extends ClockworkStateBase {
    
    private final @NotNull ClockworkConfig config;
    private final int clockChimeTaskId;
    
    public ClockChimeState(@NotNull ClockworkGame context) {
        super(context);
        this.config = context.getConfig();
        context.getSidebar().updateLine("timer", "Chiming...");
        context.getAdminSidebar().updateLine("timer", "Chiming...");
        context.setNumberOfChimes(context.getRandom().nextInt(1, 13));
        turnOffCollisions();
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            if (participant.isAlive()) {
                participant.teleport(config.getStartingLocation());
                participant.setArrowsInBody(0);
            }
        }
        clockChimeTaskId = new BukkitRunnable() {
            int count = context.getNumberOfChimes();
            @Override
            public void run() {
                if (count <= 0) {
                    this.cancel();
                    context.setState(new GetToWedgeState(context));
                    return;
                }
                playChimeSound();
                count--;
            }
        }.runTaskTimer(context.getPlugin(), 0L, (long) context.getChimeInterval()).getTaskId();
    }
    
    @Override
    public void cleanup() {
        context.getPlugin().getServer().getScheduler().cancelTask(clockChimeTaskId);
    }
    
    private void turnOffCollisions() {
        for (ClockworkTeam team : context.getTeams().values()) {
            context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    private void playChimeSound() {
        for (Participant participant : context.getParticipants().values()) {
            participant.playSound(participant.getLocation(), config.getClockChimeSound(), config.getClockChimeVolume(), config.getClockChimePitch());
        }
        context.getGameManager()
                .playSoundForAdmins(
                        config.getClockChimeSound(), 
                        config.getClockChimeVolume(), 
                        config.getClockChimePitch());
    }
    
    @Override
    public void onTeamRejoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
    
    @Override
    public void onNewTeamJoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
}
