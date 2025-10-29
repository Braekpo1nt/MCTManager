package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public void removeTeam(@NotNull String teamId) {
        teams.remove(teamId);
    }
    
    public void setAliveCount(@NotNull String teamId, int aliveCount) {
        Team team = teams.get(teamId);
        if (team == null) {
            UIUtils.logUIError("teamId \"%s\" is not contained in this ManyTeamsComponent", teamId);
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
}
