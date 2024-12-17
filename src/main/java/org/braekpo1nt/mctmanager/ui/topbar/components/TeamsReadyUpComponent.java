package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.ui.UIUtils;
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
        /**
         * the number of team members who are ready
         */
        private long readyCount;
        private final TextColor color;
        @SuppressWarnings("UnnecessaryUnicodeEscape")
        public Component toComponent() {
            if (readyCount < 0) {
                return Component.empty()
                        .append(Component.text("\u2713")
                                .decorate(TextDecoration.BOLD)
                                .color(color));
            }
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
    
    public void setReadyCount(@NotNull String teamId, long readyCount) {
        Team team = teams.get(teamId);
        if (team == null) {
            UIUtils.logUIError("teamId \"%s\" is not contained in this ReadyUpComponent", teamId);
            return;
        }
        team.setReadyCount(readyCount);
    }
    
    public Component toComponent() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Teams: ")
                .color(NamedTextColor.GRAY));
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
