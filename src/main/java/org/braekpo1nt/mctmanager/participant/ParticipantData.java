package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public abstract class ParticipantData extends Participant {
    
    private int score;
    
    public ParticipantData(@NotNull Participant participant, int score) {
        super(participant);
        this.score = score;
    }
    
    @Override
    public int getScore() {
        return this.score;
    }
    
    /**
     * Add the given points and send a message to the participant
     * @param points the points to add
     */
    public void awardPoints(int points) {
        this.score += points;
        sendMessage(Component.text("+")
                .append(Component.text(points))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
    
    /**
     * Add the given points and send a message to the team members
     * @param points the points to add (will be truncated to {@link int})
     */
    public void awardPoints(double points) {
        awardPoints((int) points);
    }
    
    /**
     * Add the given points silently, without a message to the participant
     * @param points the points to add
     */
    public void addPoints(int points) {
        this.score += points;
    }
    
    /**
     * @param score the participant's new score
     */
    public void setScore(int score) {
        this.score = score;
    }
    
}
