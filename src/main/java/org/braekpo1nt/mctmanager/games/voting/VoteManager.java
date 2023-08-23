package org.braekpo1nt.mctmanager.games.voting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class VoteManager implements Listener {
    
    private int voteCountDownDuration;
    private final Component TITLE = Component.text("Vote for a Game");
    
    private final GameManager gameManager;
    private final Map<UUID, GameType> votes = new HashMap<>();
    private final Main plugin;
    private final List<Player> voters = new ArrayList<>();
    private boolean voting = false;
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private final ItemStack NETHER_STAR;
    private int voteCountDownTaskId;
    private List<GameType> votingPool = new ArrayList<>();
    private Consumer<GameType> executeMethod;
    private boolean paused = false;
    
    public VoteManager(GameManager gameManager, Main plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.NETHER_STAR = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = this.NETHER_STAR.getItemMeta();
        netherStarMeta.displayName(NETHER_STAR_NAME);
        this.NETHER_STAR.setItemMeta(netherStarMeta);
    }
    
    /**
     *Starts a voting phase with the given list of participants using the given voting pool
     * @param participants The participants who should vote
     * @param votingPool The games to vote between
     * @param duration how long (in seconds) the vote should last
     * @param executeMethod the method to execute when the voting is over (either because the duration
     *                      is up or all voters have voted). It will be passed the voted for
     *                      GameType.
     */
    public void startVote(List<Player> participants, List<GameType> votingPool, int duration, Consumer<GameType> executeMethod) {
        this.executeMethod = executeMethod;
        this.voteCountDownDuration = duration;
        voting = true;
        paused = false;
        votes.clear();
        voters.clear();
        this.votingPool = votingPool;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : participants) {
            this.voters.add(participant);
            showVoteGui(participant);
            participant.sendMessage(Component.text("Vote for the game you want to play")
                    .color(NamedTextColor.GREEN));
        }
        startVoteCountDown();
    }
    
    private void startVoteCountDown() {
        this.voteCountDownTaskId = new BukkitRunnable() {
            private int count = voteCountDownDuration;
            @Override
            public void run() {
                if (paused) {
                    return;
                }
                if (count <= 0) {
                    messageAllVoters(Component.text("Voting is over"));
                    executeVote();
                    this.cancel();
                    return;
                }
                String timeString = "Voting: " + TimeStringUtils.getTimeString(count);
                for (Player participant : voters) {
                    updateVoteTimerFastBoard(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void updateVoteTimerFastBoard(Player participant, String timer) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                "",
                timer
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    
    @EventHandler
    private void clickVoteInventory(InventoryClickEvent event) {
        if (!voting) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (participantVoted(participant)) {
            participant.sendMessage(Component.text("You already voted.")
                    .color(NamedTextColor.GREEN));
            return;
        }
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case FEATHER:
                votes.put(participant.getUniqueId(), GameType.FOOT_RACE);
                participant.sendMessage(Component.text("Voted for Foot Race")
                        .color(NamedTextColor.GREEN));
                break;
            case IRON_SWORD:
                votes.put(participant.getUniqueId(), GameType.MECHA);
                participant.sendMessage(Component.text("Voted for MECHA")
                        .color(NamedTextColor.GREEN));
                break;
            case GRAY_BANNER:
                votes.put(participant.getUniqueId(), GameType.CAPTURE_THE_FLAG);
                participant.sendMessage(Component.text("Voted for Capture the Flag")
                        .color(NamedTextColor.GREEN));
                break;
            case DIAMOND_SHOVEL:
                votes.put(participant.getUniqueId(), GameType.SPLEEF);
                participant.sendMessage(Component.text("Voted for Spleef")
                        .color(NamedTextColor.GREEN));
                break;
            case LEATHER_BOOTS:
                votes.put(participant.getUniqueId(), GameType.PARKOUR_PATHWAY);
                participant.sendMessage(Component.text("Voted for Parkour Pathway")
                        .color(NamedTextColor.GREEN));
                break;
            case CLOCK:
                votes.put(participant.getUniqueId(), GameType.CLOCKWORK);
                participant.sendMessage(Component.text("Voted for Clockwork")
                        .color(NamedTextColor.GREEN));
            default:
                return;
        }
        participant.closeInventory();
    }
    
    /**
     * Checks if the participant submitted a vote already
     * @param participant the participant to check
     * @return True of the participant voted, false if they haven't yet
     */
    private boolean participantVoted(Player participant) {
        return votes.containsKey(participant.getUniqueId());
    }
    
    @EventHandler
    private void onCloseMenu(InventoryCloseEvent event) {
        if (!voting) {
            return;
        }
        if (paused) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Player participant = ((Player) event.getPlayer());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
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
    
    /**
     * Pauses the vote. If no vote is running, nothing happens. Removes UI from players,
     * removes nether stars from players, pauses the timer, and retains player votes for resuming.
     */
    public void pauseVote() {
        paused = true;
        for (Player voter : voters) {
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
        for (Player voter : voters) {
            if (!participantVoted(voter)) {
                showVoteGui(voter);
            }
        }
    }
    
    /**
     * Cancel the vote if a vote is in progress
     */
    public void cancelVote() {
        if (!voting) {
            return;
        }
        voting = false;
        paused = false;
        cancelAllTasks();
        for (Player voter : voters) {
            voter.closeInventory();
            voter.getInventory().clear();
            hideFastBoard(voter);
        }
        messageAllVoters(Component.text("Cancelling vote"));
        HandlerList.unregisterAll(this);
        votes.clear();
        voters.clear();
    }
    
    private void executeVote() {
        voting = false;
        paused = false;
        cancelAllTasks();
        for (Player voter : voters) {
            voter.closeInventory();
            voter.getInventory().clear();
            hideFastBoard(voter);
        }
        GameType gameType = getVotedForGame();
        HandlerList.unregisterAll(this);
        votes.clear();
        voters.clear();
//        gameManager.startGameWithDelay(gameType);
        executeMethod.accept(gameType);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(voteCountDownTaskId);
    }
    
    @EventHandler
    public void interactWithNetherStar(PlayerInteractEvent event) {
        if (!voting) {
            return;
        }
        Player participant = event.getPlayer();
        if (paused) {
            participant.sendMessage(Component.text("Voting is paused.")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        if (!gameManager.isParticipant(participant.getUniqueId())) {
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
        if (!voters.contains(participant)) {
            voters.add(participant);
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
    
    @EventHandler
    public void clickNetherStar(InventoryClickEvent event) {
        if (!voting) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack netherStar = event.getCurrentItem();
        if (netherStar == null ||
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        if (!voters.contains(participant)) {
            voters.add(participant);
        }
        if (participantVoted(participant)) {
            participant.sendMessage(Component.text("You already voted.")
                    .color(NamedTextColor.GREEN));
            event.setCancelled(true);
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
        for (Player participant : voters) {
            if (!participantVoted(participant)) {
                return false;
            }
        }
        return true;
    }
    
    private void showVoteGui(Player participant) {
        ItemStack footRace = new ItemStack(Material.FEATHER);

        ItemMeta footRaceMeta = footRace.getItemMeta();
        footRaceMeta.displayName(Component.text("Foot Race"));
        footRaceMeta.lore(List.of(
                Component.text("A racing game")
        ));
        footRace.setItemMeta(footRaceMeta);

        ItemStack mecha = new ItemStack(Material.IRON_SWORD);
        ItemMeta mechaMeta = mecha.getItemMeta();
        mechaMeta.displayName(Component.text("MECHA"));
        mechaMeta.lore(List.of(
                Component.text("A fighting game")
        ));
        mecha.setItemMeta(mechaMeta);

        ItemStack captureTheFlag = new ItemStack(Material.GRAY_BANNER);
        ItemMeta captureTheFlagMeta = captureTheFlag.getItemMeta();
        captureTheFlagMeta.displayName(Component.text("Capture the Flag"));
        captureTheFlagMeta.lore(List.of(
                Component.text("A team capture the flag game")
        ));
        captureTheFlag.setItemMeta(captureTheFlagMeta);
        
        ItemStack clockwork = new ItemStack(Material.CLOCK);
        ItemMeta clockworkMeta = clockwork.getItemMeta();
        clockworkMeta.displayName(Component.text("Clockwork"));
        clockworkMeta.lore(List.of(
                Component.text("A time-sensitive game")
        ));
        clockwork.setItemMeta(clockworkMeta);

        ItemStack parkourPathway = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta parkourPathwayMeta = parkourPathway.getItemMeta();
        parkourPathwayMeta.displayName(Component.text("Parkour Pathway"));
        parkourPathwayMeta.lore(List.of(
                Component.text("A jumping game")
        ));
        LeatherArmorMeta parkourPathwayLeatherArmorMeta = ((LeatherArmorMeta) parkourPathwayMeta);
        parkourPathwayLeatherArmorMeta.setColor(Color.WHITE);
        parkourPathway.setItemMeta(parkourPathwayMeta);

        ItemStack spleef = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta spleefMeta = spleef.getItemMeta();
        spleefMeta.displayName(Component.text("Spleef"));
        spleefMeta.lore(List.of(
                Component.text("A falling game")
        ));
        spleef.setItemMeta(spleefMeta);
        
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        Map<GameType, ItemStack> votingItems = new HashMap<>();
        votingItems.put(GameType.FOOT_RACE, footRace);
        votingItems.put(GameType.MECHA, mecha);
        votingItems.put(GameType.CAPTURE_THE_FLAG, captureTheFlag);
        votingItems.put(GameType.SPLEEF, spleef);
        votingItems.put(GameType.PARKOUR_PATHWAY, parkourPathway);
        votingItems.put(GameType.CLOCKWORK, clockwork);
        
        for (GameType mctGame : votingPool) {
            newGui.addItem(votingItems.get(mctGame));
        }
        participant.openInventory(newGui);
    }
    
    private void messageAllVoters(Component message) {
        for (Player voter : voters) {
            voter.sendMessage(message);
        }
    }
    
    public boolean isVoting() {
        return voting;
    }
    
}
