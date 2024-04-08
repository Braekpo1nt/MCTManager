package org.braekpo1nt.mctmanager.games.game.capturetheflag;

public enum BattleClass {
    KNIGHT("Knight"),
    ARCHER("Archer"),
    ASSASSIN("Assassin"),
    TANK("Tank");
    
    private final String name;
    
    BattleClass(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
