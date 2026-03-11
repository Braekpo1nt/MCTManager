package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Argument resolvers allow the more intense work of resolving an argument to
 * be performed only when the command is executed. Also allows for certain tasks
 * to be performed asynchronously, as long as they don't access the bukkit api.
 * <br>
 * This one checks to see if a file exists before returning it. Saves us from
 * checking the file system after every character is typed in the command argument.
 */
public class FileResolver {
    
    private static final DynamicCommandExceptionType ERROR_FILE_NOT_FOUND = new DynamicCommandExceptionType(filePath -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("File not found: "))
            .append(Component.text(filePath.toString()))
    ));
    
    private final @NotNull File file;
    
    public FileResolver(@NotNull File file) {
        this.file = file;
    }
    
    public @NotNull File resolve() throws CommandSyntaxException {
        if (!file.exists()) {
            throw ERROR_FILE_NOT_FOUND.create(file.getAbsolutePath());
        }
        return file;
    }
    
    public @NotNull File resolveNotExists() {
        return file;
    }
}
