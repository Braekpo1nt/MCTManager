package org.braekpo1nt.mctmanager.games.base;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public abstract class WandsDuoGameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>> extends DuoGameBase<P, T, QP, QT, S> {
    
    protected final @NotNull Collection<Wand<P>> wands;
    private int wandTickTaskId;
    
    /**
     * @param gameInstanceId the {@link GameInstanceId} associated with this game
     * @param plugin the plugin
     * @param gameManager the GameManager
     * @param title the game's initial title, displayed in the sidebar
     * @param initialState the initialization state, should not contain any game functionality.
     * The state must never be null, so this is what the state should be
     * as the game is being initialized to prevent null-pointer
     * exceptions.
     * @param northTeam the north team
     * @param southTeam the south team
     */
    public WandsDuoGameBase(
            @NotNull GameInstanceId gameInstanceId,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState,
            @NotNull T northTeam,
            @NotNull T southTeam
    ) {
        super(gameInstanceId, plugin, gameManager, title, initialState, northTeam, southTeam);
        this.wands = new ArrayList<>();
    }
}
