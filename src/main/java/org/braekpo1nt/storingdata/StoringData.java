package org.braekpo1nt.storingdata;

import org.braekpo1nt.storingdata.commands.CreateNoteCommand;
import org.braekpo1nt.storingdata.utils.NoteStorageUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class StoringData extends JavaPlugin {
    
    private static StoringData plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        this.getCommand("notes").setExecutor(new CreateNoteCommand());
        try {
            NoteStorageUtil.loadNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public static StoringData getPlugin() {
        return plugin;
    }
}
