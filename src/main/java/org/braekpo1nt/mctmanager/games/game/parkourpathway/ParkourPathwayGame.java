package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.RegularDescriptionState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.ParkourPathwayState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.states.TeamSpawnsDescription;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class ParkourPathwayGame extends GameBase<ParkourParticipant, ParkourTeam, ParkourParticipant.QuitData, ParkourTeam.QuitData, ParkourPathwayState> {
    
    private final ParkourPathwayConfig config;
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private int statusEffectsTaskId;
    
    public ParkourPathwayGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ParkourPathwayConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(new GameInstanceId(GameType.PARKOUR_PATHWAY, configFile), plugin, gameManager, title, new InitialState());
        this.config = config;
        startStatusEffectsTask();
        addListener(new PreventHungerLoss<>(this));
        addListener(new PreventItemDrop<>(this, true));
        closeGlassBarrier();
        start(newTeams, newParticipants, newAdmins);
    }
    
    private void closeGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.AIR, Material.GLASS);
        BlockPlacementUtils.updateDirection(config.getWorld(), glassBarrier);
    }
    
    public void openGlassBarrier() {
        BoundingBox glassBarrier = config.getGlassBarrier();
        if (glassBarrier == null) {
            return;
        }
        BlockPlacementUtils.createCubeReplace(config.getWorld(), glassBarrier, Material.GLASS, Material.AIR);
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull ParkourPathwayState getStartState() {
        if (config.getTeamSpawns() == null) {
            return new RegularDescriptionState(this);
        } else {
            return new TeamSpawnsDescription(this, config.getTeamSpawns());
        }
    }
    
    @Override
    protected void cleanup() {
        plugin.getServer().getScheduler().cancelTask(statusEffectsTaskId);
        openGlassBarrier();
    }
    
    @Override
    protected @NotNull ParkourParticipant createParticipant(Participant participant) {
        /*
         * Initialize chat mode for new participant
         */
        if (config.isChatToggleEnabled()) {
            return new ParkourParticipant(participant, config.getDefaultChatMode(), 0);
        } else {
            return new ParkourParticipant(participant, ChatMode.ALL, 0);
        }
    }
    
    @Override
    protected @NotNull ParkourParticipant createParticipant(Participant participant, ParkourParticipant.QuitData quitData) {
        return new ParkourParticipant(participant, quitData);
    }
    
    /**
     * Gives the appropriate number of skips to the given participant
     *
     * @param participant the participant to receive skips
     */
    public void giveSkipItem(ParkourParticipant participant, int numOfSkips) {
        if (numOfSkips <= 0) {
            return;
        }
        participant.getInventory().setItem(8, config.getSkipItem().asQuantity(numOfSkips));
    }
    
    /**
     * Gives the chat toggle item to the participant if enabled
     *
     * @param participant the participant to receive the chat toggle item
     */
    public void giveChatToggleItem(ParkourParticipant participant) {
        if (!config.isChatToggleEnabled()) {
            return;
        }
        ItemStack chatItem = config.getChatToggleItem().clone();
        updateChatToggleItemLore(participant, chatItem);
        participant.getInventory().setItem(0, chatItem);
    }
    
    /**
     * Updates the lore of the chat toggle item based on the player's current chat mode
     *
     * @param participant the participant whose chat item to update
     * @param chatItem    the chat toggle item to update
     */
    public void updateChatToggleItemLore(ParkourParticipant participant, ItemStack chatItem) {
        
        List<Component> lore = switch (participant.getChatMode()) {
            case ALL -> List.of(
                    Component.text("Chat Mode: §aAll Players"),
                    Component.text("Right click to change")
            );
            case TEAM -> List.of(
                    Component.text("Chat Mode: §eTeam Only"),
                    Component.text("Right click to change")
            );
            case OFF -> List.of(
                    Component.text("Chat Mode: §cDisabled"),
                    Component.text("Right click to change")
            );
            default -> List.of(Component.text("Right click to change"));
        };
        
        chatItem.editMeta(meta -> {
            meta.lore(lore);
        });
    }
    
    @Override
    protected @NotNull ParkourParticipant.QuitData getQuitData(ParkourParticipant participant) {
        return participant.getQuitData();
    }
    
    /**
     * Checks how many skips the player has, and awards them points for any remaining
     * skips. Removes them from their inventory as well.
     */
    public void awardPointsForUnusedSkips(ParkourParticipant participant) {
        if (config.getUnusedSkipScore() <= 0.0) {
            return;
        }
        if (participant.getUnusedSkips() > 0) {
            participant.sendMessage(Component.empty()
                    .append(Component.text(participant.getUnusedSkips()))
                    .append(Component.text(" unused skips"))
                    .color(NamedTextColor.GREEN));
            this.awardPoints(participant,
                    participant.getUnusedSkips() * config.getUnusedSkipScore());
        }
        participant.setUnusedSkips(0);
        ParticipantInitializer.clearInventory(participant);
    }
    
    @Override
    protected void initializeParticipant(ParkourParticipant participant, ParkourTeam team) {
        giveBoots(participant);
    }
    
    public void giveBoots(ParkourParticipant participant) {
        Color teamColor = teams.get(participant.getTeamId()).getBukkitColor();
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
    }
    
    @Override
    protected void initializeTeam(ParkourTeam team) {
        
    }
    
    @Override
    protected @NotNull ParkourTeam createTeam(Team team) {
        return new ParkourTeam(team, 0);
    }
    
    @Override
    protected @NotNull ParkourTeam createTeam(Team team, ParkourTeam.QuitData quitData) {
        return new ParkourTeam(team, quitData.getScore());
    }
    
    @Override
    protected @NotNull ParkourTeam.QuitData getQuitData(ParkourTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ParkourParticipant participant, ParkourTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ParkourTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("timer", ""),
                new KeyLine("ending", "")
        );
    }
    
    public void updateCheckpointSidebar(ParkourParticipant participant) {
        int lastCheckpoint = config.getPuzzlesSize() - 1;
        sidebar.updateLine(participant.getUniqueId(), "checkpoint",
                Component.empty()
                        .append(Component.text(participant.getCurrentPuzzle()))
                        .append(Component.text("/"))
                        .append(Component.text(lastCheckpoint)));
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("timer", ""),
                new KeyLine("checkpoint", Component.empty()
                        .append(Component.text("0/"))
                        .append(Component.text(config.getPuzzlesSize() - 1))),
                new KeyLine("ending", "")
        );
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ParkourParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        /*
         * Check if they're holding the chat toggle item (Green Dye)
         */
        if (item == null || !isNotificationToggleItem(item)) {
            return;
        }
        
        /*
         * Check if chat toggle is enabled
         */
        if (!config.isChatToggleEnabled()) {
            return;
        }
        /*
         * Only Participants can use this item.
         */
        if (!getParticipants().containsKey(player.getUniqueId())) {
            return;
        }
        
        /*
         * Check cooldown
         */
        long now = System.currentTimeMillis();
        long lastToggle = participant.getChatToggleCooldown();
        
        if (now - lastToggle < config.getChatToggleCooldown() * 500) {
            /*
             * Still on cooldown
             */
            return;
        }
        
        /*
         * Prevent other interactions
         */
        event.setCancelled(true);
        
        /*
         * Cycle to next mode: ALL -> TEAM -> DISABLED -> ALL
         */
        NotificationMode newMode = getNextNotificationMode(participant.getNotificationMode());
        participant.setNotificationMode(newMode);
        
        /*
         * Update cooldown
         */
        participant.setChatToggleCooldown(now);
        
        /*
         * Update the item in their hand with new lore
         */
        updateNotificationToggleItem(player, newMode);
        
        /*
         * Send feedback message
         */
        String status = getNotificationModeDisplayName(newMode);
        NamedTextColor color = getNotificationModeColor(newMode);
        player.sendMessage(Component.text("Checkpoint notifications: " + status).color(color));
    }
    
    /**
     * Method to check if a player should see a specific checkpoint notification
     */
    public boolean shouldShowCheckpointNotification(ParkourParticipant viewer, ParkourParticipant achiever) {
        /*
         * Always show your own checkpoints
         */
        if (viewer.equals(achiever)) {
            return true;
        }
        
        return switch (viewer.getNotificationMode()) {
            case ALL -> true; // Show everyone's checkpoints
            case TEAM -> viewer.sameTeam(achiever); // Only show teammate checkpoints
            default -> false; // Only show your own checkpoints
        };
    }
    
    /**
     * New method to send messages only to players who want to see this specific checkpoint notification
     */
    public void messageParticipantsWithNotifications(Component message, ParkourParticipant achiever) {
        for (ParkourParticipant viewer : participants.values()) {
            if (shouldShowCheckpointNotification(viewer, achiever)) {
                viewer.sendMessage(message);
            }
        }
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isNotificationToggleItem(ItemStack item) {
        ItemStack configItem = config.getChatToggleItem();
        
        /*
         * Check material
         */
        if (item.getType() != configItem.getType()) {
            return false;
        }
        
        /*
         * Check display name
         */
        if (!item.hasItemMeta() || !configItem.hasItemMeta()) {
            return item.hasItemMeta() == configItem.hasItemMeta();
        }
        
        Component itemName = item.getItemMeta().displayName();
        Component configName = configItem.getItemMeta().displayName();
        
        return Objects.equals(itemName, configName);
    }
    
    private NotificationMode getNextNotificationMode(NotificationMode current) {
        return switch (current) {
            case ALL -> NotificationMode.TEAM;
            case TEAM -> NotificationMode.DISABLED;
            default -> NotificationMode.ALL;
        };
    }
    
    private void updateNotificationToggleItem(Player player, NotificationMode mode) {
        /*
         * Check main hand first
         */
        ItemStack item = player.getInventory().getItemInMainHand();
        boolean isMainHand = true;
        
        if (!isNotificationToggleItem(item)) {
            /*
             * Check offhand
             */
            item = player.getInventory().getItemInOffHand();
            isMainHand = false;
            if (!isNotificationToggleItem(item)) {
                return;
            }
        }
        
        List<Component> newLore = getNotificationToggleLore(mode);
        
        item.editMeta(meta -> {
            meta.lore(newLore);
        });
        
        /*
         * Update the item in inventory
         */
        if (isMainHand) {
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInOffHand(item);
        }
    }
    
    private List<Component> getNotificationToggleLore(NotificationMode mode) {
        return switch (mode) {
            case ALL -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("All Players").color(NamedTextColor.GREEN)),
                    Component.text("You see when anyone").color(NamedTextColor.GRAY),
                    Component.text("reaches checkpoints").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            case TEAM -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("Team Only").color(NamedTextColor.BLUE)),
                    Component.text("You see when you or your").color(NamedTextColor.GRAY),
                    Component.text("teammates reach checkpoints").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            case DISABLED -> List.of(
                    Component.text("Checkpoint Notifications: ").color(NamedTextColor.GRAY)
                            .append(Component.text("Self Only").color(NamedTextColor.RED)),
                    Component.text("You only see your own").color(NamedTextColor.GRAY),
                    Component.text("checkpoint progress").color(NamedTextColor.GRAY),
                    Component.text("Right click to change").color(NamedTextColor.YELLOW)
            );
            default -> List.of(Component.text("Right click to change").color(NamedTextColor.YELLOW));
        };
    }
    
    private String getNotificationModeDisplayName(NotificationMode mode) {
        return switch (mode) {
            case ALL -> "All Players";
            case TEAM -> "Team Only";
            case DISABLED -> "Self Only";
            default -> "All Players";
        };
    }
    
    private NamedTextColor getNotificationModeColor(NotificationMode mode) {
        return switch (mode) {
            case ALL -> NamedTextColor.GREEN;
            case TEAM -> NamedTextColor.BLUE;
            case DISABLED -> NamedTextColor.RED;
            default -> NamedTextColor.GREEN;
        };
    }
}