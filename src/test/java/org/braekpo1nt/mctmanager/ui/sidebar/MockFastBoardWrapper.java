package org.braekpo1nt.mctmanager.ui.sidebar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
    public void updateLine(int line, Component text) {
        updateLine(line, PlainTextComponentSerializer.plainText().serialize(text));
    }
    
    @Override
    public void updateTitle(String title) {
        
    }
    
    @Override
    public int size() {
        return lines.length;
    }
    
    @Override
    public void removeLine(int line) {
        ArrayList<String> collect = Arrays.stream(this.lines).collect(Collectors.toCollection(ArrayList::new));
        collect.remove(line);
        this.lines = collect.toArray(new String[0]);
    }
    
    @Override
    public void updateTitle(Component title) {
        
    }
    
    @Override
    public void updateLines(String... lines) {
        this.lines = lines;
    }
    
    @Override
    public void updateLines(Collection<Component> lines) {
        this.lines = lines.stream().map(line -> PlainTextComponentSerializer.plainText().serialize(line)).toList().toArray(new String[0]);
    }
    
    @Override
    public void updateLines(Component... lines) {
        updateLines(Arrays.stream(lines).toList());
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
