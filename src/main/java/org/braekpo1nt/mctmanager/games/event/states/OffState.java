package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void startEvent(@NotNull CommandSender sender, int numberOfGames) {
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
            Main.logger().severe(e.getMessage());
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
        context.setState(new ReadyUpState(context));
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
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        if (winningTeam == null) {
            Component message = Component.text("No winner declared.");
            context.messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            gameManager.returnAllParticipantsToHub();
            return;
        }
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        Component message = Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" wins!"))
                .color(teamColor)
                .decorate(TextDecoration.BOLD);
        context.messageAllAdmins(message);
        gameManager.messageOnlineParticipants(message);
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(gameManager.getOnlineParticipants())
        ).showTitle(Title.title(formattedTeamDisplayName, Component.text("wins!")
                .color(teamColor), UIUtils.DEFAULT_TIMES));
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        sender.sendMessage(Component.text("There is no event running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        if (!context.getColossalCombatGame().isActive()) {
            sender.sendMessage(Component.text("Colossal Combat is not running")
                    .color(NamedTextColor.RED));
            return;
        }
        context.getColossalCombatGame().stop(null);
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeamId, @NotNull String secondTeamId) {
        if (context.getGameManager().gameIsRunning()) {
            sender.sendMessage(Component.text("Can't start Colossal Combat while a game is running")
                    .color(NamedTextColor.RED));
            return;
        }
        if (context.getColossalCombatGame().isActive()) {
            sender.sendMessage(Component.text("Colossal Combat is already running")
                    .color(NamedTextColor.RED));
            return;
        }
        try {
            context.getColossalCombatGame().loadConfig();
        } catch (ConfigException e) {
            Main.logger().severe(e.getMessage());
            e.printStackTrace();
            sender.sendMessage(Component.text("Error loading config file. See console for details.")
                    .color(NamedTextColor.RED));
            context.messageAllAdmins(Component.text("Can't start ")
                    .append(Component.text("Colossal Combat")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        List<Player> firstPlaceParticipants = new ArrayList<>();
        List<Player> secondPlaceParticipants = new ArrayList<>();
        List<Player> spectators = new ArrayList<>();
        List<Player> participantPool;
        List<Player> adminPool;
        participantPool = new ArrayList<>(gameManager.getOnlineParticipants());
        adminPool = new ArrayList<>(gameManager.getOnlineAdmins());
        for (Player participant : participantPool) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (teamName.equals(firstTeamId)) {
                firstPlaceParticipants.add(participant);
            } else if (teamName.equals(secondTeamId)) {
                secondPlaceParticipants.add(participant);
            } else {
                spectators.add(participant);
            }
        }
        
        if (firstPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeamId, secondTeamId)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (secondPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeamId, secondTeamId)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return;
        }
        
        gameManager.removeParticipantsFromHub(participantPool);
        context.getColossalCombatGame().start(firstPlaceParticipants, secondPlaceParticipants, spectators, adminPool);
    }
    
    @Override
    public void readyUpParticipant(@NotNull Player participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void unReadyParticipant(@NotNull Player participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        sender.sendMessage(Component.text("There is no event going on right now"));
    }
}
