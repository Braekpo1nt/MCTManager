package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.Listener;

import java.util.UUID;

public interface GameListener<P extends ParticipantData> extends Listener {
    P getParticipant(UUID uuid);
}
