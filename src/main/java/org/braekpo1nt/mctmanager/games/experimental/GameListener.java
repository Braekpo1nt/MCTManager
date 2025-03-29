package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.Listener;

import java.util.UUID;

public interface GameListener<P> extends Listener {
    P getParticipant(UUID uuid);
}
