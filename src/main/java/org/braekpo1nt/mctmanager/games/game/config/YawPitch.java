package org.braekpo1nt.mctmanager.games.game.config;

/**
 * Simple record to store the yaw and pitch of a Location. Useful for serializing the directional portion of a Location in JSON. 
 * @param yaw the yaw of a Location
 * @param pitch the pitch of a Location
 */
public record YawPitch(float yaw, float pitch) {
}
