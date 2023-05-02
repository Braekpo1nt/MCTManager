package org.braekpo1nt.mctmanager.ui;

public class MockFastBoard {
    
    private String[] lines;
    
    public void updateLine(int line, String text) {
        this.lines[line] = text;
    }
    
    public void updateTitle(String title) {
        
    }
    
    public void updateLines(String... lines) {
        this.lines = lines;
    }
    
    public boolean isDeleted() {
        return false;
    }
    
    public void delete() {
        this.lines = null;
    }
    
    public String[] getLines() {
        return lines;
    }
}
