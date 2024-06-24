package org.braekpo1nt.mctmanager.ui.topbar.components;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A component representing how many kills and deaths a player has
 */
public class KillDeathComponent {
    private int kills;
    private int deaths;
    private final @NotNull Component killsPrefix;
    private final @NotNull Component deathsPrefix;
    
    public KillDeathComponent() {
        this.kills = 0;
        this.deaths = 0;
        this.killsPrefix = Component.text("K: ");
        this.deathsPrefix = Component.text("D: ");
    }
    
    public KillDeathComponent(@NotNull Component killsPrefix, @NotNull Component deathsPrefix) {
        this.killsPrefix = killsPrefix;
        this.deathsPrefix = deathsPrefix;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public @NotNull Component toComponent() {
        return Component.empty()
                .append(killsPrefix)
                .append(Component.text(kills))
                .append(Component.space())
                .append(deathsPrefix)
                .append(Component.text(deaths))
                ;
    }
}
