package org.braekpo1nt.mctmanager.games.game.capturetheflag;

/**
 * Describes how many rounds a player has spent on-deck, and the last round that they played in
 */
class OnDeckRounds {
    private int roundsSpentOnDeck;
    private int lastPlayedRound;
    
    /**
     * @param roundsSpentOnDeck the number of rounds that a participant spent on-deck
     * @param lastPlayedRound the last round that a participant played (-1 if they've never played a round)
     */
    public OnDeckRounds(int roundsSpentOnDeck, int lastPlayedRound) {
        this.roundsSpentOnDeck = roundsSpentOnDeck;
        this.lastPlayedRound = lastPlayedRound;
    }
    
    public int getRoundsSpentOnDeck() {
        return roundsSpentOnDeck;
    }
    
    public int getLastPlayedRound() {
        return lastPlayedRound;
    }
    
    /**
     * Add 1 to roundsSpentOnDeck
     * @return the new value for roundsSpentOnDeck after incrementing
     */
    public int incrementRoundsSpentOnDeck() {
        roundsSpentOnDeck++;
        return roundsSpentOnDeck;
    }
    
    /**
     * Add 1 to lastPlayedRound
     * @return the new value for lastPlayedRound after incrementing
     */
    public int incrementLastPlayedRound() {
        lastPlayedRound++;
        return lastPlayedRound;
    }
}
