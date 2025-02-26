package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VotingState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    
    public VotingState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.voteManager = context.getVoteManager();
        List<GameType> votingPool = new ArrayList<>(List.of(GameType.values()));
        votingPool.removeAll(context.getPlayedGames());
        context.getSidebar().removeAllPlayers();
        context.getAdminSidebar().removeAllPlayers();
        List<Player> adminCopy = new ArrayList<>(context.getAdmins());
        context.getParticipants().clear();
        context.getAdmins().clear();
        voteManager.startVote(context.getParticipants().values(), votingPool, context.getConfig().getVotingDuration(), 
                this::startingGameDelay, adminCopy);
    }
    
    private void startingGameDelay(GameType gameType) {
        context.setState(new StartingGameDelayState(context, gameType));
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        gameManager.returnParticipantToHubInstantly(participant);
        voteManager.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        voteManager.onParticipantQuit(participant);
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        voteManager.onAdminJoin(admin);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        voteManager.onAdminQuit(admin);
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber) {
        sender.sendMessage(Component.text("An event is already running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.VotingState.onPlayerDamage() cancelled");
        event.setCancelled(true);
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
    public void colossalCombatIsOver(@Nullable Team winningTeam) {
        // do nothing
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber()) {
            sender.sendMessage(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(context.getCurrentGameNumber())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber() - 1))
                    .append(Component.text(" game(s) have been played and voting is in progress."))
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
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Colossal Combat is not running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull Team firstTeam, @NotNull Team secondTeam) {
        sender.sendMessage(Component.text("Can't start Colossal Combat during voting")
                .color(NamedTextColor.RED));
    }
}
