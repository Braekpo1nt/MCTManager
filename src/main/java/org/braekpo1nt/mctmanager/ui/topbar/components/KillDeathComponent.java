package org.braekpo1nt.mctmanager.ui.topbar.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * A component representing how many kills and deaths a player has
 */
public class KillDeathComponent {
    private int kills;
    /**
     * the number of deaths to display. Negative numbers will result in no
     * deaths being shown. 
     */
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
    
    /**
     * @param deaths the number of deaths to display. Negative numbers will result
     *               in the deaths not displaying at all. 
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public @NotNull Component toComponent() {
        TextComponent.Builder builder = Component.text();
        builder
                .append(killsPrefix)
                .append(Component.text(kills));
        if (deaths >= 0) {
            builder
                    .append(Component.space())
                    .append(deathsPrefix)
                    .append(Component.text(deaths));
        }
        return builder.asComponent();
    }
}
