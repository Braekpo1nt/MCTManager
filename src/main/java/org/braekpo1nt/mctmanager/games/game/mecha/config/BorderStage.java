package org.braekpo1nt.mctmanager.games.game.mecha.config;

/**
 * 
 * @param size The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over the previous stage's duration
 * @param delay the border will stay at this stage's size for this many seconds
 * @param duration after the delay, the border will shrink for this many seconds to the next stage's size
 */
public record BorderStage (int size, int delay, int duration){}
