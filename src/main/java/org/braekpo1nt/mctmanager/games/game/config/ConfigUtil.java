package org.braekpo1nt.mctmanager.games.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ConfigUtil {
    
    /**
     * 
     * @param componentDTO a {@link JsonElement} representing a {@link Component}.
     *                     (essentially a /tellraw argument)
     * @return the componentDTO as a {@link Component} object 
     * @throws IllegalArgumentException if the provided componentDTO can't be parsed into a {@link Component} object.
     */
    public static @NotNull Component toComponent(final @NotNull JsonElement componentDTO) throws JsonIOException, JsonSyntaxException {
        try {
            return GsonComponentSerializer.gson().deserializeFromTree(componentDTO);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException(String.format("Unable to parse JsonElement as Component: \"%s\"", componentDTO), e);
        }
    }
    
    /**
     * 
     * @param componentDTOs a list of {@link JsonElement}s representing {@link Component}s.
     *                      (essentially a list of /tellraw arguments)
     * @return a List containing all the componentDTOs as {@link Component} objects
     * @throws IllegalArgumentException if any one of the provided componentDTOs can't be parsed into a {@link Component} object.
     */
    public static @NotNull List<Component> toComponents(final @NotNull List<JsonElement> componentDTOs) throws JsonIOException, JsonSyntaxException {
        return componentDTOs.stream().filter(Objects::nonNull).map(ConfigUtil::toComponent).toList();
    }
    
    private ConfigUtil() {
        // do not instantiate
    }
}
