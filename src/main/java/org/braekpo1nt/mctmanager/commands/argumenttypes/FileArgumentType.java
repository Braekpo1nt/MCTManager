package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class FileArgumentType implements CustomArgumentType.Converted<File, String> {
    
    private static final DynamicCommandExceptionType ERROR_FILE_NOT_FOUND = new DynamicCommandExceptionType(filePath -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("File not found: "))
            .append(Component.text(filePath.toString()))
    ));
    
    private final @NotNull File parentDirectory;
    private final @Nullable FileFilter filter;
    
    /**
     * @param parentDirectory the directory to search for files in
     * @param filter the file filter to apply to files in the parentDirectory before suggesting them
     */
    public FileArgumentType(@NotNull File parentDirectory, @Nullable FileFilter filter) {
        this.parentDirectory = parentDirectory;
        this.filter = filter;
    }
    
    /**
     * @param parentDirectory the directory to search for files in
     * @param endsWith a shortcut for a file filter that checks if the given file ends with the given string (e.g.
     * ".json")
     */
    public FileArgumentType(@NotNull File parentDirectory, @NotNull String endsWith) {
        this(parentDirectory, file -> file.getName().endsWith(endsWith));
    }
    
    /**
     * @param parentDirectory the directory to search for files in
     */
    public FileArgumentType(@NotNull File parentDirectory) {
        this(parentDirectory, (FileFilter) null);
    }
    
    @Override
    public @NotNull File convert(@NotNull String fileName) throws CommandSyntaxException {
        File file = new File(parentDirectory, fileName);
        if (!file.exists()) {
            throw ERROR_FILE_NOT_FOUND.create(file.getAbsolutePath());
        }
        return file;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            File[] files = parentDirectory.listFiles(filter);
            if (files != null) {
                Arrays.stream(files)
                        .map(File::getName)
                        .filter(fileName -> fileName.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);
            }
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
