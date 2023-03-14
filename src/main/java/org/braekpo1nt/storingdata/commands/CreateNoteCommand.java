package org.braekpo1nt.storingdata.commands;

import org.braekpo1nt.storingdata.models.Note;
import org.braekpo1nt.storingdata.utils.NoteStorageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CreateNoteCommand implements TabExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);
            if (args.length > 0) {
                switch (args[0]) {
                    case "create":
                        String[] message = Arrays.copyOfRange(args, 1, args.length);
                        create(player, message);
                        break;
                    case "listnotes":
                        listNotes(player);
                        break;
                    default:
                        sender.sendMessage(args[0] + " is not a recognized option.");
                }
            }
        }
        return true;
    }
    
    public boolean listNotes(Player player) {
        List<Note> notes = NoteStorageUtil.findAllNotes();
        for (Note note : notes) {
            player.sendMessage(note.getMessage());
        }
        return true;
    }
    
    public boolean create(Player player, @NotNull String[] args) {
        if (args.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (args.length - 1); i++) {
                stringBuilder.append(args[i]).append(" ");
            }
            stringBuilder.append(args[args.length - 1]);
            
            NoteStorageUtil.createNote(player, stringBuilder.toString());
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Arrays.asList("create", "listnotes");
    }
}
