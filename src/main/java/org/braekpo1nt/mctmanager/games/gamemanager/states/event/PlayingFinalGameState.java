package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.jetbrains.annotations.NotNull;

public class PlayingFinalGameState extends PlayingGameState {
    
    
    public PlayingFinalGameState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData, GameType.FINAL, eventData.getConfig().getColossalCombatConfig());
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
    }
}
