package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FarmRushParticipant extends ParticipantData {
    public FarmRushParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
    }
    
    public FarmRushParticipant(@NotNull Participant participant, @NotNull QuitData quitData) {
        this(participant, quitData.getScore());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), player.getInventory().getContents());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final ItemStack[] inventory;
    }
}
