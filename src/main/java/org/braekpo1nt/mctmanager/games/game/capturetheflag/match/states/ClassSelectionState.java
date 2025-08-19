package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.games.game.capturetheflag.ClassPicker;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ClassSelectionState extends CaptureTheFlagMatchStateBase {
    
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    
    public ClassSelectionState(CaptureTheFlagMatch context) {
        super(context);
        this.northClassPicker = new ClassPicker(
                context.getPlugin(),
                context.getNorthTeam().getParticipants(),
                context.getNorthTeam().getBukkitColor(),
                context.getConfig().getLoadouts());
        this.southClassPicker = new ClassPicker(
                context.getPlugin(),
                context.getSouthTeam().getParticipants(),
                context.getSouthTeam().getBukkitColor(),
                context.getConfig().getLoadouts());
    }
    
    @Override
    public void enter() {
        northClassPicker.start();
        southClassPicker.start();
    }
    
    @Override
    public void exit() {
        northClassPicker.stop(false);
        southClassPicker.stop(false);
    }
    
    @Override
    public void nextState() {
        northClassPicker.stop(true);
        southClassPicker.stop(true);
        context.setState(new MatchActiveState(context));
    }
    
    @Override
    public void cleanup() {
        northClassPicker.stop(false);
        southClassPicker.stop(false);
    }
    
    @Override
    public void onParticipantRejoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        super.onParticipantRejoin(participant, team);
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.addTeamMate(participant);
            participant.teleport(context.getArena().northSpawn());
        } else {
            southClassPicker.addTeamMate(participant);
            participant.teleport(context.getArena().southSpawn());
        }
    }
    
    @Override
    public void onNewParticipantJoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(true);
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.addTeamMate(participant);
            participant.teleport(context.getArena().northSpawn());
        } else {
            southClassPicker.addTeamMate(participant);
            participant.teleport(context.getArena().southSpawn());
        }
    }
    
    @Override
    public void onParticipantQuit(CTFMatchParticipant participant, CTFMatchTeam team) {
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.removeTeamMate(participant.getUniqueId());
        } else {
            southClassPicker.removeTeamMate(participant.getUniqueId());
        }
        super.onParticipantQuit(participant, team);
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, CTFMatchParticipant participant) {
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            event.setRespawnLocation(context.getArena().northSpawn());
        } else {
            event.setRespawnLocation(context.getArena().southSpawn());
        }
    }
}
