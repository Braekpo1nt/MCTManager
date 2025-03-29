package org.braekpo1nt.mctmanager.games.experimental;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Implemented by items which take advantage of {@link GameListener}
 * @param <P> the type of participant returned from uuids
 */
public interface GameData<P> {
    
    /**
     * @param uuid the UUID of the participant to get
     * @return the participant with the given UUID (or null if no participant exists)
     */
    @Nullable P getParticipant(UUID uuid);
    
}
