package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.games.game.capturetheflag.ClassPicker;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class ClassSelectionState extends CaptureTheFlagMatchStateBase {
    
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    
    public ClassSelectionState(CaptureTheFlagMatch context) {
        super(context);
        this.northClassPicker = new ClassPicker(context.getGameManager());
        this.southClassPicker = new ClassPicker(context.getGameManager());
        northClassPicker.start(
                context.getPlugin(), 
                context.getNorthTeam().getParticipants(), 
                context.getConfig().getLoadouts());
        southClassPicker.start(
                context.getPlugin(),
                context.getSouthTeam().getParticipants(), 
                context.getConfig().getLoadouts());
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
        } else {
            southClassPicker.addTeamMate(participant);
        }
    }
    
    @Override
    public void onNewParticipantJoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(true);
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.addTeamMate(participant);
        } else {
            southClassPicker.addTeamMate(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(CTFMatchParticipant participant, CTFMatchTeam team) {
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.removeTeamMate(participant);
        } else {
            southClassPicker.removeTeamMate(participant);
        }
        super.onParticipantQuit(participant, team);
    }
    
    @Override
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFMatchParticipant participant) {
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            northClassPicker.onClickInventory(event);
        } else {
            southClassPicker.onClickInventory(event);
        }
    }
}
