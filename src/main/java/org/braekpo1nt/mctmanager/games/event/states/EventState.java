package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface EventState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber);
    void onParticipantDamage(EntityDamageEvent event);
    void onClickInventory(InventoryClickEvent event, Participant participant);
    void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant);
    void gameIsOver(@NotNull GameType finishedGameType);
    void colossalCombatIsOver(@Nullable Team winningTeam);
    void setMaxGames(@NotNull CommandSender sender, int newMaxGames);
    void stopColossalCombat(@NotNull CommandSender sender);
    void startColossalCombat(@NotNull CommandSender sender, @NotNull Team firstTeam, @NotNull Team secondTeam);
    void updatePersonalScores(Collection<Participant> updateParticipants);
    <T extends Team> void updateTeamScores(Collection<T> updateTeams);
    static void reorderTeamLines(List<Team> sortedTeamIds, EventManager context) {
        String[] teamKeys = new String[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            teamKeys[i] = "team"+i;
        }
        context.getSidebar().deleteLines(teamKeys);
        context.getAdminSidebar().deleteLines(teamKeys);
        
        context.setNumberOfTeams(sortedTeamIds.size());
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeamIds.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().addLines(0, teamLines);
        context.getAdminSidebar().addLines(0, teamLines);
    }
    default void cancelAllTasks() {
        // do nothing
    }
    default void readyUpParticipant(@NotNull Participant participant) {
        // do nothing
    }
    default void unReadyParticipant(@NotNull Participant participant) {
        // do nothing
    }
    default void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        // do nothing
    }
}
