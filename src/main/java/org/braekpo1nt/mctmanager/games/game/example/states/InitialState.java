package org.braekpo1nt.mctmanager.games.game.example.states;

import org.braekpo1nt.mctmanager.games.base.states.DoNothingState;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.games.game.example.ExampleTeam;

/**
 * Used during initialization, does nothing, prevents null pointer exceptions
 */
public class InitialState implements ExampleState, DoNothingState<ExampleParticipant, ExampleTeam> {
}
