package org.braekpo1nt.mctmanager.ui.topbar;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BattleTopbar extends Topbar {
    
    /**
     * A component representing a team and which members of it are alive/dead
     * can be converted to a {@link Component} for displaying to the user
     */
    public static class TeamComponent {
        
        private final @NotNull List<@NotNull Boolean> alive;
        private final @NotNull Component aliveComponent;
        private final @NotNull Component deadComponent;
        
        /**
         * Initial state of the team is that all members are alive
         * @param size the number of members in the team. Can't be negative.
         * @param color the color of the team, to be used in the living and dead components
         */
        public TeamComponent(int size, TextColor color) {
            alive = createAlive(size);
            aliveComponent = Component.text("O")
                    .decorate(TextDecoration.BOLD)
                    .color(color);
            deadComponent = Component.text("X")
                    .decorate(TextDecoration.BOLD)
                    .color(color);
        }
        
        /**
         * Initial state of the team is that all members are alive
         * @param size the number of members in the team. Can't be negative.
         * @param aliveComponent the component to represent a living member
         * @param deadComponent the component to represent a dead member
         */
        public TeamComponent(int size, @NotNull Component aliveComponent, @NotNull Component deadComponent) {
            alive = createAlive(size);
            this.aliveComponent = aliveComponent;
            this.deadComponent = deadComponent;
        }
        
        private @NotNull List<@NotNull Boolean> createAlive(int size) {
            Preconditions.checkArgument(size >= 0, "size can't be negative");
            List<Boolean> newAlive = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                newAlive.add(true);
            }
            return newAlive;
        }
        
        /**
         * Sets 'deaths' elements in the isLiving list to false from right to left, 
         * skipping any that are already false.
         * @param deaths the number of deaths to mark. 
         *               Nothing happens if deaths is negative, and if deaths is greater 
         *               than the number of members or living members in this TeamComponent, 
         *               all the members will be set to dead.
         */
        public void addDeaths(int deaths) {
            int count = 0;
            for (int i = alive.size() - 1; i >= 0 && count < deaths; i--) {
                if (alive.get(i)) {
                    alive.set(i, false);
                    count++;
                }
            }
        }
        
        /**
         * Sets 'living' elements in the alive list to true from left to right, 
         * skipping any that are already true.
         * @param living the number of living members to add.
         *               Nothing happens if the living is negative, and if living is greater
         *               than the number of members or dead members in this TeamComponent,
         *               all the members will be set to living
         */
        public void addLiving(int living) {
            int count = 0;
            for (int i = 0; i < alive.size() && count < living; i++) {
                if (!alive.get(i)) {
                    alive.set(i, true);
                    count++;
                }
            }
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
    
    /**
     * Represents two teams battling
     */
    public static class VersusComponent {
        private TeamComponent left;
        private TeamComponent right;
        
        
    }
    
}
