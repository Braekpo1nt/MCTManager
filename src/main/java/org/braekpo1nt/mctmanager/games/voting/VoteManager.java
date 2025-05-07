package org.braekpo1nt.mctmanager.games.voting;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;
import java.util.function.BiConsumer;

public class VoteManager implements Listener {
    
    private final Component TITLE = Component.text("Vote for a Game");
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    
    private final Map<UUID, GameType> votes = new HashMap<>();
    private final Map<UUID, Participant> voters = new HashMap<>();
    private final Map<UUID, ChestGui> guis;
    private final ItemStack NETHER_STAR;
    private final List<GameType> votingPool;
    private final Collection<GuiItem> guiItems;
    private final BiConsumer<GameType, String> executeMethod;
    
    private boolean paused;
    
    /**
     * @param plugin used for creating inventories
     * @param executeMethod the method to execute when the voting is over (either because the duration
     *                      is up or all voters have voted). It will be passed the voted for
     *                      GameType.
     * @param votingPool The games to vote between
     * @param newParticipants The participants who should vote
     */
    public VoteManager(
            Main plugin, 
            BiConsumer<GameType, String> executeMethod,
            List<GameType> votingPool, 
            Collection<Participant> newParticipants) {
        this.NETHER_STAR = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = this.NETHER_STAR.getItemMeta();
        netherStarMeta.displayName(NETHER_STAR_NAME);
        this.NETHER_STAR.setItemMeta(netherStarMeta);
        this.executeMethod = executeMethod;
        paused = false;
        votes.clear();
        voters.clear();
        this.votingPool = votingPool;
        this.guiItems = createGuiItems();
        this.guis = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Participant participant : newParticipants) {
            initializeParticipant(participant);
        }
    }
    
    private void initializeParticipant(Participant voter) {
        this.voters.put(voter.getUniqueId(), voter);
        ChestGui gui = createGui();
        this.guis.put(voter.getUniqueId(), gui);
        if (paused) {
            return;
        }
        showVoteGui(voter);
        voter.sendMessage(Component.text("Vote for the game you want to play")
                .color(NamedTextColor.GREEN));
    }
    
    /**
     * Mark a vote and close the inventory
     * @param voter the one who voted
     * @param vote the game they voted for
     */
    private void vote(HumanEntity voter, GameType vote) {
        votes.put(voter.getUniqueId(), vote);
        guis.get(voter.getUniqueId()).setOnClose(event -> {});
        voter.closeInventory();
        voter.sendMessage(Component.empty()
                .append(Component.text("Voted for "))
                .append(Component.text(vote.getTitle()))
                .color(NamedTextColor.GREEN));
    }
    
    /**
     * @return a list of GuiItems matching the votingPool
     */
    private Collection<GuiItem> createGuiItems() {
        Collection<GuiItem> items = new ArrayList<>(votingPool.size());
        
        if (votingPool.contains(GameType.FOOT_RACE)) {
            ItemStack footRace = new ItemStack(Material.FEATHER);
            ItemMeta meta = footRace.getItemMeta();
            meta.displayName(Component.text("Foot Race"));
            meta.lore(List.of(
                    Component.text("A racing game")
            ));
            footRace.setItemMeta(meta);
            items.add(new GuiItem(footRace, 
                    event -> vote(event.getWhoClicked(), GameType.FOOT_RACE)));
        }
        
        if (votingPool.contains(GameType.SURVIVAL_GAMES)) {
            ItemStack survivalGames = new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = survivalGames.getItemMeta();
            meta.displayName(Component.text("Survival Games"));
            meta.lore(List.of(
                    Component.text("A fighting game")
            ));
            survivalGames.setItemMeta(meta);
            items.add(new GuiItem(survivalGames,
                    event -> vote(event.getWhoClicked(), GameType.SURVIVAL_GAMES)));
        }
        
        if (votingPool.contains(GameType.CAPTURE_THE_FLAG)) {
            ItemStack captureTheFlag = new ItemStack(Material.GRAY_BANNER);
            ItemMeta meta = captureTheFlag.getItemMeta();
            meta.displayName(Component.text("Capture the Flag"));
            meta.lore(List.of(
                    Component.text("A team capture the flag game")
            ));
            captureTheFlag.setItemMeta(meta);
            items.add(new GuiItem(captureTheFlag,
                    event -> vote(event.getWhoClicked(), GameType.CAPTURE_THE_FLAG)));
        }
        
        if (votingPool.contains(GameType.CLOCKWORK)) {
            ItemStack clockwork = new ItemStack(Material.CLOCK);
            ItemMeta meta = clockwork.getItemMeta();
            meta.displayName(Component.text("Clockwork"));
            meta.lore(List.of(
                    Component.text("A time-sensitive game")
            ));
            clockwork.setItemMeta(meta);
            items.add(new GuiItem(clockwork,
                    event -> vote(event.getWhoClicked(), GameType.CLOCKWORK)));
        }
        
        if (votingPool.contains(GameType.PARKOUR_PATHWAY)) {
            ItemStack parkourPathway = new ItemStack(Material.LEATHER_BOOTS);
            ItemMeta meta = parkourPathway.getItemMeta();
            meta.displayName(Component.text("Parkour Pathway"));
            meta.lore(List.of(
                    Component.text("A jumping game")
            ));
            LeatherArmorMeta parkourPathwayLeatherArmorMeta = ((LeatherArmorMeta) meta);
            parkourPathwayLeatherArmorMeta.setColor(Color.WHITE);
            parkourPathway.setItemMeta(meta);
            items.add(new GuiItem(parkourPathway,
                    event -> vote(event.getWhoClicked(), GameType.PARKOUR_PATHWAY)));
        }
        
        if (votingPool.contains(GameType.SPLEEF)) {
            ItemStack spleef = new ItemStack(Material.DIAMOND_SHOVEL);
            ItemMeta meta = spleef.getItemMeta();
            meta.displayName(Component.text("Spleef"));
            meta.lore(List.of(
                    Component.text("A falling game")
            ));
            spleef.setItemMeta(meta);
            items.add(new GuiItem(spleef,
                    event -> vote(event.getWhoClicked(), GameType.SPLEEF)));
        }
        
        if (votingPool.contains(GameType.FARM_RUSH)) {
            ItemStack farmRush = new ItemStack(Material.STONE_HOE);
            ItemMeta meta = farmRush.getItemMeta();
            meta.displayName(Component.text("Farm Rush"));
            meta.lore(List.of(
                    Component.text("A farming game")
            ));
            farmRush.setItemMeta(meta);
            items.add(new GuiItem(farmRush,
                    event -> vote(event.getWhoClicked(), GameType.FARM_RUSH)));
        }
        
        return items;
    }
    
    private ChestGui createGui() {
        ChestGui gui = new ChestGui(1, ComponentHolder.of(Component.empty()
                .append(Component.text("Vote"))));
        OutlinePane pane = new OutlinePane(0, 0, 9, 1);
        for (GuiItem item : guiItems) {
            pane.addItem(item);
        }
        gui.addPane(pane);
        gui.update();
        gui.setOnClose(event -> {
            if (!votes.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(Component.empty()
                        .append(Component.text("You didn't vote. Use the nether star to vote."))
                        .color(NamedTextColor.DARK_RED));
            }
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
            netherStar.editMeta(meta -> meta.displayName(NETHER_STAR_NAME));
            event.getPlayer().getInventory().addItem(netherStar);
        });
        return gui;
    }
    
    public void onParticipantJoin(Participant voter) {
        initializeParticipant(voter);
    }
    
    public void onParticipantQuit(Participant voter) {
        resetParticipant(voter);
        voters.remove(voter.getUniqueId());
        votes.remove(voter.getUniqueId());
    }
    
    @EventHandler
    private void clickVoteInventory(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Participant participant = voters.get(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (participantVoted(participant)) {
            participant.sendMessage(Component.text("You already voted.")
                    .color(NamedTextColor.GREEN));
            return;
        }
        Material clickedItem = event.getCurrentItem().getType();
        GameType votedForType;
        switch (clickedItem) {
            case FEATHER -> {
                votedForType = GameType.FOOT_RACE;
            }
            case IRON_SWORD -> {
                votedForType = GameType.SURVIVAL_GAMES;
            }
            case GRAY_BANNER -> {
                votedForType = GameType.CAPTURE_THE_FLAG;
            }
            case DIAMOND_SHOVEL -> {
                votedForType = GameType.SPLEEF;
            }
            case LEATHER_BOOTS -> {
                votedForType = GameType.PARKOUR_PATHWAY;
            }
            case CLOCK -> {
                votedForType = GameType.CLOCKWORK;
            }
            case STONE_HOE -> {
                votedForType = GameType.FARM_RUSH;
            }
            default -> {
                return;
            }
        }
        votes.put(participant.getUniqueId(), votedForType);
        participant.sendMessage(Component.empty()
                .append(Component.text("Voted for "))
                .append(Component.text(votedForType.getTitle()))
                .color(NamedTextColor.GREEN));
        participant.closeInventory();
    }
    
    /**
     * Stop players from dropping items
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Participant participant = voters.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Checks if the participant submitted a vote already
     * @param participant the participant to check
     * @return True of the participant voted, false if they haven't yet
     */
    private boolean participantVoted(Participant participant) {
        return votes.containsKey(participant.getUniqueId());
    }
    
    @EventHandler
    private void onCloseMenu(InventoryCloseEvent event) {
        if (paused) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Participant participant = voters.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (allPlayersHaveVoted()) {
            executeVote();
        } else {
            if (participantVoted(participant)) {
                return;
            }
            participant.getInventory().addItem(NETHER_STAR);
            participant.sendMessage(Component.text("You didn't vote for a game. Use the nether star to vote.")
                    .color(NamedTextColor.YELLOW));
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_DAMAGE_CAUSES.contains(event.getCause())) {
            return;
        }
        Participant participant = voters.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "VoteManager.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    /**
     * Pauses the vote. If no vote is running, nothing happens. Removes UI from players,
     * removes nether stars from players, pauses the timer, and retains player votes for resuming.
     */
    public void pauseVote() {
        if (paused) {
            return;
        }
        paused = true;
        for (Participant voter : voters.values()) {
            voter.closeInventory();
            voter.getInventory().remove(NETHER_STAR);
        }
        messageAllVoters(Component.text("Voting is paused.")
                .color(NamedTextColor.YELLOW));
    }
    
    /**
     * Resumes a paused vote. If the vote is not currently paused, nothing happens. Gives the UI
     * back to players who have not yet voted, resumes the timer, and retains any votes that
     * occurred before the pause.
     */
    public void resumeVote() {
        if (!paused) {
            return;
        }
        paused = false;
        for (Participant voter : voters.values()) {
            if (!participantVoted(voter)) {
                showVoteGui(voter);
            }
        }
    }
    
    /**
     * Cancel the vote if a vote is in progress
     */
    public void cancelVote() {
        HandlerList.unregisterAll(this);
        paused = false;
        for (Participant voter : voters.values()) {
            resetParticipant(voter);
        }
        messageAllVoters(Component.text("Cancelling vote"));
        votes.clear();
        voters.clear();
        guis.clear();
        guiItems.clear();
    }
    
    private void resetParticipant(Participant voter) {
        voter.closeInventory();
        ChestGui removed = guis.remove(voter.getUniqueId());
        removed.getInventory().close();
    }
    
    public void executeVote() {
        HandlerList.unregisterAll(this);
        paused = false;
        GameType gameType = getVotedForGame();
        Audience.audience(
                voters.values()
        ).showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text(gameType.getTitle()))
                        .color(NamedTextColor.BLUE),
                Component.empty()
        ));
        for (Participant voter : voters.values()) {
            resetParticipant(voter);
        }
        votes.clear();
        voters.clear();
        guis.clear();
        guiItems.clear();
        executeMethod.accept(gameType, "default.json");
    }
    
    @EventHandler
    public void interactWithNetherStar(PlayerInteractEvent event) {
        Participant participant = voters.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (paused) {
            participant.sendMessage(Component.text("Voting is paused.")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        ItemStack netherStar = event.getItem();
        if (netherStar == null ||
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        if (participantVoted(participant)) {
            participant.sendMessage(Component.text("You already voted.")
                    .color(NamedTextColor.GREEN));
            return;
        }
        event.setCancelled(true);
        participant.getInventory().remove(netherStar);
        showVoteGui(participant);
    }
    
    private GameType getVotedForGame() {
        Random random = new Random();
        
        if (votes.isEmpty()) {
            int randomGameIndex = random.nextInt(votingPool.size());
            return votingPool.get(randomGameIndex);
        }
        
        Map<GameType, Integer> voteCount = new HashMap<>();
        // Count the number of occurrences of each string in the list
        for (GameType vote : votes.values()) {
            int count = voteCount.getOrDefault(vote, 0);
            voteCount.put(vote, count + 1);
        }
        
        // Find the maximum number of occurrences
        int maxCount = 0;
        for (Integer count : voteCount.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }
        
        // Get all strings with the maximum number of occurrences
        List<GameType> winners = new ArrayList<>();
        for (Map.Entry<GameType, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() == maxCount) {
                winners.add(entry.getKey());
            }
        }
        
        // Randomly select a winner from the list of strings with the maximum number of occurrences
        int index = random.nextInt(winners.size());
        return winners.get(index);
    }
    
    private boolean allPlayersHaveVoted() {
        for (Participant participant : voters.values()) {
            if (!participantVoted(participant)) {
                return false;
            }
        }
        return true;
    }
    
    private void showVoteGui(Participant participant) {
        ChestGui gui = this.guis.get(participant.getUniqueId());
        gui.show(participant.getPlayer());
    }
    
    public static List<GameType> votableGames() {
        return List.of(
                GameType.FOOT_RACE,
                GameType.SURVIVAL_GAMES,
                GameType.CAPTURE_THE_FLAG,
                GameType.SPLEEF,
                GameType.PARKOUR_PATHWAY,
                GameType.CLOCKWORK,
                GameType.FARM_RUSH
        );
    }
    
    private void messageAllVoters(Component message) {
        Audience.audience(voters.values()).sendMessage(message);
    }
}
