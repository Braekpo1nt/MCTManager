package org.braekpo1nt.mctmanager.participant;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A convenience unit for easily differentiating between participants
 * when they change teams. Participants can change teams, and should
 * be treated as a separate entity when re-joining a game.
 */
public record ParticipantID(@NotNull UUID uuid, @NotNull String teamId) {
}
