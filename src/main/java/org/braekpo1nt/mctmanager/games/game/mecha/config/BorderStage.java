package org.braekpo1nt.mctmanager.games.game.mecha.config;

/**
 * 
 * @param size The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over this stage's duration
 * @param delay the border will stay at the previous stage's size for this many seconds
 * @param duration the border will take this many seconds to transition from the previous stage's size to this stage's size
 */
public record BorderStage (int size, int delay, int duration){}
