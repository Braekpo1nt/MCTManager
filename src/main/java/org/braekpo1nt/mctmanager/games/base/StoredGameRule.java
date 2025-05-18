package org.braekpo1nt.mctmanager.games.base;

import org.bukkit.GameRule;

/**
 * Used in {@link GameBase} to set and restore game rules
 * @param rule the rule
 * @param value the value
 * @param <T> the type of the rule's value
 */
public record StoredGameRule<T>(GameRule<T> rule, T value) {
}
