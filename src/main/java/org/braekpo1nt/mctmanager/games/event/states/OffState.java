package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class OffState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    
    public OffState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        // do nothing
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        // do nothing
    }
    
    @Override
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        if (context.getGameManager().editorIsRunning()) {
            return CommandResult.failure(Component.text("Can't start an event while an editor is running."));
        }
        
        try {
            context.setConfig(context.getConfigController().getConfig());
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            return CommandResult.failure(Component.text("Can't start event. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage())));
        }
        
        context.getPlugin().getServer().getPluginManager().registerEvents(context, context.getPlugin());
        context.getGameManager().getTimerManager().register(context.getTimerManager());
        context.setWinningTeam(null);
        context.setMaxGames(numberOfGames);
        context.setCurrentGameNumber(currentGameNumber);
        context.getPlayedGames().clear();
        context.getScoreKeepers().clear();
        context.setSidebar(context.getGameManager().createSidebar());
        context.setAdmins(new ArrayList<>());
        context.setAdminSidebar(context.getGameManager().createSidebar());
        initializeSidebar();
        initializeAdminSidebar();
        context.initializeParticipantsAndAdmins();
//        context.getGameManager().removeParticipantsFromHub(context.getParticipants());
        context.setState(new ReadyUpState(context));
        return CommandResult.success();
    }
    
    private void initializeSidebar() {
        List<Team> sortedTeams = GameManagerUtils.sortTeams(gameManager.getTeams());
        context.setNumberOfTeams(sortedTeams.size());
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
        context.getSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getSidebar().addLines(teamLines);
        context.getSidebar().addLine("personalScore", "");
        context.getSidebar().addLine("timer", "");
        context.getSidebar().updateTitle(context.getConfig().getTitle());
    }
    
    private void initializeAdminSidebar() {
        List<Team> sortedTeams = GameManagerUtils.sortTeams(gameManager.getTeams());
        context.setNumberOfTeams(sortedTeams.size());
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
        context.getAdminSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().addLines(teamLines);
        context.getAdminSidebar().addLine("timer", "");
        context.getAdminSidebar().updateTitle(context.getConfig().getTitle());
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "OffState updatePersonalScores()");
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
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "OffState updateTeamScores()");
        if (context.getSidebar() == null) {
            return;
        }
        List<Team> sortedTeams = GameManagerUtils.sortTeams(updateTeams);
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
    public void onParticipantDamage(EntityDamageEvent event) {
        // do nothing
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
    public CommandResult setMaxGames(int newMaxGames) {
        return CommandResult.failure(Component.text("There is no event running"));
    }
    
    @Override
    public void readyUpParticipant(@NotNull Participant participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void unReadyParticipant(@NotNull Participant participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        sender.sendMessage(Component.text("There is no event going on right now"));
    }
}
