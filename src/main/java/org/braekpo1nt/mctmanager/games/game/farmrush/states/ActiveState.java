package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.jetbrains.annotations.Nullable;

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
        TopCommand.setEnabled(true);
        for (FarmRushGame.Team team : context.getTeams().values()) {
            team.getArena().openBarnDoor();
        }
    }
    
    @Override
    public void onParticipantJoin(Player player) {
        String teamId = context.getGameManager().getTeamId(player.getUniqueId());
        boolean brandNewTeam = !context.getTeams().containsKey(teamId);
        if (brandNewTeam) {
            context.onNewTeamJoin(teamId);
            context.getTeams().get(teamId).getArena().openBarnDoor();
        }
        context.initializeParticipant(player);
        player.setGameMode(GameMode.SURVIVAL);
        context.getSidebar().updateLine(player.getUniqueId(), "title", context.getTitle());
    }
    
    @Override
    public void onParticipantQuit(FarmRushGame.Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.getTeams().get(participant.getTeamId()).getMembers().remove(participant.getUniqueId());
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
    private Map<Material, Integer> sellItems(@NotNull List<@NotNull ItemStack> itemsToSell, FarmRushGame.Team team) {
        if (itemsToSell.isEmpty()) {
            return Collections.emptyMap();
        }
        
//        Map<Material, Integer> itemTotals = new HashMap<>();
        
        int totalAmount = 0;
        int totalScore = 0;
        Map<Material, Integer> soldItems = new HashMap<>(itemsToSell.size());
        for (ItemStack itemStack : itemsToSell) {
            Material material = itemStack.getType();
            int amount = itemStack.getAmount();
            
            ItemSale sellInfo = context.getConfig().getMaterialScores().get(material);
            if (sellInfo != null) {
                int requiredAmount = sellInfo.getRequiredAmount();
                int pricePerRequiredAmount = sellInfo.getScore();
                
                // a bundle is a multiple of the required amount for the material
                int bundlesSold = amount / requiredAmount;
                int salePrice = bundlesSold * pricePerRequiredAmount;
                
                totalScore += salePrice;
                totalAmount += bundlesSold*requiredAmount;
                
                if (bundlesSold > 0) {
                    Integer oldAmount = soldItems.getOrDefault(material, 0);
                    soldItems.put(material, oldAmount+(bundlesSold*requiredAmount));
                }
            }
        }
        
        if (totalAmount > 0) {
            Component message = Component.empty()
                    .append(Component.text("Sold "))
                    .append(Component.text(totalAmount))
                    .append(Component.text(" items"));
            for (UUID uuid : team.getMembers()) {
                context.getParticipants().get(uuid).getPlayer().sendMessage(message);
            }
            if (totalScore > 0) {
                gameManager.awardPointsToTeam(team.getTeamId(), totalScore);
            }
        }
        Main.logger().info(String.format("soldItems: %s", soldItems));
        return soldItems;
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
