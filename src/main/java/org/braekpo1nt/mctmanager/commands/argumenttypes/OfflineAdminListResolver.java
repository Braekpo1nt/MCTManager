package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Argument resolvers allow the more intense work of resolving an argument to
 * be performed only when the command is executed. Also allows for certain tasks
 * to be performed asynchronously, as long as they don't access the bukkit api.
 * <br>
 * This one resolves an {@link OfflinePlayer} from the name of an admin
 */
public class OfflineAdminListResolver {
    
    private static final DynamicCommandExceptionType ERROR_NOT_AN_ADMIN = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" is not an admin"))
    ));
    
    private final @NotNull GameManager gameManager;
    private final @NotNull PlayerProfileListResolver profileResolver;
    
    public OfflineAdminListResolver(@NotNull GameManager gameManager, @NotNull PlayerProfileListResolver profileResolver) {
        this.gameManager = gameManager;
        this.profileResolver = profileResolver;
    }
    
    public @NotNull Collection<OfflinePlayer> resolve(@NotNull CommandSourceStack source) throws CommandSyntaxException {
        Collection<PlayerProfile> profiles = profileResolver.resolve(source);
        List<OfflinePlayer> results = new ArrayList<>(profiles.size());
        for (PlayerProfile profile : profiles) {
            if (profile.getId() != null) {
                OfflinePlayer admin = gameManager.getOfflineAdmin(profile.getId());
                if (admin == null) {
                    throw ERROR_NOT_AN_ADMIN.create(profile.getName());
                }
                results.add(admin);
            }
        }
        return results;
    }
}
