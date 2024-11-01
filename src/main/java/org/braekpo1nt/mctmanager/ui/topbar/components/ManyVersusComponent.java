package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents many instances of pairs of teams battling.
 * Not to be confused with {@link VersusManyComponent}
 */
public class ManyVersusComponent {
    
    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @AllArgsConstructor
    private static class Team {
        
        @EqualsAndHashCode.Include
        private final String teamId;
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
    @Data
    private static class TeamPair implements Comparable<TeamPair> {
        
        private final Team left;
        private final Team right;
        public @Nullable Team getTeam(String teamId) {
            if (left.getTeamId().equals(teamId)) {
                return left;
            }
            if (right.getTeamId().equals(teamId)) {
                return right;
            }
            return null;
        }
        
        public Component toComponent() {
            TextComponent.Builder builder = Component.text();
            builder.append(left.toComponent());
            builder.append(Component.text("/")
                    .color(NamedTextColor.GRAY));
            builder.append(right.toComponent());
            return builder.build();
        }
        
        @Override
        public int compareTo(@NotNull ManyVersusComponent.TeamPair o) {
            int l = this.left.teamId.compareTo(o.left.teamId);
            if (l != 0) {
                return l;
            }
            return this.right.teamId.compareTo(o.right.teamId);
        }
        
    }
    /**
     * Maps teamIds to its {@link TeamPair}
     */
    private final List<TeamPair> teamPairs = new ArrayList<>();
    private final Map<String, Team> teams = new HashMap<>();
    
    public void addTeamPair(@NotNull String teamIdA, @NotNull TextColor colorA, @NotNull String teamIdB, @NotNull TextColor colorB) {
        Team teamA = new Team(teamIdA, 0, colorA);
        Team teamB = new Team(teamIdB, 0, colorB);
        teams.put(teamIdA, teamA);
        teams.put(teamIdB, teamB);
        TeamPair teamPair;
        if (teamIdA.compareTo(teamIdB) < 0) {
            teamPair = new TeamPair(teamA, teamB);
        } else {
            teamPair = new TeamPair(teamB, teamA);
        }
        teamPairs.add(teamPair);
        teamPairs.sort(TeamPair::compareTo);
    }
    
    public void removeTeamPair(@NotNull String teamIdA, @NotNull String teamIdB) {
        Iterator<TeamPair> iterator = teamPairs.iterator();
        while (iterator.hasNext()) {
            TeamPair next = iterator.next();
            if (next.getTeam(teamIdA) != null && next.getTeam(teamIdB) != null) {
                iterator.remove();
                break;
            }
        }
        teams.remove(teamIdA);
        teams.remove(teamIdB);
    }
    
    /**
     * Removes all teams and team pairs from this component
     */
    public void clear() {
        teamPairs.clear();
        teams.clear();
    }
    
    public void setAliveCount(@NotNull String teamId, int aliveCount) {
        Team team = teams.get(teamId);
        if (team == null) {
            UIUtils.logUIError("teamId \"%s\" is not contained in this ManyVersusComponent", teamId);
            return;
        }
        team.setAliveCount(aliveCount);
    }
    
    public Component toComponent() {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < teamPairs.size(); i++) {
            TeamPair teamPair = teamPairs.get(i);
            builder.append(teamPair.toComponent());
            if (i < teamPairs.size() - 1) {
                builder.append(Component.space());
            }
        }
        return builder.build();
    }
}
