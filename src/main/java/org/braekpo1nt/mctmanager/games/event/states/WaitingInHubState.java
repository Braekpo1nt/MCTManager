package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.Tip;
import org.braekpo1nt.mctmanager.games.event.states.delay.ToColossalCombatDelay;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class WaitingInHubState implements EventState {
    
    protected final EventManager context;
    protected final GameManager gameManager;
    protected final Sidebar sidebar;
    protected final Sidebar adminSidebar;
    protected final Timer waitingInHubTimer;
    
    protected final Map<UUID, Component> playerTips = new HashMap<>();
    protected int updateTipsTaskId;
    protected int displayTipsTaskId;
    
    
    public WaitingInHubState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        gameManager.returnAllParticipantsToHub();
        double scoreMultiplier = context.matchProgressPointMultiplier();
        gameManager.messageOnlineParticipants(Component.text("Score multiplier: ")
                .append(Component.text(scoreMultiplier))
                .color(NamedTextColor.GOLD));
        waitingInHubTimer = startTimer();
        startActionBarTips();
    }
    
    protected Timer startTimer() {
        Component prefix;
        if (context.allGamesHaveBeenPlayed()) {
            prefix = Component.text("Final round: ");
        } else {
            prefix = Component.text("Vote starts in: ");
        }
        return context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getWaitingInHubDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(prefix)
                .onCompletion(() -> {
                    context.getPlugin().getServer().getScheduler().cancelTask(updateTipsTaskId);
                    context.getPlugin().getServer().getScheduler().cancelTask(displayTipsTaskId);
                    if (context.allGamesHaveBeenPlayed()) {
                        context.setState(new ToColossalCombatDelay(context));
                    } else {
                        context.setState(new VotingState(context));
                    }
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        gameManager.returnParticipantToHubInstantly(participant);
        if (sidebar != null) {
            sidebar.addPlayer(participant);
            context.updateTeamScores();
            sidebar.updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        if (sidebar != null) {
            sidebar.removePlayer(participant);
        }
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        if (adminSidebar != null) {
            adminSidebar.addPlayer(admin);
            context.updateTeamScores();
            adminSidebar.updateLine(admin.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        context.getAdmins().remove(admin);
        if (adminSidebar != null) {
            adminSidebar.removePlayer(admin);
        }
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber) {
        sender.sendMessage(Component.text("An event is already running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.WaitingInHubState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "WaitingInHubState updatePersonalScores()");
        if (context.getSidebar() == null) {
            return;
        }
        for (Participant participant : updateParticipants) {
            context.getSidebar().updateLine(participant.getUniqueId(), "personalScore",
                    Component.empty()
                            .append(Component.text("Personal: "))
                            .append(Component.text(participant.getScore()))
                            .color(NamedTextColor.GOLD));
        }
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "WaitingInHubState updateTeamScores()");
        if (context.getSidebar() == null) {
            return;
        }
        List<Team> sortedTeams = EventManager.sortTeams(updateTeams);
        if (context.getNumberOfTeams() != sortedTeams.size()) {
            EventState.reorderTeamLines(sortedTeams, context);
            return;
        }
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().updateLines(teamLines);
        if (context.getAdminSidebar() == null) {
            return;
        }
        context.getAdminSidebar().updateLines(teamLines);
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber() - 1) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("Can't set the max games for this event to less than "))
                    .append(Component.text(context.getCurrentGameNumber() - 1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber() - 1))
                    .append(Component.text(" game(s) have been played."))
                    .color(NamedTextColor.RED));
            return;
        }
        context.setMaxGames(newMaxGames);
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        gameManager.updateGameTitle();
        sender.sendMessage(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    public void startActionBarTips() {
        BukkitScheduler scheduler = context.getPlugin().getServer().getScheduler();

        // Task to compute and update tips
        updateTipsTaskId = scheduler.scheduleSyncRepeatingTask(context.getPlugin(), () -> {
            Map<String, List<Participant>> teamMapping = getTeamPlayerMapping();
            playerTips.clear();
            
            for (List<Participant> teamPlayers : teamMapping.values()) {
                List<Tip> tips = Tip.selectMultipleWeightedRandomTips(context.getConfig().getTips(), teamPlayers.size());

                for (int i = 0; i < teamPlayers.size(); i++) {
                    Participant participant = teamPlayers.get(i);
                    Component tip = tips.get(i).getBody();
                    playerTips.put(participant.getUniqueId(), tip);
                }
            }
        }, 0L, context.getConfig().getTipsDisplayTime());
        
        // Task to display the tips
        displayTipsTaskId = scheduler.scheduleSyncRepeatingTask(context.getPlugin(), () -> {
            for (Participant participant : context.getParticipants()) {
                Component text = playerTips.getOrDefault(participant.getUniqueId(), Component.empty());
                participant.sendActionBar(text);
            }
        }, 0L, 20L);
        
    }
    
    /**
     * @return mapping from online team's ids to their online members
     */
    public Map<String, List<Participant>> getTeamPlayerMapping() {
        return context.getParticipants().stream()
                .collect(Collectors.groupingBy(
                        OfflineParticipant::getTeamId,
                        Collectors.toList()
                ));
    }

    @Override
    public void cancelAllTasks() {
        context.getPlugin().getServer().getScheduler().cancelTask(updateTipsTaskId);
        context.getPlugin().getServer().getScheduler().cancelTask(displayTipsTaskId);
        Audience.audience(context.getParticipants()).sendActionBar(Component.empty());
    }

}
