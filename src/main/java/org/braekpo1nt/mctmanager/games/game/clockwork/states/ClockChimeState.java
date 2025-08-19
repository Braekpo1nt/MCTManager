package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ClockChimeState extends RoundActiveState {
    
    private final @NotNull ClockworkConfig config;
    private int clockChimeTaskId;
    
    public ClockChimeState(@NotNull ClockworkGame context) {
        super(context);
        this.config = context.getConfig();
    }
    
    @Override
    public void enter() {
        context.getSidebar().updateLine("timer", "Chiming...");
        context.getAdminSidebar().updateLine("timer", "Chiming...");
        context.setNumberOfChimes(context.getRandom().nextInt(1, 13));
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
    public void exit() {
        context.getPlugin().getServer().getScheduler().cancelTask(clockChimeTaskId);
    }
    
    @Override
    public void cleanup() {
        context.getPlugin().getServer().getScheduler().cancelTask(clockChimeTaskId);
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
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ClockworkParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        Location stayLoc = event.getTo();
        Vector position = context.getConfig().getStartingLocation().toVector();
        if (!stayLoc.toVector().equals(position)) {
            participant.teleport(position.toLocation(stayLoc.getWorld(), stayLoc.getYaw(), stayLoc.getPitch()));
        }
    }
}
