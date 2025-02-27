package org.braekpo1nt.mctmanager.participant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;

public class ScoredTeamData<T extends Participant> extends TeamData<T> {
    
    private int score;
    
    public ScoredTeamData(Team team, int score) {
        super(team);
        this.score = score;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getScore() {
        return score;
    }
    
    /**
     * Add the given points and send a message to the team members
     * @param points the points to add
     */
    public void awardPoints(int points) {
        this.score += points;
        Main.logger().info(String.format("awarded %d points to %s", points, getTeamId()));
        sendMessage(Component.text("+")
                .append(Component.text(points))
                .append(Component.text(" points for "))
                .append(getFormattedDisplayName())
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
     * Add the given points silently, without a message to the team
     * @param points the points to add
     */
    public void addPoints(int points) {
        this.score += points;
    }
    
    /**
     * @param score the team's new score
     */
    public void setScore(int score) {
        this.score = score;
    }
}
