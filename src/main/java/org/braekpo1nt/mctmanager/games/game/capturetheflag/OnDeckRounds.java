package org.braekpo1nt.mctmanager.games.game.capturetheflag;

/**
 * Describes how many rounds a player has spent on-deck, and the last round that they played in
 * @param roundsSpentOnDeck the number of rounds that a participant spent on-deck
 * @param lastPlayedRound the last round that a participant played (-1 if they've never played a round)
 */
record OnDeckRounds(int roundsSpentOnDeck, int lastPlayedRound) {
}
