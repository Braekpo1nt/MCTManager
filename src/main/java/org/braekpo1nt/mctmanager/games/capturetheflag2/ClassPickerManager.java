package org.braekpo1nt.mctmanager.games.capturetheflag2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.capturetheflag.BattleClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClassPickerManager {
    
    public static final Component TITLE = Component.empty()
            .append(Component.text("Pick a Class")
                    .color(NamedTextColor.DARK_GRAY))
            .append(Component.text(" (One per team)")
                    .color(NamedTextColor.GRAY));
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private final Map<String, List<BattleClass>> classTracker = new HashMap<>();
    private final List<UUID> participantsWhoPickedClasses = new ArrayList<>();
    private final List<BattleClass> pickedBattleClasses = new ArrayList<>();

    /**
     * Converts a {@link Material} to a {@link BattleClass}
     * @param material The material to get the associated battle class of. 
     * @return The battle class associated with the given material. Null if the given material is not one of the associated material.
     */
    public static BattleClass materialToBattleClass(@NotNull Material material) {
        switch (material) {
            case STONE_SWORD -> {
                return BattleClass.KNIGHT;
            }
            case BOW -> {
                return BattleClass.ARCHER;
            }
            case IRON_SWORD -> {
                return BattleClass.ASSASSIN;
            }
            case LEATHER_CHESTPLATE -> {
                return BattleClass.TANK;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Gets the pretty name of the battle class
     * @param battleClass The battle class to get the name of
     * @return The name of the battle class. Null if the battle class is not one with a name (shouldn't ever return null)
     */
    private String getBattleClassName(@NotNull BattleClass battleClass) {
        switch (battleClass) {
            case KNIGHT -> {
                return "Knight";
            }
            case ARCHER -> {
                return "Archer";
            }
            case ASSASSIN -> {
                return "Assassin";
            }
            case TANK -> {
                return "Tank";
            }
            default -> {
                return null;
            }
        }
    }

    public void playerSelectBattleClass(@NotNull Player participant, @NotNull BattleClass battleClass) {
        if (pickedBattleClasses.contains(battleClass)) {
            participant.sendMessage(Component.empty()
                            .append(Component.text("Someone on your team already selected "))
                            .append(Component.text(getBattleClassName(battleClass)))
                            .color(NamedTextColor.DARK_RED));
            return;
        }
        pickedBattleClasses.add(battleClass);
        assignClass(participant, battleClass);
        participantsWhoPickedClasses.add(participant.getUniqueId());
    }

    @EventHandler
    public void onCloseMenu(InventoryCloseEvent event) {
        if (!picking) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Player participant = ((Player) event.getPlayer());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        if (participantsWhoPickedClasses.contains(participant.getUniqueId())) {
            return;
        }
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.displayName(NETHER_STAR_NAME);
        netherStar.setItemMeta(netherStarMeta);
        participant.getInventory().addItem(netherStar);
        participant.sendMessage(Component.text("You didn't pick a class. Use the nether star to pick.").color(NamedTextColor.DARK_RED));
    }
    
    @EventHandler
    public void onClickNetherStar(PlayerInteractEvent event) {
        if (!picking) {
            return;
        }
        Player participant = event.getPlayer();
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() != Material.NETHER_STAR) {
            return;
        }
        ItemMeta netherStarMeta = item.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !netherStarMeta.displayName().equals(NETHER_STAR_NAME)) {
            return;
        }
        participant.getInventory().remove(item);
        showClassPickerGui(participant);
    }
    
    /**
     * Assign the given class to the given participant. Setting their inventory
     * and armor to the appropriate items. 
     * @param participant the participant to assign a class to
     * @param battleClass the class to assign
     */
    public void assignClass(Player participant, BattleClass battleClass) {
        switch (battleClass) {
            case KNIGHT -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                participant.sendMessage("Selected Knight");
            }
            case ARCHER -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.getInventory().addItem(new ItemStack(Material.BOW));
                participant.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                participant.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                participant.sendMessage("Selected Archer");
            }
            case ASSASSIN -> {
                participant.getInventory().clear();
                participant.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                participant.sendMessage("Selected Assassin");
            }
            case TANK -> {
                participant.getInventory().clear();
                participant.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                participant.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                participant.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                participant.sendMessage("Selected Tank");
            }
        }
    }
    
    /**
     * Goes through all given participants and assigns a class to any that don't
     * already have one. It will assign classes from the pool of unpicked classes
     * for that team. 
     * @param participants The list of participants to check for a class
     *                     and assign if absent. 
     */
    public void assignClassesToParticipantsWithoutClasses(List<Player> participants) {
        for (Player participant : participants) {
            if (!this.participantsWhoPickedClasses.contains(participant.getUniqueId())) {
                randomlyAssignClass(participant);
            }
        }
    }
    
    /**
     * Randomly assign a class to the given participant from the pool of classes
     * that are left (classes that other players on their team didn't pick).
     * If there are no classes left unpicked to assign, this method does nothing.
     * @param participant the participant to assign a class to
     * @throws NullPointerException if the participant is not in the game state
     */
    private void randomlyAssignClass(Player participant) {
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        if (!classTracker.containsKey(teamName)) {
            classTracker.put(teamName, new ArrayList<>());
        }
        List<BattleClass> pickedBattleClasses = this.classTracker.get(teamName);
        for (BattleClass battleClass : BattleClass.values()) {
            if (!pickedBattleClasses.contains(battleClass)) {
                pickedBattleClasses.add(battleClass);
                assignClass(participant, battleClass);
                return;
            }
        }
    }
    
    public void resetClassPickerTracker() {
        this.classTracker.clear();
        this.participantsWhoPickedClasses.clear();
    }
    
    public void startClassPicking(List<Player> participants) {
        picking = true;
        for (Player participant : participants) {
            showClassPickerGui(participant);
        }
    }
    
    public void stopClassPicking(List<Player> participants) {
        picking = false;
        resetClassPickerTracker();
        for (Player participant : participants) {
            participant.closeInventory();
        }
    }
    
    /**
     * Shows the given participant the Class Picker gui
     * @param participant The participant to show the gui to
     */
    private void showClassPickerGui(Player participant) {
        ItemStack knight = new ItemStack(Material.STONE_SWORD);
        ItemStack archer = new ItemStack(Material.BOW);
        ItemStack assassin = new ItemStack(Material.IRON_SWORD);
        ItemStack tank = new ItemStack(Material.LEATHER_CHESTPLATE);
        
        ItemMeta knightMeta = knight.getItemMeta();
        knightMeta.displayName(Component.text("Knight"));
        knightMeta.lore(Arrays.asList(
                Component.text("- Stone Sword"),
                Component.text("- Chest Plate"),
                Component.text("- Boots")
        ));
        knight.setItemMeta(knightMeta);
        
        ItemMeta archerMeta = archer.getItemMeta();
        archerMeta.displayName(Component.text("Archer"));
        archerMeta.lore(Arrays.asList(
                Component.text("- Bow"),
                Component.text("- 16 Arrows"),
                Component.text("- Wooden Sword"),
                Component.text("- Chest Plate"),
                Component.text("- Boots")
        ));
        archer.setItemMeta(archerMeta);
        
        ItemMeta assassinMeta = assassin.getItemMeta();
        assassinMeta.displayName(Component.text("Assassin"));
        assassinMeta.lore(Arrays.asList(
                Component.text("- Iron Sword"),
                Component.text("- No Armor")
        ));
        assassin.setItemMeta(assassinMeta);
        
        ItemMeta tankMeta = tank.getItemMeta();
        tankMeta.displayName(Component.text("Tank"));
        tankMeta.lore(Arrays.asList(
                Component.text("Comes with:"),
                Component.text("- Full Leather Armor"),
                Component.text("- No Sword")
        ));
        tank.setItemMeta(tankMeta);
        
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        newGui.setItem(getSlotIndex(1, 1), knight);
        newGui.setItem(getSlotIndex(1, 2), archer);
        newGui.setItem(getSlotIndex(1, 3), assassin);
        newGui.setItem(getSlotIndex(1, 4), tank);
        participant.openInventory(newGui);
    }
    
    public int getSlotIndex(int line, int column) {
        int slotIndex = (line - 1) * 9 + (column - 1);
        return slotIndex;
    }
    
}
