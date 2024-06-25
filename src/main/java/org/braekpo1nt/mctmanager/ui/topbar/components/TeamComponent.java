package org.braekpo1nt.mctmanager.ui.topbar.components;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A component representing a team and which members of it are alive/dead
 * can be converted to a {@link Component} for displaying to the user
 */
public class TeamComponent {
    
    private @NotNull List<@NotNull Boolean> alive;
    private final @NotNull Component aliveComponent;
    private final @NotNull Component deadComponent;
    
    /**
     * Initial state of the team is that all members are alive
     *
     * @param size  the number of members in the team. Can't be negative.
     * @param color the color of the team, to be used in the living and dead components
     */
    public TeamComponent(int size, TextColor color) {
        alive = createAlive(size, 0);
        aliveComponent = Component.text("O")
                .decorate(TextDecoration.BOLD)
                .color(color);
        deadComponent = Component.text("x")
                .color(color);
    }
    
    /**
     * Initial state of the team is that all members are alive
     *
     * @param size           the number of members in the team. Can't be negative.
     * @param aliveComponent the component to represent a living member
     * @param deadComponent  the component to represent a dead member
     */
    public TeamComponent(int size, @NotNull Component aliveComponent, @NotNull Component deadComponent) {
        alive = createAlive(size, 0);
        this.aliveComponent = aliveComponent;
        this.deadComponent = deadComponent;
    }
    
    /**
     * Create the alive list based on the given numbers
     * @param living the number of living players on the team
     * @param dead the number of dead players on the team
     * @return a list containing `living` trues, and `dead` falses, in that order
     */
    private @NotNull List<@NotNull Boolean> createAlive(int living, int dead) {
        Preconditions.checkArgument(living >= 0, "living can't be negative");
        Preconditions.checkArgument(dead >= 0, "dead can't be negative");
        List<Boolean> newAlive = new ArrayList<>(living + dead);
        for (int i = 0; i < living; i++) {
            newAlive.add(true);
        }
        for (int i = 0; i < dead; i++) {
            newAlive.add(false);
        }
        return newAlive;
    }
    
    /**
     * set the members of this TeamComponent to the given number of living and dead
     * @param living the number of living players on this team
     * @param dead the number of dead players on this team
     */
    public void setMembers(int living, int dead) {
        alive = createAlive(living, dead);
    }
    
    /**
     * @return this TeamComponent as a {@link Component} object for display to the user
     */
    public @NotNull Component toComponent() {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < alive.size(); i++) {
            boolean isAlive = alive.get(i);
            builder.append(getStatusComponent(isAlive));
            if (i < alive.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.asComponent();
    }
    
    /**
     * @param isAlive whether to get an alive or dead status component
     * @return a component representing the alive or dead status (true is alive, false is dead)
     */
    private @NotNull Component getStatusComponent(boolean isAlive) {
        return isAlive ? aliveComponent : deadComponent;
    }
    
}
