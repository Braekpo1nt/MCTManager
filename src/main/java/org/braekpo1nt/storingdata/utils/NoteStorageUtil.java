package org.braekpo1nt.storingdata.utils;

import com.google.gson.Gson;
import org.braekpo1nt.storingdata.StoringData;
import org.braekpo1nt.storingdata.models.Note;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteStorageUtil {
    
    //CRUD - Create, Read, Update, Delete
    private static ArrayList<Note> notes = new ArrayList<>();
    
    public static Note createNote(Player p, String message) {
        
        Note note = new Note(p.getName(), message);
        notes.add(note);

        try {
            saveNotes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return note;
    }
    
    public static void deleteNote(String id) {
        
        for(Note note : notes) {
            if (note.getId().equalsIgnoreCase(id)) {
                notes.remove(note);
                break;
            }
        }
        try {
            saveNotes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Note findNote(String id) {
        
        for(Note note : notes) {
            if (note.getId().equalsIgnoreCase(id)) {
                return note;
            }
        }
        
        return null;
    }
    
    public static Note updateNote(String id, Note newNote) {
        
        for (Note note : notes) {
            if (note.getId().equalsIgnoreCase(id)) {
                note.setPlayerName(newNote.getPlayerName());
                note.setMessage(newNote.getMessage());
                
                try {
                    saveNotes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
                return note;
            }
        }
        return null;
    }
    
    public static void saveNotes() throws IOException {
        
        Gson gson = new Gson();
        File file = new File(StoringData.getPlugin().getDataFolder().getAbsolutePath() + "/notes.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = new FileWriter(file, false);
        gson.toJson(notes, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("Notes saved.");
        
    }
    
    public static void loadNotes() throws IOException {
        Gson gson = new Gson();
        File file = new File(StoringData.getPlugin().getDataFolder().getAbsolutePath() + "/notes.json");
        if (file.exists()) {
            Reader reader = new FileReader(file);
            Note[] n = gson.fromJson(reader, Note[].class);
            notes = new ArrayList<>(Arrays.asList(n));
            Bukkit.getLogger().info("Notes loaded.");
        }
    }
    
    public static List<Note> findAllNotes() {
        return notes;
    }
    
}
