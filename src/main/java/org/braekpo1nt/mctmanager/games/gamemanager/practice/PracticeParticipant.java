package org.braekpo1nt.mctmanager.games.gamemanager.practice;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class PracticeParticipant extends Participant {
    
    /**
     * the last open gui for the {@link PracticeManager}, used for closing
     * inventories associated with PracticeManager, rather than normal inventories
     */
    private @Nullable ChestGui lastGui;
    /**
     * The invite associated with this participant. This participant
     * may have been the initiator, or may have received this invite
     */
    private @Nullable Invite invite;
    
    public PracticeParticipant(@NotNull Participant participant) {
        super(participant);
    }
    
    /**
     * Simple convenience method to show the participant a given gui
     * @param gui the gui to show the participant
     */
    public void showGui(@NotNull ChestGui gui) {
        gui.show(getPlayer());
        lastGui = gui;
    }
    
    /**
     * If there is a last gui opened from PracticeManager, closes the inventory 
     */
    public void closeLastGui() {
        if (lastGui == null) {
            return;
        }
        lastGui.getInventory().close();
        lastGui = null;
    }
}
