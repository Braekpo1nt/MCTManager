package org.braekpo1nt.mctmanager.ui.sidebar;

import org.bukkit.entity.Player;

public class MockFastBoardWrapper extends FastBoardWrapper {
    
    private String[] lines;
    
    @Override
    public void updateLine(int line, String text) {
        if (this.lines == null) {
            this.lines = new String[line+1];
        }
        if (line >= this.lines.length) {
            String[] newLines = new String[line + 1];
            for (int i = 0; i < this.lines.length; i++) {
                newLines[i] = this.lines[i];
            }
            this.lines = newLines;
        }
        this.lines[line] = text;
    }
    
    @Override
    public void updateTitle(String title) {
        
    }
    
    @Override
    public void updateLines(String... lines) {
        this.lines = lines;
    }
    
    @Override
    public boolean isDeleted() {
        return lines == null;
    }
    
    @Override
    public void delete() {
        this.lines = null;
    }
    
    public String[] getLines() {
        return lines;
    }
    
    public String getLine(int line) {
        return lines[line];
    }
    
    @Override
    public void setPlayer(Player player) {
        
    }
}
