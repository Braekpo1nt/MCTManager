package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OffState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    
    public OffState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
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
    public void startEvent(CommandSender sender, int numberOfGames) {
        if (context.getGameManager().gameIsRunning()) {
            sender.sendMessage(Component.text("Can't start an event while a game is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (context.getGameManager().editorIsRunning()) {
            sender.sendMessage(Component.text("Can't start an event while an editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        try {
            context.setConfig(context.getConfigController().getConfig());
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("Can't start event. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        
        context.getPlugin().getServer().getPluginManager().registerEvents(context, context.getPlugin());
        context.getGameManager().getTimerManager().register(context.getTimerManager());
        context.setWinningTeam(null);
        context.setMaxGames(numberOfGames);
        context.setCurrentGameNumber(1);
        context.getPlayedGames().clear();
        context.getScoreKeepers().clear();
        context.setParticipants(new ArrayList<>());
        context.setSidebar(context.getGameManager().getSidebarFactory().createSidebar());
        context.setAdmins(new ArrayList<>());
        context.setAdminSidebar(context.getGameManager().getSidebarFactory().createSidebar());
        initializeSidebar();
        initializeAdminSidebar();
        context.initializeParticipantsAndAdmins();
        context.getGameManager().removeParticipantsFromHub(context.getParticipants());
        context.messageAllAdmins(Component.text("Starting event. On game ")
                .append(Component.text(context.getCurrentGameNumber()))
                .append(Component.text("/"))
                .append(Component.text(context.getMaxGames()))
                .append(Component.text(".")));
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(context.getParticipants())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("Event Starting"))
                        .color(NamedTextColor.GOLD)
        ));
        context.setState(new WaitingInHubState(context));
    }
    
    private void initializeSidebar() {
        List<String> sortedTeamNames = context.sortTeamNames(gameManager.getTeamNames());
        context.setNumberOfTeams(sortedTeamNames.size());
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        context.getSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getSidebar().addLines(teamLines);
        context.getSidebar().addLine("personalScore", "");
        context.getSidebar().addLine("timer", "");
        context.getSidebar().updateTitle(context.getConfig().getTitle());
        context.updatePersonalScores();
    }
    
    private void initializeAdminSidebar() {
        List<String> sortedTeamNames = context.sortTeamNames(gameManager.getTeamNames());
        context.setNumberOfTeams(sortedTeamNames.size());
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            String teamName = sortedTeamNames.get(i);
            String teamDisplayName = gameManager.getTeamDisplayName(teamName);
            ChatColor teamChatColor = gameManager.getTeamChatColor(teamName);
            int teamScore = gameManager.getScore(teamName);
            teamLines[i] = new KeyLine("team"+i, String.format("%s%s: %s", teamChatColor, teamDisplayName, teamScore));
        }
        context.getAdminSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().addLines(teamLines);
        context.getAdminSidebar().addLine("timer", "");
        context.getAdminSidebar().updateTitle(context.getConfig().getTitle());
    }
    
    @Override
    public void stopEvent(CommandSender sender) {
        sender.sendMessage(Component.text("There is no event running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        sender.sendMessage(Component.text("There is no event running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(GameType finishedGameType) {
        // do nothing
    }
}
