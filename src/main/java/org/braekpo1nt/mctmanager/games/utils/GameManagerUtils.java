package org.braekpo1nt.mctmanager.games.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GameManagerUtils {
    
    public static final String TEAM_NAME_REGEX = "[-+\\._A-Za-z0-9]+";
    
    public final static List<EntityDamageEvent.DamageCause> EXCLUDED_CAUSES = List.of(
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.KILL
    );
    
    public static final List<Material> SIGNS = List.of(
            Material.OAK_SIGN,
            Material.SPRUCE_SIGN,
            Material.BIRCH_SIGN,
            Material.JUNGLE_SIGN,
            Material.ACACIA_SIGN,
            Material.DARK_OAK_SIGN,
            Material.CRIMSON_SIGN,
            Material.WARPED_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.CRIMSON_WALL_SIGN,
            Material.WARPED_WALL_SIGN
    );
    
    /**
     * A list of all the {@link InventoryAction}s which constitute
     * removing items from the player's inventory
     */
    public final static List<InventoryAction> INV_REMOVE_ACTIONS = List.of(InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
    public static final NamespacedKey IGNORE_TEAM_COLOR = NamespacedKey.minecraft("ignoreteamcolor");
    
    /**
     * returns a list that contains the first place, or first place ties.
     * @param teamScores a map pairing teamIds with scores
     * @return An array with the teamId who has the highest score. If there is a tie, returns all tied teamIds. If teamScores is empty, returns empty list.
     */
    public static @NotNull String[] calculateFirstPlace(@NotNull Map<String, Integer> teamScores) {
        if (teamScores.isEmpty()) {
            return new String[0];
        }
        if (teamScores.size() == 1) {
            return teamScores.keySet().toArray(new String[0]);
        }
        
        Iterator<Map.Entry<String, Integer>> iterator = teamScores.entrySet().iterator();
        Map.Entry<String, Integer> initial = iterator.next();
        int firstPlaceScore = initial.getValue();
        List<String> firstPlaces = new ArrayList<>();
        firstPlaces.add(initial.getKey());
        
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> teamScore = iterator.next();
            String teamId = teamScore.getKey();
            int score = teamScore.getValue();
            if (score > firstPlaceScore) {
                firstPlaces.clear();
                firstPlaces.add(teamId);
                firstPlaceScore = score;
            } else if (score == firstPlaceScore) {
                firstPlaces.add(teamId);
            }
        }
        
        return firstPlaces.toArray(new String[0]);
    }
    
    public static boolean validTeamId(String teamId) {
        return teamId.matches(TEAM_NAME_REGEX);
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a Component with a formatted display of all team and player scores
     */
    public static Component getTeamDisplay(GameManager gameManager) {
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("Scores:\n")
                    .decorate(TextDecoration.BOLD));
        List<OfflineParticipant> offlineParticipants = getSortedOfflineParticipants(gameManager);
        List<Team> sortedTeams = getSortedTeams(gameManager);
        
        for (Team team : sortedTeams) {
            int teamScore = gameManager.getScore(team.getTeamId());
            messageBuilder.append(Component.empty()
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" - "))
                            .append(Component.text(teamScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                    .append(Component.text(":\n")));
            for (OfflineParticipant offlineParticipant : offlineParticipants) {
                int playerScore = gameManager.getScore(offlineParticipant.getUniqueId());
                if (offlineParticipant.getTeamId().equals(team.getTeamId())) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(offlineParticipant.displayName())
                            .append(Component.text(" - "))
                            .append(Component.text(playerScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                            .append(Component.newline()));
                }
            }
        }
        
        return messageBuilder.build();
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a sorted list of {@link OfflineParticipant}s. 
     * Sorted first by score from greatest to least, then alphabetically (A first, Z last).
     */
    public static @NotNull List<OfflineParticipant> getSortedOfflineParticipants(GameManager gameManager) {
        Collection<OfflineParticipant> offlineParticipants = gameManager.getOfflineParticipants();
        return sortOfflinePlayers(offlineParticipants, gameManager);
    }
    
    /**
     * Sorts the provided list of OfflinePlayer objects.
     * @param offlineParticipants each entry must have a UUID of a valid participant in the GameState of the given GameManager
     * @param gameManager the GameManager to get the data from
     * @return the given participants in a sorted list
     */
    public static List<OfflineParticipant> sortOfflinePlayers(Collection<OfflineParticipant> offlineParticipants, GameManager gameManager) {
        return offlineParticipants.stream().sorted((p1, p2) -> {
            int scoreComparison = gameManager.getScore(p2.getUniqueId()) - gameManager.getScore(p1.getUniqueId());
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return p1.getName().compareToIgnoreCase(p2.getName());
        }).toList();
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a sorted list of team names. Sorted first by score from greatest to least, then alphabetically (A to Z).
     */
    public static List<Team> getSortedTeams(GameManager gameManager) {
        return gameManager.getTeams().stream().sorted((t1, t2) -> {
            int scoreComparison = gameManager.getScore(t2.getTeamId()) - gameManager.getScore(t1.getTeamId());
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return t1.getDisplayName().compareToIgnoreCase(t2.getDisplayName());
        }).toList();
    }
    
    /**
     * This validation and creation is used across multiple commands, so I've put it here for easy replication. 
     * <br>
     * Makes sure the inputs are able to form a valid team that doesn't already exist. 
     * Returns a failure response if anything goes wrong that would prevent the team from being created, otherwise creates the given team. 
     * @param gameManager the GameManager to add the team to
     * @param teamId the teamId
     * @param teamDisplayName the team display name
     * @param colorString the string representing the team's color
     * @return a comprehensive message about the success or failure of the addition of the given team
     */
    public static CommandResult addTeam(GameManager gameManager, @NotNull String teamId, @NotNull String teamDisplayName, @NotNull String colorString) {
        if (gameManager.hasTeam(teamId)) {
            return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                    .append(Component.text(teamId))
                    .append(Component.text("\"")));
        }
        if (teamId.equals(GameManager.ADMIN_TEAM)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" cannot be "))
                    .append(Component.text(GameManager.ADMIN_TEAM)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because that is reserved for the admin team.")));
        }
        if (!GameManagerUtils.validTeamId(teamId)) {
            return CommandResult.failure(Component.text("Provide a valid team name\n")
                    .append(Component.text(
                            "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")));
        }
        
        if (teamDisplayName.isEmpty()) {
            return CommandResult.failure("Display name can't be blank");
        }
        
        if (!ColorMap.hasNamedTextColor(colorString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(colorString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a recognized color")));
        }
        
        Team team = gameManager.addTeam(teamId, teamDisplayName, colorString);
        if (team == null) {
            return CommandResult.failure("Unable to create team (already exists)");
        }
        return CommandResult.success(Component.text("Created team ")
                .append(team.getFormattedDisplayName())
                .append(Component.text(" (teamId=\""))
                .append(Component.text(teamId))
                .append(Component.text("\")")));
    }
    
    /**
     * Removes the specified team from the GameState, and leaves all participants of that team
     * @param sender the sender who will receive success/error messages
     * @param gameManager the GameManager to modify
     * @param teamId the teamId of the team to remove. Must be a valid teamId.
     * @return a CommandResult detailing what happened. 
     */
    public static CommandResult removeTeam(@NotNull CommandSender sender, @NotNull GameManager gameManager, @NotNull String teamId) {
        if (!gameManager.hasTeam(teamId)) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" does not exist")));
        }
        gameManager.removeTeam(sender, teamId);
        return CommandResult.success();
    }
    
    public static CommandResult joinParticipant(@NotNull CommandSender sender, Main plugin, @NotNull GameManager gameManager, @NotNull String ign, @NotNull String teamId) {
        if (teamId.isEmpty()) {
            return CommandResult.failure("teamId must not be blank");
        }
        if (ign.isEmpty()) {
            return CommandResult.failure("player name must not be blank");
        }
        Team team = gameManager.getTeam(teamId);
        if (team == null) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" does not exist.")));
        }
        
        OfflinePlayer playerToJoin = plugin.getServer().getOfflinePlayer(ign);
        gameManager.joinParticipantToTeam(sender, playerToJoin, ign, teamId);
        return CommandResult.success();
    }
    
    /**
     * Replaces instances of the given player's name in the given component with the player's display name. 
     * @param player the player whose name should be replaced with their display name
     * @param component the component in which the name should be replaced
     * @return a new component with the replacements. Null if the component is null
     */
    @Contract("_, null -> null")
    public static Component replaceWithDisplayName(@NotNull Player player, Component component) {
        if (component == null) {
            return null;
        }
        return component.replaceText(TextReplacementConfig.builder()
                .match(player.getName())
                .replacement(player.displayName())
                .build());
    }
    
    /**
     * Colors all leather armor in the equipment slots of the participant to be the team color
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of an item's LeatherArmorMeta contains the {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then that item will not be colored. 
     * @param gameManager the game manager in which the given participant should be contained
     * @param participant the participant whose armor slots may or may not contain leather armor, but for whom any existing leather armor slots should be colored their team color. If this participant is not a participiant in the given gameManager, then nothing happens. 
     */
    public static void colorLeatherArmor(@NotNull GameManager gameManager, @NotNull Participant participant) {
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        Color teamColor = gameManager.getTeam(participant).getBukkitColor();
        colorLeatherArmor(participant.getInventory().getHelmet(), teamColor);
        colorLeatherArmor(participant.getInventory().getChestplate(), teamColor);
        colorLeatherArmor(participant.getInventory().getLeggings(), teamColor);
        colorLeatherArmor(participant.getInventory().getBoots(), teamColor);
    }
    
    /**
     * Applies the given color to the LeatherArmorMeta of the given item, if it exists. If the item is null or has no LeatherArmorMeta, then nothing happens. 
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of the LeatherArmorMeta contains the {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then nothing will happen. 
     * @param leatherArmor the item to color the LeatherArmorMeta of
     * @param color the color to apply to the LeatherArmorMeta
     */
    public static void colorLeatherArmor(@Nullable ItemStack leatherArmor, @Nullable Color color) {
        if (leatherArmor == null) {
            return;
        }
        if (!(leatherArmor.getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta)) {
            return;
        }
        if (leatherArmorMeta.getPersistentDataContainer().has(IGNORE_TEAM_COLOR, PersistentDataType.STRING)) {
            return;
        }
        leatherArmorMeta.setColor(color);
        leatherArmor.setItemMeta(leatherArmorMeta);
    }
    
    /**
     * Removes any color from the LeatherArmorMeta of the given item, if it exists. If the item is null or has no LeatherArmorMeta, then nothing happens.
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of the LeatherArmorMeta contains the {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then nothing will happen. 
     * @param leatherArmor the item to color the LeatherArmorMeta of
     */
    public static void deColorLeatherArmor(@Nullable ItemStack leatherArmor) {
        if (leatherArmor == null) {
            return;
        }
        if (!(leatherArmor.getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta)) {
            return;
        }
        if (leatherArmorMeta.getPersistentDataContainer().has(IGNORE_TEAM_COLOR, PersistentDataType.STRING)) {
            return;
        }
        leatherArmorMeta.setColor(null);
        leatherArmor.setItemMeta(leatherArmorMeta);
    }
    
    /**
     * Removes the color from any and all leather armor items in the given list. Uses {@link GameManagerUtils#deColorLeatherArmor(ItemStack)} on each item in the list.
     * @param items the list of items
     * @see GameManagerUtils#deColorLeatherArmor(ItemStack) 
     */
    public static void deColorLeatherArmor(@Nullable List<@Nullable ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        items.forEach(GameManagerUtils::deColorLeatherArmor);
    }
    
    /**
     * Removes the color from any and all leather armor items in the given player's armor slots. Uses {@link GameManagerUtils#deColorLeatherArmor(ItemStack)} on each armor item.
     * @param inventory the inventory of the player wearing the armor
     * @see GameManagerUtils#deColorLeatherArmor(ItemStack)
     */
    public static void deColorLeatherArmor(@NotNull PlayerInventory inventory) {
        deColorLeatherArmor(inventory.getHelmet());
        deColorLeatherArmor(inventory.getChestplate());
        deColorLeatherArmor(inventory.getLeggings());
        deColorLeatherArmor(inventory.getBoots());
    }
    
    public static int calculateExpPoints(int level) {
        int maxExpPoints = level > 7 ? 100 : level * 7;
        return maxExpPoints / 10;
    }
    
    /**
     * Returns the formal placement title of the given place. 
     * 1 gives 1st, 2 gives second, 11 gives 11th, 103 gives 103rd.
     * @param placement A number representing the placement
     * @return The placement number with the appropriate postfix (st, nd, rd, th)
     */
    public static Component getPlacementTitle(int placement) {
        return Component.empty()
                .append(Component.text(placement))
                .append(Component.text(getStandingSuffix(placement)));
    }
    
    /**
     * Returns the number suffix title of the given standing. 
     * 1 gives 1st, 2 gives {@code "nd"}, 11 gives {@code "th"} (11th), 
     * 103 gives {@code "rd"} (103rd).
     * @param standing A number representing the standing
     * @return The appropriate suffix for the standing (st, nd, rd, th)
     */
    public static String getStandingSuffix(int standing) {
        if (standing % 100 >= 11 && standing % 100 <= 13) {
            return "th";
        } else {
            return switch (standing % 10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }
    }
    
    /**
     * Returns the formal placement title of the given place. 
     * 1 gives 1st, 2 gives second, 11 gives 11th, 103 gives 103rd.
     * @param placement A number representing the placement
     * @return The placement number with the appropriate postfix (st, nd, rd, th)
     */
    public static String getPlacementTitleString(int placement) {
        return placement + getStandingSuffix(placement);
    }
}
