package org.braekpo1nt.mctmanager.ui;

public class MockFastBoardWrapper extends FastBoardWrapper {
    
    private String[] lines;
    
    @Override
    public void updateLine(int line, String text) {
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
}
