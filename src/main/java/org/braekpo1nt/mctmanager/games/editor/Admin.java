package org.braekpo1nt.mctmanager.games.editor;

import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.nonParticipant.NonParticipantType;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Admin extends NonParticipantType implements AudienceDelegate {
    
    private final @NotNull Player player;
    
    public Admin(@NotNull Player player) {
        super(player);
        this.player = player;
    }
    
    @Override
    public @NotNull Audience getAudience() {
        return player;
    }
}
