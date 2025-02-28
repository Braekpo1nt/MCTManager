package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Contains logic common across all gameplay states, namely
 * {@link ActiveState} and {@link GracePeriodState}
 */
public abstract class GameplayState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    protected final GameManager gameManager;
    
    public GameplayState(@NotNull FarmRushGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        context.onTeamJoin(team);
        context.getTeams().get(participant.getTeamId()).getArena().openBarnDoor();
        FarmRushParticipant.QuitData quitData = context.getQuitDatas().remove(participant.getUniqueId());
        if (quitData != null) {
            context.initializeParticipant(participant, quitData.getScore());
        } else {
            context.initializeParticipant(participant, 0);
        }
        participant.setGameMode(GameMode.SURVIVAL);
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.displayScore(context.getParticipants().get(participant.getUniqueId()));
        context.displayScore(context.getTeams().get(team.getTeamId()));
    }
    
    @Override
    public void onParticipantQuit(FarmRushParticipant participant) {
        context.getQuitDatas().put(participant.getUniqueId(), participant.getQuitData());
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    protected void sellItemsOnCloseInventory(InventoryCloseEvent event, Participant participant) {
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
        FarmRushTeam team = context.getTeams().get(participant.getTeamId());
        Location delivery = team.getArena().getDelivery();
        if (!barrelPos.equals(delivery)) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (countParticipantViewers(viewers) > 1) {
            return;
        }
        ItemStack[] contents = inventory.getContents();
        List<ItemStack> items = Arrays.stream(contents).filter(Objects::nonNull).toList();
        Map<Material, Integer> soldItems = sellItems(items, team);
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack != null && soldItems.containsKey(itemStack.getType())) {
                int amountSold = soldItems.get(itemStack.getType());
                int oldStackAmount = itemStack.getAmount();
                int amountLeftInStack = oldStackAmount - amountSold;
                if (amountLeftInStack <= 0) {
                    contents[i] = null;
                } else {
                    itemStack.setAmount(amountLeftInStack);
                }
                soldItems.put(itemStack.getType(), Math.max(0, amountSold - oldStackAmount));
            }
        }
        inventory.setContents(contents);
    }
    
    /**
     * Sell the given items, award the appropriate points
     * @param itemsToSell the items to sell. Should not be null, should not contain null items. 
     * @param team the team who is being awarded the points and selling the items.
     * @return how many of each material type were sold
     */
    private Map<Material, Integer> sellItems(@NotNull List<@NotNull ItemStack> itemsToSell, FarmRushTeam team) {
        if (itemsToSell.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Material, Integer> materialTotals = new HashMap<>();
        for (ItemStack itemStack : itemsToSell) {
            Material material = itemStack.getType();
            if (context.getConfig().getMaterialScores().containsKey(material)) {
                int oldAmount = materialTotals.getOrDefault(material, 0);
                materialTotals.put(material, oldAmount+itemStack.getAmount());
            }
        }
        
        int totalAmountSold = 0;
        int totalScore = 0;
        Map<Material, Integer> soldItems = new HashMap<>(materialTotals.size());
        for (Map.Entry<Material, Integer> entry : materialTotals.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();
            
            ItemSale itemSale = context.getConfig().getMaterialScores().get(material);
            int requiredAmount = itemSale.getRequiredAmount();
            int pricePerRequiredAmount = itemSale.getScore();
            
            // a bundle is a multiple of the required amount for the material
            int bundlesSold = amount / requiredAmount;
            int salePrice = bundlesSold * pricePerRequiredAmount;
            int amountSold = bundlesSold * requiredAmount;
            if (amountSold > 0) {
                soldItems.put(material, amountSold);
            }
            
            totalAmountSold += amountSold;
            totalScore += salePrice;
        }
        
        if (totalAmountSold > 0) {
            Component message = Component.empty()
                    .append(Component.text("Sold "))
                    .append(Component.text(totalAmountSold))
                    .append(Component.text(" items"));
            for (UUID uuid : team.getMemberUUIDs()) {
                context.getParticipants().get(uuid).getPlayer().sendMessage(message);
            }
            if (totalScore > 0) {
                int multiplied = (int) (gameManager.getMultiplier() * totalScore);
                team.awardPoints(multiplied);
                context.displayScore(team);
            }
        }
        return soldItems;
    }
    
    @Override
    public void onPlaceBlock(BlockPlaceEvent event, Participant participant) {
        context.getPowerupManager().onPlaceBlock(event);
    }
    
    @Override
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        context.getPowerupManager().onPlayerOpenInventory(event);
    }
    
    /**
     * @param viewers the viewers of the inventory in question
     * @return the number of viewers of the inventory who are participants
     */
    private int countParticipantViewers(List<HumanEntity> viewers) {
        int count = 0;
        for (HumanEntity viewer : viewers) {
            if (context.getParticipants().containsKey(viewer.getUniqueId())) {
                count++;
            }
        }
        return count;
    }
}
