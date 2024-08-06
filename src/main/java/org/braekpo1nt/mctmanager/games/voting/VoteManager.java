package org.braekpo1nt.mctmanager.games.voting;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;
import java.util.function.Consumer;

public class VoteManager implements Listener {
    
    private int voteCountDownDuration;
    private final Component TITLE = Component.text("Vote for a Game");
    
    private final GameManager gameManager;
    private Sidebar sidebar;
    private Sidebar adminSidebar;
    private final Map<UUID, GameType> votes = new HashMap<>();
    private final Main plugin;
    private final List<Player> voters = new ArrayList<>();
    private final List<Player> admins = new ArrayList<>();
    private boolean voting = false;
    private final Component NETHER_STAR_NAME = Component.text("Vote");
    private final ItemStack NETHER_STAR;
    private List<GameType> votingPool = new ArrayList<>();
    private Consumer<GameType> executeMethod;
    private boolean paused = false;
    private final TimerManager timerManager;
    
    public VoteManager(GameManager gameManager, Main plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.timerManager = new TimerManager(plugin);
        this.NETHER_STAR = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = this.NETHER_STAR.getItemMeta();
        netherStarMeta.displayName(NETHER_STAR_NAME);
        this.NETHER_STAR.setItemMeta(netherStarMeta);
    }
    
    /**
     *Starts a voting phase with the given list of participants using the given voting pool
     * @param newParticipants The participants who should vote
     * @param votingPool The games to vote between
     * @param duration how long (in seconds) the vote should last
     * @param executeMethod the method to execute when the voting is over (either because the duration
     *                      is up or all voters have voted). It will be passed the voted for
     *                      GameType.
     */
    public void startVote(List<Player> newParticipants, List<GameType> votingPool, int duration, Consumer<GameType> executeMethod, List<Player> newAdmins) {
        this.executeMethod = executeMethod;
        this.voteCountDownDuration = duration;
        voting = true;
        paused = false;
        votes.clear();
        voters.clear();
        this.votingPool = votingPool;
        sidebar = gameManager.getSidebarFactory().createSidebar();
        adminSidebar = gameManager.getSidebarFactory().createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        gameManager.getTimerManager().register(timerManager);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        startAdmins(newAdmins);
        startVoteCountDown();
    }
    
    private void startAdmins(List<Player> newAdmins) {
        this.admins.clear();
        for (Player admin : newAdmins) {
            initializeAdmin(admin);
        }
        initializeAdminSidebar();
    }
    
    private void initializeAdmin(Player admin) {
        this.admins.add(admin);
        adminSidebar.addPlayer(admin);
    }
    
    public void onAdminJoin(Player admin) {
        if (!voting) {
            return;
        }
        initializeAdmin(admin);
    }
    
    public void onAdminQuit(Player admin) {
        if (!voting) {
            return;
        }
        admins.remove(admin);
        adminSidebar.removePlayer(admin);
    }
    
    private void initializeParticipant(Player voter) {
        if (!voting) {
            return;
        }
        this.voters.add(voter);
        sidebar.addPlayer(voter);
        if (paused) {
            return;
        }
        showVoteGui(voter);
        voter.sendMessage(Component.text("Vote for the game you want to play")
                .color(NamedTextColor.GREEN));
    }
    
    public void onParticipantJoin(Player voter) {
        if (!voting) {
            return;
        }
        initializeParticipant(voter);
    }
    
    public void onParticipantQuit(Player voter) {
        resetParticipant(voter);
        voters.remove(voter);
        sidebar.removePlayer(voter);
        votes.remove(voter.getUniqueId());
    }
    
    private void startVoteCountDown() {
        timerManager.start(Timer.builder()
                .duration(voteCountDownDuration)
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Voting: "))
                .onCompletion(this::executeVote)
                .onTogglePause((paused) -> {
                    if (paused) {
                        pauseVote();
                    } else {
                        resumeVote();
                    }
                })
                .build());
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLine("timer", "");
    }
    
    private void clearAdminSidebar() {
        adminSidebar.removeAllPlayers();
        adminSidebar.deleteAllLines();
        adminSidebar = null;
    }
    
    private void initializeSidebar() {
        sidebar.addLine("timer", "");
    }
    
    private void clearSidebar() {
        sidebar.removeAllPlayers();
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
    @EventHandler
    private void clickVoteInventory(InventoryClickEvent event) {
        if (!voting) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        if (!voters.contains(participant)) {
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
        if (!voting) {
            return;
        }
        Player participant = event.getPlayer();
        if (!voters.contains(participant)) {
            return;
        }
        event.setCancelled(true);
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
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!voting) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player voter)) {
            return;
        }
        if (!voters.contains(voter)) {
            return;
        }
        Bukkit.getLogger().info("VoteManager");
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
        timerManager.pause();
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
        timerManager.resume();
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
            resetParticipant(voter);
        }
        clearSidebar();
        clearAdminSidebar();
        messageAllVoters(Component.text("Cancelling vote"));
        HandlerList.unregisterAll(this);
        votes.clear();
        voters.clear();
        admins.clear();
    }
    
    private static void resetParticipant(Player voter) {
        voter.closeInventory();
        voter.getInventory().clear();
    }
    
    private void executeVote() {
        voting = false;
        paused = false;
        GameType gameType = getVotedForGame();
        Audience.audience(voters).showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text(gameType.getTitle()))
                        .color(NamedTextColor.BLUE),
                Component.empty()
        ));
        cancelAllTasks();
        for (Player voter : voters) {
            resetParticipant(voter);
        }
        HandlerList.unregisterAll(this);
        votes.clear();
        voters.clear();
        clearSidebar();
        admins.clear();
        clearAdminSidebar();
        executeMethod.accept(gameType);
    }
    
    private void cancelAllTasks() {
        timerManager.cancel();
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
        if (!voters.contains(participant)) {
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

        ItemStack survivalGames = new ItemStack(Material.IRON_SWORD);
        ItemMeta survivalGamesMeta = survivalGames.getItemMeta();
        survivalGamesMeta.displayName(Component.text("Survival Games"));
        survivalGamesMeta.lore(List.of(
                Component.text("A fighting game")
        ));
        survivalGames.setItemMeta(survivalGamesMeta);

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
        votingItems.put(GameType.SURVIVAL_GAMES, survivalGames);
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
