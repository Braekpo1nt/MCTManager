package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ActiveState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    private final GameManager gameManager;
    
    public ActiveState(@NotNull FarmRushGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        for (FarmRushGame.Participant participant : context.getParticipants().values()) {
            participant.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .onCompletion(() -> context.setState(new GameOverState(context)))
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(FarmRushGame.Participant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onCloseInventory(InventoryCloseEvent event, FarmRushGame.Participant participant) {
        if (!event.getInventory().getType().equals(InventoryType.BARREL)) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof Barrel barrel)) {
            return;
        }
        if (inventory.isEmpty()) {
            return;
        }
        Block block = barrel.getBlock();
        Location barrelPos = block.getLocation();
        FarmRushGame.Team team = context.getTeams().get(participant.getTeamId());
        Location delivery = team.getArena().getDelivery();
        if (!barrelPos.equals(delivery)) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (countParticipantViewers(viewers) > 1) {
            return;
        }
        List<ItemStack> items = Arrays.stream(inventory.getContents()).filter(Objects::nonNull).toList();
        sellItems(items, team);
        inventory.clear();
    }
    
    /**
     * Sell the given items, award the appropriate points
     * @param items the items to sell. Should not be null, should not contain null items. 
     * @param team the team who is being awarded the points and selling the items.
     */
    private void sellItems(@NotNull List<@NotNull ItemStack> items, FarmRushGame.Team team) {
        if (items.isEmpty()) {
            return;
        }
        int totalScore = 0;
        int totalAmount = 0;
        for (ItemStack item : items) {
            Integer materialScore = context.getConfig().getMaterialScores().get(item.getType());
            int amount = item.getAmount();
            totalAmount += amount;
            if (materialScore != null) {
                totalScore += materialScore * amount;
            }
        }
        if (totalAmount == 0) {
            return;
        }
        Component message = Component.empty()
                .append(Component.text("Sold "))
                .append(Component.text(totalAmount))
                .append(Component.text(" items"));
        for (UUID uuid : team.getMembers()) {
            context.getParticipants().get(uuid).getPlayer().sendMessage(message);
        }
        gameManager.awardPointsToTeam(team.getTeamId(), totalScore);
    }
    
    private int countParticipantViewers(List<HumanEntity> viewers) {
        int count = 0;
        for (HumanEntity viewer : viewers) {
            FarmRushGame.Participant participant = context.getParticipants().get(viewer.getUniqueId());
            if (participant != null) {
                if (participant.isAlive()) {
                    count++;
                }
            }
        }
        return count;
    }
}
