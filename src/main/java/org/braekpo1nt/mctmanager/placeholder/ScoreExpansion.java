package org.braekpo1nt.mctmanager.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreExpansion extends PlaceholderExpansion {
    
    private final GameManager gameManager;
    
    public ScoreExpansion(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "score";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "Braekpo1nt";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "0.1.0";
    }
    
    /**
     * This ensures that the expansion won't be unregistered by PlaceholderAPI whenever it is reloaded.
     * @return true
     */
    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }
        if (params.equalsIgnoreCase("player_score")) {
            if (!gameManager.hasPlayer(player.getUniqueId())) {
                return "-";
            }
            return String.valueOf(gameManager.getPlayerScore(player.getUniqueId()));
        }
        return null;
    }
}
