package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlayingFinalGameState extends PlayingGameState {
    
    
    public PlayingFinalGameState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData, eventData.getConfig().getFinaleGame(), eventData.getConfig().getFinaleConfig());
    }
    
    @Override
    protected void postGame() {
        context.setState(new PodiumState(context, contextReference, eventData));
    }
    
    @Override
    protected @NotNull Component createNewTitle(String baseTitle) {
        return Component.empty()
                .append(Component.text(baseTitle))
                .color(NamedTextColor.BLUE);
    }
    
    @Override
    public void setWinner(@NotNull MCTTeam team) {
        eventData.setWinningTeam(team);
        try {
            context.getEventService().setEventWinner(
                    eventData.getEventInfo().getEventId(),
                    team.getTeamId()
            );
        } catch (SQLException e) {
            Main.logger().log(Level.WARNING, "Could not set the winning team in the database", e);
        }
    }
}
