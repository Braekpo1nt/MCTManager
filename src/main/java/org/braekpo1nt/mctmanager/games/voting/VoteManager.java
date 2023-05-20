package org.braekpo1nt.mctmanager.games.voting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class VoteManager implements Listener {
    
    private final Component TITLE = Component.text("Vote for a Game");
    
    private final GameManager gameManager;
    private final Map<UUID, MCTGames> votes = new HashMap<>();
    private final Main plugin;
    private final List<Player> voters = new ArrayList<>();
    private boolean voting = false;
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private int voteCountDownTaskId;
    private List<MCTGames> votingPool = new ArrayList<>();
    private int executeVoteCountdownTaskId;
    
    public VoteManager(GameManager gameManager, Main plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }
    
    /**
     * Starts a voting phase with the given list of participants using the given voting pool
     * @param participants The participants who should vote
     * @param votingPool The games to vote between
     */
    public void startVote(List<Player> participants, List<MCTGames> votingPool) {
        voting = true;
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
            private int count = 60;
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllVoters(Component.text("Voting is over"));
                    startExecuteVoteCountdown();
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player participant : voters) {
                    updateVoteTimerFastBoard(participant, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void updateVoteTimerFastBoard(Player participant, String timeString) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                "",
                "Voting:",
                timeString
        );
    }
    
    private void updateExecuteVoteFastBoard(Player voter, String gameTitle, String timeString) {
        gameManager.getFastBoardManager().updateLines(
                voter.getUniqueId(),
                "",
                gameTitle,
                timeString
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
        if (participantAbstained(participant)) {
            votes.remove(participant.getUniqueId());
        }
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case FEATHER:
                votes.put(participant.getUniqueId(), MCTGames.FOOT_RACE);
                participant.sendMessage(Component.text("Voted for Foot Race")
                        .color(NamedTextColor.GREEN));
                break;
            case IRON_SWORD:
                votes.put(participant.getUniqueId(), MCTGames.MECHA);
                participant.sendMessage(Component.text("Voted for MECHA")
                        .color(NamedTextColor.GREEN));
                break;
            case GRAY_BANNER:
                votes.put(participant.getUniqueId(), MCTGames.CAPTURE_THE_FLAG);
                participant.sendMessage(Component.text("Voted for Capture the Flag")
                        .color(NamedTextColor.GREEN));
                break;
            case DIAMOND_SHOVEL:
                votes.put(participant.getUniqueId(), MCTGames.SPLEEF);
                participant.sendMessage(Component.text("Voted for Spleef")
                        .color(NamedTextColor.GREEN));
                break;
            case LEATHER_BOOTS:
                votes.put(participant.getUniqueId(), MCTGames.PARKOUR_PATHWAY);
                participant.sendMessage(Component.text("Voted for Parkour Pathway")
                        .color(NamedTextColor.GREEN));
                break;
            default:
                return;
        }
        participant.closeInventory();
    }

    /**
     * Checks if the participant submitted a vote already
     * @param participant the participant to check
     * @return True of the participant voted, false if they haven't yet
     * or if they have abstained (see {@link VoteManager#participantAbstained(Player)})
     */
    private boolean participantVoted(Player participant) {
        if (!votes.containsKey(participant.getUniqueId())) {
            return false;
        }
        return votes.get(participant.getUniqueId()) != null;
    }

    /**
     * Checks if the participant abstained from voting
     * @param participant the participant
     * @return True if the participant abstained from voting,
     * false if they voted or didn't vote or abstain yet (i.e. still
     * in the voting gui)
     */
    private boolean participantAbstained(Player participant) {
        if (!votes.containsKey(participant.getUniqueId())) {
            return false;
        }
        return votes.get(participant.getUniqueId()) == null;
    }
    
    @EventHandler
    private void onCloseMenu(InventoryCloseEvent event) {
        if (!voting) {
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
            startExecuteVoteCountdown();
        } else {
            if (participantVoted(participant)) {
                return;
            }
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
            ItemMeta netherStarMeta = netherStar.getItemMeta();
            netherStarMeta.displayName(NETHER_STAR_NAME);
            netherStar.setItemMeta(netherStarMeta);
            participant.getInventory().addItem(netherStar);
            participant.sendMessage(Component.text("You didn't vote for a game. Use the nether star to vote.")
                    .color(NamedTextColor.DARK_RED));
            votes.put(participant.getUniqueId(), null);
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
    
    private void startExecuteVoteCountdown() {
        for (Player voter : voters) {
            voter.closeInventory();
            voter.getInventory().clear();
            hideFastBoard(voter);
        }
        MCTGames votedForGame = getVotedForGame();
        String gameTitle = ChatColor.BLUE+""+ChatColor.BOLD+MCTGames.getTitle(votedForGame);
        messageAllVoters(Component.empty()
                .append(Component.text(gameTitle)
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" was selected"))
                .color(NamedTextColor.GREEN));
        this.executeVoteCountdownTaskId = new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    executeVote(votedForGame);
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                for (Player voter : voters) {
                    updateExecuteVoteFastBoard(voter, gameTitle, timeString);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void executeVote(MCTGames votedForGame) {
        voting = false;
        cancelAllTasks();
        HandlerList.unregisterAll(this);
        votes.clear();
        voters.clear();
        gameManager.startGame(votedForGame);
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(voteCountDownTaskId);
        Bukkit.getScheduler().cancelTask(executeVoteCountdownTaskId);
    }
    
    @EventHandler
    public void interactWithNetherStar(PlayerInteractEvent event) {
        if (!voting) {
            return;
        }
        Player participant = event.getPlayer();
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
            return;
        }
        event.setCancelled(true);
        participant.getInventory().remove(netherStar);
        showVoteGui(participant);
    }
    
    private MCTGames getVotedForGame() {
        Random random = new Random();
        
        if (votes.isEmpty()) {
            int randomGameIndex = random.nextInt(votingPool.size());
            return votingPool.get(randomGameIndex);
        }
    
        Map<MCTGames, Integer> voteCount = new HashMap<>();
        // Count the number of occurrences of each string in the list
        for (MCTGames vote : votes.values()) {
            if (vote != null) {
                int count = voteCount.getOrDefault(vote, 0);
                voteCount.put(vote, count + 1);
            }
        }
        
        if (voteCount.isEmpty()) {
            int randomGameIndex = random.nextInt(votingPool.size());
            return votingPool.get(randomGameIndex);
        }
        
        // Find the maximum number of occurrences
        int maxCount = 0;
        for (Integer count : voteCount.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }
    
        // Get all strings with the maximum number of occurrences
        List<MCTGames> winners = new ArrayList<>();
        for (Map.Entry<MCTGames, Integer> entry : voteCount.entrySet()) {
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
            if (!participantVoted(participant) && !participantAbstained(participant)) {
                return false;
            }
        }
        return true;
    }
    
    private void showVoteGui(Player participant) {
        ItemStack footRace = new ItemStack(Material.FEATHER);

        ItemMeta footRaceMeta = footRace.getItemMeta();
        footRaceMeta.displayName(Component.text("Foot Race"));
        footRaceMeta.lore(Arrays.asList(
                Component.text("A racing game")
        ));
        footRace.setItemMeta(footRaceMeta);

        ItemStack mecha = new ItemStack(Material.IRON_SWORD);
        ItemMeta mechaMeta = mecha.getItemMeta();
        mechaMeta.displayName(Component.text("MECHA"));
        mechaMeta.lore(Arrays.asList(
                Component.text("A fighting game")
        ));
        mecha.setItemMeta(mechaMeta);

        ItemStack captureTheFlag = new ItemStack(Material.GRAY_BANNER);
        ItemMeta captureTheFlagMeta = captureTheFlag.getItemMeta();
        captureTheFlagMeta.displayName(Component.text("Capture the Flag"));
        captureTheFlagMeta.lore(Arrays.asList(
                Component.text("A team capture the flag game")
        ));
        captureTheFlag.setItemMeta(captureTheFlagMeta);

        ItemStack parkourPathway = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta parkourPathwayMeta = parkourPathway.getItemMeta();
        parkourPathwayMeta.displayName(Component.text("Parkour Pathway"));
        parkourPathwayMeta.lore(Arrays.asList(
                Component.text("A jumping game")
        ));
        LeatherArmorMeta parkourPathwayLeatherArmorMeta = ((LeatherArmorMeta) parkourPathwayMeta);
        parkourPathwayLeatherArmorMeta.setColor(Color.WHITE);
        parkourPathway.setItemMeta(parkourPathwayMeta);

        ItemStack spleef = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta spleefMeta = spleef.getItemMeta();
        spleefMeta.displayName(Component.text("Spleef"));
        spleefMeta.lore(Arrays.asList(
                Component.text("A falling game")
        ));
        spleef.setItemMeta(spleefMeta);
    
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        Map<MCTGames, ItemStack> votingItems = new HashMap<>();
        votingItems.put(MCTGames.FOOT_RACE, footRace);
        votingItems.put(MCTGames.MECHA, mecha);
        votingItems.put(MCTGames.CAPTURE_THE_FLAG, captureTheFlag);
        votingItems.put(MCTGames.SPLEEF, spleef);
        votingItems.put(MCTGames.PARKOUR_PATHWAY, parkourPathway);
        
        for (MCTGames mctGame : votingPool) {
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
