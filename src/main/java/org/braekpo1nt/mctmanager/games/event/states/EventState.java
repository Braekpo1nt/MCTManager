package org.braekpo1nt.mctmanager.games.event.states;

import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
