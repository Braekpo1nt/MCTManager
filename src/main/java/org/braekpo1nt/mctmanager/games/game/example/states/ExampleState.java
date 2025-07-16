package org.braekpo1nt.mctmanager.games.game.example.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.games.game.example.ExampleTeam;
import org.bukkit.event.entity.EntityToggleGlideEvent;

/**
 * The interface implemented by all this game's states
 */
public interface ExampleState extends GameStateBase<ExampleParticipant, ExampleTeam> {
    // Any method declarations specific to this game's states
    void onParticipantToggleGlide(EntityToggleGlideEvent event, ExampleParticipant participant);
}
