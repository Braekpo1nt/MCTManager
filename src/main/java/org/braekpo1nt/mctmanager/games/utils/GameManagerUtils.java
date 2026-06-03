package org.braekpo1nt.mctmanager.games.utils;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetOpts;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class GameManagerUtils {
    
    public static final String TEAM_NAME_REGEX = "[-+\\._A-Za-z0-9]+";
    
    public final static Set<EntityDamageEvent.DamageCause> EXCLUDED_DAMAGE_CAUSES = Set.of(
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.KILL
    );
    
    /**
     * A list of all the {@link InventoryAction}s which constitute
     * removing items from the player's inventory
     */
    public final static List<InventoryAction> INV_REMOVE_ACTIONS = List.of(InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
    public static final NamespacedKey IGNORE_TEAM_COLOR = NamespacedKey.minecraft("ignoreteamcolor");
    private static final int NUM_OF_PARTICIPANTS_PER_CYCLE = 5;
    private static final long DELAY_BETWEEN_CYCLE_IN_SECONDS = 5L;
    
    
    /**
     * returns a list that contains the first place, or first place ties.
     * @param teamScores a map pairing teamIds with scores
     * @return An array with the teamId who has the highest score. If there is a tie, returns all tied teamIds. If
     * teamScores is empty, returns empty list.
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
        List<Team> sortedTeams = gameManager.getSortedTeams();
        
        for (Team team : sortedTeams) {
            messageBuilder.append(Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" - "))
                    .append(Component.text(team.getScore())
                            .decorate(TextDecoration.BOLD)
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(":\n")));
            for (OfflineParticipant offlineParticipant : offlineParticipants) {
                if (offlineParticipant.getTeamId().equals(team.getTeamId())) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(offlineParticipant.displayName())
                            .append(Component.text(" - "))
                            .append(Component.text(offlineParticipant.getScore())
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
        return sortOfflinePlayers(offlineParticipants);
    }
    
    /**
     * Sorts the provided list of OfflinePlayer objects.
     * @param offlineParticipants each entry must have a UUID of a valid participant in the GameState of the given
     * GameManager
     * @return the given participants in a sorted list
     */
    public static List<OfflineParticipant> sortOfflinePlayers(Collection<OfflineParticipant> offlineParticipants) {
        return offlineParticipants.stream().sorted((p1, p2) -> {
            int scoreComparison = p2.getScore() - p1.getScore();
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return p1.getName().compareToIgnoreCase(p2.getName());
        }).toList();
    }
    
    /**
     * This validation and creation is used across multiple commands, so I've put it here for easy replication.
     * <br>
     * Makes sure the inputs are able to form a valid team that doesn't already exist.
     * Returns a failure response if anything goes wrong that would prevent the team from being created, otherwise
     * creates the given team.
     * @param gameManager the GameManager to add the team to
     * @param teamId the teamId
     * @param teamDisplayName the team display name
     * @param colorString the string representing the team's color
     * @return a comprehensive message about the success or failure of the addition of the given team
     */
    public static CompletableFuture<CommandResult> addTeam(GameManager gameManager, @NotNull String teamId, @NotNull String teamDisplayName, @NotNull String colorString) {
        Team existingTeam = gameManager.getTeam(teamId);
        if (existingTeam != null) {
            return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                            .append(Component.text(teamId))
                            .append(Component.text("\"")))
                    .asFuture();
        }
        if (teamId.equals(GameManager.ADMIN_TEAM)) {
            return CommandResult.failure(Component.empty()
                            .append(Component.text(teamId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" cannot be "))
                            .append(Component.text(GameManager.ADMIN_TEAM)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" because that is reserved for the admin team.")))
                    .asFuture();
        }
        if (!GameManagerUtils.validTeamId(teamId)) {
            return CommandResult.failure(Component.text("Provide a valid team name\n")
                            .append(Component.text(
                                    "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")))
                    .asFuture();
        }
        
        if (teamDisplayName.isEmpty()) {
            return CommandResult.failure("Display name can't be blank")
                    .asFuture();
        }
        
        if (!ColorMap.hasNamedTextColor(colorString)) {
            return CommandResult.failure(Component.empty()
                            .append(Component.text(colorString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized color")))
                    .asFuture();
        }
        
        return gameManager.addTeam(teamId, teamDisplayName, colorString)
                .thenApply(team -> {
                    if (team == null) {
                        return CommandResult.failure("Unable to create team (already exists)");
                    }
                    return CommandResult.success(Component.text("Created team ")
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" (teamId=\""))
                            .append(Component.text(teamId))
                            .append(Component.text("\")")));
                })
                ;
    }
    
    /**
     * Removes the specified team from the GameState, and leaves all participants of that team
     * @param gameManager the GameManager to modify
     * @param teamId the teamId of the team to remove. Must be a valid teamId.
     * @return a CommandResult detailing what happened.
     */
    public static CompletableFuture<CommandResult> removeTeam(@NotNull GameManager gameManager, @NotNull String teamId) {
        Team existingTeam = gameManager.getTeam(teamId);
        if (existingTeam == null) {
            return CommandResult.failure(Component.text("Team ")
                            .append(Component.text(teamId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" does not exist")))
                    .asFuture();
        }
        return gameManager.removeTeam(teamId);
    }
    
    /**
     * Replaces instances of the given name with the given display name in the given component.
     * @param name the name to replace
     * @param displayName the display name to replace it with
     * @param component the component to replace within
     * @return a new component with the replacements, Null if the component is null.
     */
    @Contract("_, _, null -> null")
    public static Component replaceWithDisplayName(@RegExp @NotNull String name, @NotNull Component displayName, Component component) {
        if (component == null) {
            return null;
        }
        return component.replaceText(TextReplacementConfig.builder()
                .match(name)
                .replacement(displayName)
                .build());
    }
    
    /**
     * Replaces instances of the given player's name in the given component with the player's display name.
     * @param player the player whose name should be replaced with their display name
     * @param component the component in which the name should be replaced
     * @return a new component with the replacements. Null if the component is null
     */
    @Contract("_, null -> null")
    public static Component replaceWithDisplayName(@NotNull Player player, Component component) {
        return replaceWithDisplayName(player.getName(), player.displayName(), component);
    }
    
    /**
     * Replaces instances of the given player's name in the given component with the player's display name.
     * @param participant the player whose name should be replaced with their display name
     * @param component the component in which the name should be replaced
     * @return a new component with the replacements. Null if the component is null
     */
    @Contract("_, null -> null")
    public static Component replaceWithDisplayName(@NotNull Participant participant, Component component) {
        return replaceWithDisplayName(participant.getName(), participant.displayName(), component);
    }
    
    /**
     * Applies the given color to the LeatherArmorMeta of the given item, if it exists. If the item is null or has no
     * LeatherArmorMeta, then nothing happens.
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of the LeatherArmorMeta contains the
     * {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then nothing will happen.
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
     * Removes any color from the LeatherArmorMeta of the given item, if it exists. If the item is null or has no
     * LeatherArmorMeta, then nothing happens.
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of the LeatherArmorMeta contains the
     * {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then nothing will happen.
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
     * Removes the color from any and all leather armor items in the given list. Uses
     * {@link GameManagerUtils#deColorLeatherArmor(ItemStack)} on each item in the list.
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
     * Removes the color from any and all leather armor items in the given player's armor slots. Uses
     * {@link GameManagerUtils#deColorLeatherArmor(ItemStack)} on each armor item.
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
     * @param name the name to turn into a display name
     * @param color the color to use for the display name
     * @return the display name from the given name and color
     */
    public static @NotNull Component createDisplayName(@NotNull String name, @NotNull TextColor color) {
        return Component.empty()
                .append(Component.text(name))
                .color(color);
    }
    
    /**
     * @deprecated because {@link PlayerArmorChangeEvent.SlotType} is deprecated
     */
    @Deprecated
    public static @Nullable EquipmentSlot toEquipmentSlot(@Nullable PlayerArmorChangeEvent.SlotType slotType) {
        switch (slotType) {
            case HEAD -> {
                return EquipmentSlot.HEAD;
            }
            case CHEST -> {
                return EquipmentSlot.CHEST;
            }
            case LEGS -> {
                return EquipmentSlot.LEGS;
            }
            case FEET -> {
                return EquipmentSlot.FEET;
            }
            case null, default -> {
                return null;
            }
        }
    }
    
    /**
     * Takes in a {@link PlayerDeathEvent} and replaces all instances of the given player's name with the given player's
     * display name
     * @param event the event
     * @param participant the player whose name should be replaced with their display name.
     */
    public static void replaceWithDisplayName(PlayerDeathEvent event, Participant participant) {
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Component newDeathMessage = replaceWithDisplayName(participant.getPlayer(), deathMessage);
            event.deathMessage(newDeathMessage);
        }
    }
    
    /**
     * @param item the item in question. If this is null, will return false.
     * @return true if the item is of a leather armor type, false otherwise. False if the given item is null.
     */
    @Contract("null -> false")
    public static boolean isLeatherArmor(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getItemMeta() instanceof LeatherArmorMeta;
    }
    
    public static <T extends Team> List<Team> sortTeams(Collection<T> teamsToSort) {
        List<Team> sortedTeams = new ArrayList<>(teamsToSort);
        sortedTeams.sort(Comparator.comparing(Team::getScore, Comparator.reverseOrder()));
        sortedTeams.sort(Comparator
                .comparing(team -> ((Team) team).getScore())
                .reversed()
                .thenComparing(team -> ((Team) team).getTeamId())
        );
        return sortedTeams;
    }
    
    @Data
    private static class ParticipantInfo {
        private final UUID uuid;
        private final String ign;
        private final String teamId;
    }
    
    private static boolean presetApplyLock = false;
    
    /**
     * @param opts the options
     * @return a comprehensive {@link CompositeCommandResult} including every {@link CommandResult} of the (perhaps
     * many) operations performed here.
     * // TODO: this is architecturally sound, but very difficult to read. Improve readability.
     */
    public static @NotNull CompletableFuture<CommandResult> applyPreset(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull PresetStorageUtil storageUtil,
            @NotNull File presetFile,
            @NotNull CommandSender sender,
            @NotNull PresetOpts opts) {
        if (presetApplyLock) {
            return CommandResult.failure("A preset is currently being applied, please wait for its completion.").asFuture();
        }
        presetApplyLock = true;
        Preset preset;
        try {
            preset = storageUtil.loadPreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            presetApplyLock = false;
            return CommandResult.failure(Component.empty()
                            .append(Component.text("Error occurred loading preset. See console for details: "))
                            .append(Component.text(e.getMessage())))
                    .asFuture();
        }
        
        
        List<CommandResult> results = new LinkedList<>();
        
        Collection<OfflineParticipant> offlineParticipants = gameManager.getOfflineParticipants();
        if (opts.unWhitelist()) {
            int count = 0;
            for (OfflineParticipant offlineParticipant : offlineParticipants) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(offlineParticipant.getUniqueId());
                offlinePlayer.setWhitelisted(false);
                count++;
            }
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Removed "))
                    .append(Component.text(count))
                    .append(Component.text(" participants from the whitelist"))));
        }
        
        // check if they want to overwrite or merge the game state
        CompletableFuture<List<CommandResult>> chain = CompletableFuture.completedFuture(results);
        if (opts.override()) {
            // remove all existing teams and leave all existing players
            int oldParticipantCount = offlineParticipants.size();
            Set<String> teamIds = gameManager.getTeamIds();
            int oldTeamCount = teamIds.size();
            for (String teamId : teamIds) {
                chain = chain.thenComposeAsync(commandResults -> {
                    CompletableFuture<CommandResult> joinFuture = removeTeam(gameManager, teamId);
                    return joinFuture.thenApply(result -> {
                        commandResults.add(result);
                        return commandResults;
                    });
                }, plugin.getMainThreadExecutor());
            }
            chain = chain.thenApply(commandResults -> {
                        commandResults.add(CommandResult.success(Component.empty()
                                .append(Component.text("Removed "))
                                .append(Component.text(oldTeamCount))
                                .append(Component.text(" team(s) and left "))
                                .append(Component.text(oldParticipantCount))
                                .append(Component.text(" participants"))));
                        return commandResults;
                    }
            );
        }
        chain = chain.exceptionally(e -> List.of(CommandResult.throwable("remove the teams", e)));
        // add all the teams
        int teamCount = preset.getTeamCount();
        int participantCount = preset.getParticipantCount();
        for (Preset.PresetTeam team : preset.getTeams()) {
            chain = chain.thenComposeAsync(commandResults -> {
                Team realTeam = gameManager.getTeam(team.getTeamId());
                if (realTeam != null) {
                    commandResults.add(CommandResult.success(Component.empty()
                            .append(realTeam.getFormattedDisplayName())
                            .append(Component.text(" already exists."))
                    ));
                    return CompletableFuture.completedFuture(commandResults);
                } else {
                    CompletableFuture<CommandResult> joinFuture = addTeam(gameManager, team.getTeamId(), team.getDisplayName(), team.getColor());
                    return joinFuture.thenApply(result -> {
                        commandResults.add(result);
                        return commandResults;
                    });
                }
            }, plugin.getMainThreadExecutor());
        }
        chain = chain.exceptionally(e -> List.of(CommandResult.throwable("create the teams", e)));
        return chain.thenApplyAsync(v -> {
            List<ParticipantInfo> participantInfos = new ArrayList<>(participantCount);
            for (Preset.PresetTeam team : preset.getTeams()) {
                for (Preset.PresetParticipant member : team.getMembers()) {
                    participantInfos.add(new ParticipantInfo(
                            member.getUuid(),
                            member.getIgn(),
                            team.getTeamId()
                    ));
                }
            }
            
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Successfully added "))
                    .append(Component.text(teamCount))
                    .append(Component.text(" team(s)")))
            );
            
            // join all the participants in a staggered formation to prevent crashes
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text(participantCount))
                    .append(Component.text(" participants to join. "))
                    .append(Component.text(DELAY_BETWEEN_CYCLE_IN_SECONDS))
                    .append(Component.text(" second delay before first wave of "))
                    .append(Component.text(Math.min(NUM_OF_PARTICIPANTS_PER_CYCLE, participantCount)))
                    .append(Component.text(" participants."))
            ));
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                bulkJoinParticipants(
                        participantInfos,
                        plugin,
                        gameManager,
                        sender,
                        opts,
                        0,
                        preset.getTeams()
                );
            }, 20 * DELAY_BETWEEN_CYCLE_IN_SECONDS);
            
            return CompositeCommandResult.all(results);
        }, plugin.getMainThreadExecutor());
    }
    
    /**
     * A recursive operation with a delay
     */
    private static void bulkJoinParticipants(
            List<ParticipantInfo> participantInfos,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull CommandSender sender,
            @NotNull PresetOpts opts,
            int startIndex,
            @NotNull List<Preset.PresetTeam> teams
    ) {
        if (startIndex >= participantInfos.size()) {
            // termination condition
            applyRestOfSettings(
                    plugin,
                    gameManager,
                    sender,
                    opts,
                    teams,
                    participantInfos.size()
            );
            presetApplyLock = false;
            return;
        }
        CompletableFuture<List<CommandResult>> chain = CompletableFuture.completedFuture(new ArrayList<>());
        // join the first 5 participants
        int maxIndex = Math.min(startIndex + NUM_OF_PARTICIPANTS_PER_CYCLE, participantInfos.size());
        int left = participantInfos.size() - maxIndex;
        for (int i = startIndex; i < maxIndex; i++) {
            ParticipantInfo participantInfo = participantInfos.get(i);
            chain = chain.thenComposeAsync(results -> {
                CompletableFuture<CommandResult> joinFuture;
                Player player = plugin.getServer().getPlayer(participantInfo.getUuid());
                if (player != null) {
                    // they are online
                    joinFuture = gameManager.joinOnlineParticipant(player, participantInfo.getTeamId());
                } else {
                    // they are not online
                    joinFuture = gameManager.joinOfflineParticipant(participantInfo.getUuid(), participantInfo.getIgn(), participantInfo.getTeamId());
                }
                return joinFuture.thenApply(result -> {
                    results.add(result);
                    return results;
                });
            }, plugin.getMainThreadExecutor());
        }
        chain.thenApply(results -> {
                    if (left > 0) {
                        results.add(CommandResult.success(Component.empty()
                                .append(Component.text(left))
                                .append(Component.text(" participants to go. "))
                                .append(Component.text(DELAY_BETWEEN_CYCLE_IN_SECONDS))
                                .append(Component.text(" second delay before next wave of "))
                                .append(Component.text(Math.min(NUM_OF_PARTICIPANTS_PER_CYCLE, left)))
                                .append(Component.text(" participants."))
                        ));
                    } else {
                        results.add(CommandResult.success(Component.empty()
                                .append(Component.text("All "))
                                .append(Component.text(participantInfos.size()))
                                .append(Component.text(" participants joined. "))
                                .append(Component.text(DELAY_BETWEEN_CYCLE_IN_SECONDS))
                                .append(Component.text(" second delay before final preset options are applied."))
                        ));
                    }
                    return results;
                })
                .thenAccept(results -> {
                    CommandResult.showResult(sender, CompositeCommandResult.all(results));
                });
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            bulkJoinParticipants(
                    participantInfos,
                    plugin,
                    gameManager,
                    sender,
                    opts,
                    maxIndex,
                    teams
            );
        }, 20L * DELAY_BETWEEN_CYCLE_IN_SECONDS);
    }
    
    /**
     * These operations must be done after all the participants from the preset have
     * been joined to their teams, which happens in
     * {@link #bulkJoinParticipants(List, Main, GameManager, CommandSender, PresetOpts, int, List)}.
     */
    private static void applyRestOfSettings(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull CommandSender sender,
            @NotNull PresetOpts opts,
            @NotNull List<Preset.PresetTeam> teams,
            int participantCount
    ) {
        List<CommandResult> results = new ArrayList<>();
        results.add(CommandResult.success(Component.empty()
                .append(Component.text("Successfully added "))
                .append(Component.text(teams.size()))
                .append(Component.text(" team(s) and joined "))
                .append(Component.text(participantCount))
                .append(Component.text(" participant(s).")))
        );
        if (opts.resetScores()) {
            gameManager.setScoreAll(0, "apply preset reset scores command");
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("All team and player scores have been set to 0"))));
        }
        
        if (opts.whiteList()) {
            for (Preset.PresetTeam team : teams) {
                for (Preset.PresetParticipant member : team.getMembers()) {
                    OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(member.getUuid());
                    if (!offlinePlayer.isWhitelisted()) {
                        offlinePlayer.setWhitelisted(true);
                    }
                }
            }
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Whitelisted "))
                    .append(Component.text(participantCount))
                    .append(Component.text(" participant(s)"))));
        }
        
        if (opts.kickUnWhitelisted()) {
            int kickCount = 0;
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.isWhitelisted() && !onlinePlayer.isOp()) {
                    onlinePlayer.kick(Component.empty()
                            .append(Component.text("You are not whitelisted on this server.")));
                    kickCount++;
                }
            }
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Kicked "))
                    .append(Component.text(kickCount))
                    .append(Component.text(" un-whitelisted player(s)"))));
        }
        CommandResult result = CompositeCommandResult.all(results);
        CommandResult.showResult(sender, result);
    }
}
