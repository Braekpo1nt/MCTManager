package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class ActiveState extends GameplayState {
    
    private final Timer gameTimer;
    private final Component slashTopMessage;
    
    public ActiveState(@NotNull FarmRushGame context) {
        super(context);
        TopCommand.setEnabled(true);
        slashTopMessage = Component.empty()
                .append(Component.text("Use "))
                .append(Component.text("/top")
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.suggestCommand("/top")));
        gameTimer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .completionSeconds(context.getConfig().getGracePeriodDuration())
                .onCompletion(() -> {
                    Component timeLeft = TimeStringUtils.getTimeComponent(context.getConfig().getGracePeriodDuration());
                    context.titleAllParticipants(UIUtils.defaultTitle(
                            Component.empty(),
                            Component.empty()
                                    .append(timeLeft)
                                    .append(Component.text(" left!"))
                                    .color(NamedTextColor.RED)));
                    context.messageAllParticipants(slashTopMessage);
                    context.setState(new GracePeriodState(context));
                })
                .build());
        for (FarmRushGame.Participant participant : context.getParticipants().values()) {
            participant.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
        
        for (FarmRushGame.Team team : context.getTeams().values()) {
            team.getArena().openBarnDoor();
        }
        context.getPowerupManager().start();
    }
    
    @Override
    public void onCloseInventory(InventoryCloseEvent event, FarmRushGame.Participant participant) {
        FarmRushGame.Team team = context.getTeams().get(participant.getTeamId());
        int oldScore = team.getTotalScore();
        sellItemsOnCloseInventory(event, participant);
        if (context.getConfig().shouldEnforceMaxScore() && teamReachedMaxScore(team)) {
            onTeamReachMaxScore(team);
            return;
        }
        int warningThreshold = calculateWarningThreshold();
        boolean teamWasNotAboveThreshold = oldScore < warningThreshold;
        boolean teamIsNowAboveThreshold = team.getTotalScore() >= warningThreshold;
        if (context.getConfig().shouldEnforceMaxScore() && 
                context.getConfig().shouldWarnAtThreshold() && 
                teamWasNotAboveThreshold && teamIsNowAboveThreshold) {
            onTeamReachWarningThreshold(team);
        }
    }
    
    private boolean teamReachedMaxScore(FarmRushGame.Team team) {
        return team.getTotalScore() >= getTrueMaxScore();
    }
    
    private void onTeamReachMaxScore(FarmRushGame.Team winingTeam) {
        gameTimer.cancel();
        gameManager.awardPointsToTeam(winingTeam.getTeamId(), context.getConfig().getWinnerBonus());
        context.messageAllParticipants(Component.empty()
                .append(winingTeam.getDisplayName())
                .append(Component.text(" reached "))
                .append(Component.text(getTrueMaxScore())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" points first! "))
                .append(slashTopMessage)
        );
        Component timeLeft = TimeStringUtils.getTimeComponent(context.getConfig().getGracePeriodDuration());
        context.titleAllParticipants(UIUtils.defaultTitle(
                Component.empty()
                        .append(winingTeam.getDisplayName())
                        .append(Component.text("wins!")),
                Component.empty()
                        .append(timeLeft)
                        .append(Component.text(" left!"))
                        .color(NamedTextColor.RED)));
        context.setState(new GracePeriodState(context));
    }
    
    /**
     * @return the multiplied max score
     */
    private int getTrueMaxScore() {
        return (int) (context.getConfig().getMaxScore() * gameManager.matchProgressPointMultiplier());
    }
    
    /**
     * @return the score that players should be warned about when a team passes, accounting for matchProgressMultiplier
     */
    private int calculateWarningThreshold() {
        return (int) (context.getConfig().getWarningThreshold() *
                context.getConfig().getMaxScore() *
                gameManager.matchProgressPointMultiplier());
    }
    
    private void onTeamReachWarningThreshold(FarmRushGame.Team team) {
        context.messageAllParticipants(Component.empty()
                .append(team.getDisplayName())
                .append(Component.text(" has "))
                .append(Component.text(team.getTotalScore())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text("/"))
                .append(Component.text(getTrueMaxScore())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" points"))
        );
    }
}
