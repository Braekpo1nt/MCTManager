package org.braekpo1nt.mctmanager.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.io.IOUtils;
import org.braekpo1nt.mctmanager.ui.maps.ImageMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

public class UIUtils {
    private static final Component KILL_PREFIX = Component.empty()
            .append(Component.text("["))
            .append(Component.text("k")
                    .color(NamedTextColor.GREEN))
            .append(Component.text("] "))
            ;
    private static final Title.Times KILL_TIMES = Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
    );
    /**
     * The title times used across the plugin
     */
    public static final Title.Times DEFAULT_TIMES = Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
    );
    
    /**
     * Used across the plugin to show titles with unified timings
     * @param title the title component
     * @param subtitle the subtitle component
     * @return a new title with the given title and subtitle, with the {@link #DEFAULT_TIMES} times
     */
    public static Title defaultTitle(@NotNull Component title, @NotNull Component subtitle) {
        return Title.title(title,subtitle, DEFAULT_TIMES);
    }
    
    private UIUtils() {
        // do not instantiate
    }
    
    /**
     * Formats the input components with 40 spaces between the centers of each component.
     *
     * @param left the left component
     * @param middle the middle component
     * @param right the right component
     * @param spaceBetween the number of spaces between the centers of each component
     * @return the formatted component
     */
    public static @NotNull Component formatBossBarName(@NotNull Component left, @NotNull Component middle, @NotNull Component right, int spaceBetween) {
        String leftStr = PlainTextComponentSerializer.plainText().serialize(left);
        String middleStr = PlainTextComponentSerializer.plainText().serialize(middle);
        String rightStr = PlainTextComponentSerializer.plainText().serialize(right);
    
        int leftLength = leftStr.length();
        int middleLength = middleStr.length();
        int rightLength = rightStr.length();
        return formatBossBarName(left, leftLength, middle, middleLength, right, rightLength, spaceBetween);
    }
    
    /**
     * Formats the input components with 40 spaces between the centers of each component.
     *
     * @param left the left component
     * @param leftLength the length of the left component
     * @param middle the middle component
     * @param middleLength the length of the middle component
     * @param right the right component
     * @param rightLength the length of the right component
     * @param spaceBetween the number of spaces between the centers of each component
     * @return the formatted component
     */
    public static @NotNull Component formatBossBarName(@NotNull Component left, int leftLength, @NotNull Component middle, int middleLength, @NotNull Component right, int rightLength, int spaceBetween) {
        int leftPadding = spaceBetween - leftLength / 2 - middleLength / 2;
        int rightPadding = spaceBetween - middleLength / 2 - rightLength / 2;
        
        TextComponent paddingLeft = Component.text(" ".repeat(Math.max(0, leftPadding)));
        TextComponent paddingRight = Component.text(" ".repeat(Math.max(0, rightPadding)));
        
        return Component.empty()
                .append(left)
                .append(paddingLeft)
                .append(middle)
                .append(paddingRight)
                .append(right);
    }
    
    /**
     * Shows a subtitle to the killer indicating that they killed the given player
     * @param killer the one who killed
     * @param killed the one who was killed (the one who died)
     */
    public static void showKillTitle(Player killer, Player killed) {
        killer.showTitle(Title.title(
                Component.empty(), 
                Component.empty()
                        .append(KILL_PREFIX)
                        .append(killed.displayName()),
                KILL_TIMES
        ));
    }
    
    /**
     * @return the default game over title, used everywhere
     */
    public static Title gameOverTitle() {
        return Title.title(
                Component.empty()
                        .append(Component.text("Game Over!"))
                        .color(NamedTextColor.RED),
                Component.empty(),
                DEFAULT_TIMES
        );
    }
    
    /**
     * @return the default game over title, used everywhere
     */
    public static Title roundOverTitle() {
        return Title.title(
                Component.empty()
                        .append(Component.text("Round Over!"))
                        .color(NamedTextColor.RED),
                Component.empty(),
                DEFAULT_TIMES
        );
    }
    
    /**
     * @return the default game over title, used everywhere
     */
    public static Title matchOverTitle() {
        return Title.title(
                Component.empty()
                        .append(Component.text("Match Over!"))
                        .color(NamedTextColor.RED),
                Component.empty(),
                DEFAULT_TIMES
        );
    }
    
    /**
     * Create a MapRenderer from the given file, if it exists. 
     * 
     * @param imageFile the File containing the image
     * @return The MapRenderer with the given image, resized to 127x127
     * @throws IOException if the file does not exist, or there is a problem turning it into a
     * {@link MapRenderer}.
     */
    public static @NotNull MapRenderer createMapRenderer(@NotNull File imageFile) throws IOException {
        BufferedImage image = IOUtils.toBufferedImage(imageFile);
        BufferedImage resizedImage = MapPalette.resizeImage(image);
        return new ImageMapRenderer(resizedImage);
    }
    
    /**
     * Create a map item with the given image file resized to fit the map
     * @param world this needs a world to be associated with it to create the {@link MapView}. This just has to be any non-null world object. 
     * @param imageFile the file to display on the map (will be resized to 127x127)
     * @return an ItemStack of the map item
     * @throws IOException if the image file doesn't exist or can't be read/parsed.
     */
    public static ItemStack createMapItem(@NotNull World world, @NotNull File imageFile) throws IOException {
        MapRenderer mapRenderer = createMapRenderer(imageFile);
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        
        MapView mapView = Bukkit.createMap(world);
        mapView.getRenderers().clear();
        mapView.addRenderer(mapRenderer);
        
        mapMeta.setMapView(mapView);
        mapItem.setItemMeta(mapMeta);
        return mapItem;
    }
    
    /**
     *
     * @param entity the entity to get the data for
     * @param glowing whether the entity should be glowing
     * @return a base entity metadata byte containing the flags representing the
     * given entity's true state, but with the given glowing flag.
     */
    public static byte getTrueEntityDataByte(Entity entity, boolean glowing) {
        byte flags = 0x00;
        
        // Check if the entity is on fire
        if (entity.isVisualFire()) {
            flags |= 0x01;
        }
        
        // Check if the entity is crouching (only players can crouch)
        if (entity.isSneaking()) {
            flags |= 0x02;
        }
        
        // Check if the entity is sprinting (only players can sprint)
        if (entity instanceof Player player && player.isSprinting()) {
            flags |= 0x08;
        }
        
        // Check if the entity is swimming (only living entities can swim)
        if (entity instanceof LivingEntity livingEntity && livingEntity.isSwimming()) {
            flags |= 0x10;
        }
        
        // Check if the entity is invisible
        if (entity.isInvisible()) {
            flags |= 0x20;
        }
        
        // Check if the entity has a glowing effect
        if (glowing) {
            flags |= 0x40;
        }
        
        // Check if the entity is flying with an elytra (only players can fly with an elytra)
        if (entity instanceof LivingEntity livingEntity && livingEntity.isGliding()) {
            flags |= (byte) 0x80;
        }
        
        return flags;
    }
    
    /**
     * Log a UI error. This will print the full stack trace and the given reason to
     * the console, no need to specify the specific location in your error message.
     * 
     * @param reason the reason for the error (a {@link String#format(String, Object...)} template
     * @param args optional args for the reason format string
     */
    public static void logUIError(@NotNull String reason, Object... args) {
        Main.logger().log(Level.SEVERE, "A UI error occurred. Failing gracefully.",
                new UIException(String.format(reason, args)));
    }
}
