package org.braekpo1nt.mctmanager.games.base.states;

import org.jetbrains.annotations.Nullable;

public interface Kitted {
    @Nullable String getKitId();
    
    void setKitId(@Nullable String kitId);
}
