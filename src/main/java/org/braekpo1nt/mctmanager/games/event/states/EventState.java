package org.braekpo1nt.mctmanager.games.event.states;

import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EventState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    void startEvent(@NotNull CommandSender sender, int numberOfGames);
    void onPlayerDamage(EntityDamageEvent event);
    void onClickInventory(InventoryClickEvent event);
    void onDropItem(PlayerDropItemEvent event);
    void gameIsOver(@NotNull GameType finishedGameType);
    void colossalCombatIsOver(@Nullable String winningTeam);
    void setMaxGames(@NotNull CommandSender sender, int newMaxGames);
    void stopColossalCombat(@NotNull CommandSender sender);
    void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam);
    default void cancelAllTasks() {
        // do nothing
    }
    default void readyUpParticipant(@NotNull Player participant) {
        // do nothing
    }
    default void unReadyParticipant(@NotNull Player participant) {
        // do nothing
    }
}
