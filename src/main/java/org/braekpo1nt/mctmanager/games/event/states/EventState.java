package org.braekpo1nt.mctmanager.games.event.states;

import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public interface EventState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    void startEvent(CommandSender sender, int numberOfGames);
    void stopEvent(CommandSender sender);
    void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex);
    void onPlayerDamage(EntityDamageEvent event);
    void onClickInventory(InventoryClickEvent event);
    void onDropItem(PlayerDropItemEvent event);
    void gameIsOver(GameType finishedGameType);
}
