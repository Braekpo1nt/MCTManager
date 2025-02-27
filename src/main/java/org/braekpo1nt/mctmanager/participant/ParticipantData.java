package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public class ParticipantData extends Participant {
    
    protected int score;
    
    public ParticipantData(@NotNull Participant participant, int score) {
        super(participant);
        this.score = score;
    }
    
    public ParticipantData(@NotNull Participant participant) {
        super(participant);
        this.score = 0;
    }
    
    @Override
    public int getScore() {
        return this.score;
    }
    
    public void awardPoints(int score) {
        this.score += score;
        sendMessage(Component.text("+")
                .append(Component.text(score))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
}
