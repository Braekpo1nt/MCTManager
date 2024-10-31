package org.braekpo1nt.mctmanager.ui.topbar.components;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.topbar.TopbarException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A component representing many teams and their members' alive/dead status.
 * Can be converted to a {@link Component} for display to a user 
 */
public class ManyTeamsComponent {
    
    @Data
    @AllArgsConstructor
    private static class Team {
        private int aliveCount;
        private final TextColor color;
        
        public Component toComponent() {
            if (aliveCount > 0) {
                return Component.empty()
                        .append(Component.text(aliveCount)
                                .color(color));
            } else {
                return Component.empty()
                        .append(Component.text("x")
                                .color(color));
            }
        }
    }
    
    /**
     * Maps each teamId to the number of alive opponents
     */
    private final @NotNull Map<String, Team> teams = new HashMap<>();
    
    public void addTeam(@NotNull String teamId, @NotNull TextColor color) {
        teams.put(teamId, new Team(0, color));
    }
    
    public void setAliveCount(@NotNull String teamId, int aliveCount) {
        Team team = teams.get(teamId);
        if (team == null) {
            logUIError("teamId \"%s\" is not contained in this ManyTeamsComponent", teamId);
            return;
        }
        team.setAliveCount(aliveCount);
    }
    
    public Component toComponent() {
        TextComponent.Builder builder = Component.text();
        List<String> sortedTeams = teams.keySet().stream().sorted().toList();
        for (int i = 0; i < sortedTeams.size(); i++) {
            String teamId = sortedTeams.get(i);
            Team team = teams.get(teamId);
            builder.append(team.toComponent());
            if (i < sortedTeams.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.build();
    }
    
    /**
     * Log a UI error
     * @param reason the reason for the error (a {@link String#format(String, Object...)} template
     * @param args optional args for the reason format string
     */
    private void logUIError(@NotNull String reason, Object... args) {
        Main.logger().log(Level.SEVERE, "An error occurred in the ManyTeamsComponent. Failing gracefully.",
                new TopbarException(String.format(reason, args)));
    }
    
}
