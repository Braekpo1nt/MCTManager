package org.braekpo1nt.mctmanager.games.voting;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VoteManager implements Listener {
    
    private final Component TITLE = Component.text("Vote for a Game");
    
    private final GameManager gameManager;
    private final Map<UUID, String> votes = new HashMap<>();
    private final Main plugin;
    private List<Player> voters = new ArrayList<>();
    private boolean voting = false;
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private int voteCountDownTaskId;
    
    public VoteManager(GameManager gameManager, Main plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void startVote(List<Player> participants) {
        voting = true;
        votes.clear();
        voters.clear();
        for (Player participant : participants) {
            showVoteGui(participant);
            this.voters.add(participant);
            participant.sendMessage(Component.text("Vote for the game you want to play"));
        }
        startVoteCountDown();
    }
    
    private void startVoteCountDown() {
        this.voteCountDownTaskId = new BukkitRunnable() {
            private int count = 30;
            @Override
            public void run() {
                if (count <= 0) {
                    messageAllVoters(Component.text("Time to vote has run out"));
                } else {
                    for (Player participant : voters) {
                        String timeString = TimeStringUtils.getTimeString(count);
                        updateVoteTimerFastBoard(participant, timeString);
                    }
                }
                if (count <= 0) {
                    executeVote();
                    this.cancel();
                    return;
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
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    
    
    @EventHandler
    private void clickEvent(InventoryClickEvent event) {
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
            String vote = votes.get(participant.getUniqueId());
            participant.sendMessage(Component.text("You already voted for ")
                    .append(Component.text(vote)));
            return;
        }
        if (participantAbstained(participant)) {
            votes.remove(participant.getUniqueId());
        }
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case FEATHER:
                votes.put(participant.getUniqueId(), "foot-race");
                participant.sendMessage(Component.text("Voted for Foot Race"));
                break;
            case IRON_SWORD:
                votes.put(participant.getUniqueId(), "mecha");
                participant.sendMessage(Component.text("Voted for MECHA"));
                break;
            case GRAY_BANNER:
                votes.put(participant.getUniqueId(), "capture-the-flag");
                participant.sendMessage(Component.text("Voted for Capture the Flag"));
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
    private void menuClose(InventoryCloseEvent event) {
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
            executeVote();
        } else {
            if (participantVoted(participant)) {
                return;
            }
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
            ItemMeta netherStarMeta = netherStar.getItemMeta();
            netherStarMeta.displayName(NETHER_STAR_NAME);
            netherStar.setItemMeta(netherStarMeta);
            participant.getInventory().addItem(netherStar);
            participant.sendMessage(Component.text("You didn't vote for a game. Use the nether star to vote."));
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
        Bukkit.getScheduler().cancelTask(voteCountDownTaskId);
        for (Player voter : voters) {
            voter.closeInventory();
            voter.getInventory().clear();
            hideFastBoard(voter);
        }
        messageAllVoters(Component.text("Cancelling vote"));
        votes.clear();
        voters.clear();
    }
    
    private void executeVote() {
        voting = false;
        Bukkit.getScheduler().cancelTask(voteCountDownTaskId);
        for (Player voter : voters) {
            voter.closeInventory();
            voter.getInventory().clear();
            hideFastBoard(voter);
        }
        String game = getVotedGame();
        votes.clear();
        voters.clear();
        gameManager.startGame(game, Bukkit.getConsoleSender());
    }
    
    @EventHandler
    public void onClickNetherStar(PlayerInteractEvent event) {
        if (!voting) {
            return;
        }
        Player participant = event.getPlayer();
        if (!gameManager.isParticipant(participant.getUniqueId())) {
            return;
        }
        ItemStack item = participant.getInventory().getItemInMainHand();
        if (item.getType() != Material.NETHER_STAR) {
            return;
        }
        ItemMeta netherStarMeta = item.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !netherStarMeta.displayName().equals(NETHER_STAR_NAME)) {
            return;
        }
        if (!voters.contains(participant)) {
            voters.add(participant);
        }
        if (participantVoted(participant)) {
            String vote = votes.get(participant.getUniqueId());
            participant.sendMessage(Component.text("You already voted for ")
                    .append(Component.text(vote)));
            return;
        }
        participant.getInventory().clear();
        showVoteGui(participant);
    }
    
    private String getVotedGame() {
        Random random = new Random();
        Map<String, Integer> voteCount = new HashMap<>();
    
        if (votes.isEmpty()) {
            int randomGameIndex = random.nextInt(3);
            return Arrays.asList("foot-race", "mecha", "capture-the-flag").get(randomGameIndex);
        }
    
        // Count the number of occurrences of each string in the list
        for (String vote : votes.values()) {
            if (vote != null) {
                int count = voteCount.getOrDefault(vote, 0);
                voteCount.put(vote, count + 1);
            }
        }
    
        // Find the maximum number of occurrences
        int maxCount = 0;
        for (Integer count : voteCount.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }
    
        // Get all strings with the maximum number of occurrences
        List<String> winners = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
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
        ItemStack mecha = new ItemStack(Material.IRON_SWORD);
        ItemStack captureTheFlag = new ItemStack(Material.GRAY_BANNER);
    
        ItemMeta footRaceMeta = footRace.getItemMeta();
        footRaceMeta.displayName(Component.text("Foot Race"));
        footRaceMeta.lore(Arrays.asList(
                Component.text("A racing game")
        ));
        footRace.setItemMeta(footRaceMeta);
    
        ItemMeta mechaMeta = mecha.getItemMeta();
        mechaMeta.displayName(Component.text("MECHA"));
        mechaMeta.lore(Arrays.asList(
                Component.text("A fighting game")
        ));
        mecha.setItemMeta(mechaMeta);
    
        ItemMeta captureTheFlagMeta = captureTheFlag.getItemMeta();
        captureTheFlagMeta.displayName(Component.text("Capture the Flag"));
        captureTheFlagMeta.lore(Arrays.asList(
                Component.text("A team capture the flag game")
        ));
        captureTheFlag.setItemMeta(captureTheFlagMeta);
    
        Inventory newGui = Bukkit.createInventory(null, 9, TITLE);
        ItemStack[] contents = {footRace, mecha, captureTheFlag};
        newGui.setContents(contents);
        participant.openInventory(newGui);
    }
    
    private void messageAllVoters(Component message) {
        for (Player voter : voters) {
            voter.sendMessage(message);
        }
    }
    
}
