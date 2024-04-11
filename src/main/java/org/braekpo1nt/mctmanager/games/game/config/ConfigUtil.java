package org.braekpo1nt.mctmanager.games.game.config;

import com.google.gson.*;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTODeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConfigUtil {
    
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemMetaDTO.class, new ItemMetaDTODeserializer())
            .create();
    
    /**
     * 
     * @param componentDTO a {@link JsonElement} representing a {@link Component}.
     *                     (essentially a /tellraw argument)
     * @return the componentDTO as a {@link Component} object ({@link Component#empty()} if the componentDTO is null)
     * @throws IllegalArgumentException if the provided componentDTO can't be parsed into a {@link Component} object.
     */
    public static @NotNull Component toComponent(final @Nullable JsonElement componentDTO) throws JsonIOException, JsonSyntaxException {
        if (componentDTO == null) {
            return Component.empty();
        }
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
     * @return a List containing all the componentDTOs as {@link Component} objects, or an empty list if the componentDTOs is empty or null. Null elements of componentDTOs will be filtered out (ignored).
     * @throws IllegalArgumentException if any one of the provided componentDTOs can't be parsed into a {@link Component} object.
     */
    public static @NotNull List<Component> toComponents(final @Nullable List<@Nullable JsonElement> componentDTOs) throws JsonIOException, JsonSyntaxException {
        if (componentDTOs == null) {
            return Collections.emptyList();
        }
        return componentDTOs.stream().filter(Objects::nonNull).map(ConfigUtil::toComponent).toList();
    }
    
    private ConfigUtil() {
        // do not instantiate
    }
}
