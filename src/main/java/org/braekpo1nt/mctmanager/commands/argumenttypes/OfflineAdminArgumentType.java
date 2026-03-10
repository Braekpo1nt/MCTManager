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
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class OfflineAdminArgumentType implements CustomArgumentType.Converted<OfflinePlayer, String> {
    
    private static final DynamicCommandExceptionType ERROR_ADMIN_DOES_NOT_EXIST = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" is not an admin"))
    ));
    
    private static final DynamicCommandExceptionType ERROR_SQL_COMMUNICATION_FAILED = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Could not connect to database to find "))
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(". see console for details."))
    ));
    
    private final @NotNull GameManager gameManager;
    
    public OfflineAdminArgumentType(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull OfflinePlayer convert(@NotNull String name) throws CommandSyntaxException {
        OfflinePlayer admin;
        try {
            admin = gameManager.getOfflineAdmin(name);
        } catch (SQLException e) {
            throw ERROR_SQL_COMMUNICATION_FAILED.create(name);
        }
        if (admin == null) {
            throw ERROR_ADMIN_DOES_NOT_EXIST.create(name);
        }
        return admin;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            gameManager.getAllAdminNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
