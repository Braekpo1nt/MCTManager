package org.braekpo1nt.mctmanager.ui.topbar.components;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A component representing all teams readyup status
 * Can be converted to a {@link Component} for display to a user
 */
public class TeamsReadyUpComponent {
    
    @Data
    private static class Team {
    
        private int readyCount;
        private final TextColor color;
        public Component toComponent() {
            return Component.empty()
                    .append(Component.text(readyCount)
                            .color(color));
        }
    
    }
    /**
     * Maps each teamId to their component
     */
    private final @NotNull Map<String, Team> teams = new HashMap<>();
    
    public void addTeam(@NotNull String teamId, @NotNull TextColor color) {
        teams.put(teamId, new Team(color));
    }
    
    public void removeAllTeams() {
        teams.clear();
    }
    
    public void setReadyCount(@NotNull String teamId, int readyCount) {
        Team team = teams.get(teamId);
        Preconditions.checkArgument(team != null, "teamId \"%s\" is not contained in this ReadyUpComponent", teamId);
        team.setReadyCount(readyCount);
    }
    
    public Component toComponent() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Ready: "));
        List<String> sortedTeams = teams.keySet().stream().sorted().toList();
        for (int i = 0; i < sortedTeams.size(); i++) {
            String teamId = sortedTeams.get(i);
            Team team = teams.get(teamId);
            builder.append(team.toComponent());
            if (i < sortedTeams.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.asComponent();
    }
}
