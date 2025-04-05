package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.GameRule;

public record StoredGameRule<T>(GameRule<T> rule, T value) {
}
