package org.braekpo1nt.mctmanager.games.editor.wand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@Setter
@Builder
@AllArgsConstructor
public class Wand {
    
    @Getter
    private final @NotNull ItemStack wandItem;
    
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onInteract = event -> {};
    
    /**
     * Called on every {@link Action#isRightClick()}
     */
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightClick = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightClickAir = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightClickBlock = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightSneakClick = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightSneakClickAir = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onRightSneakClickBlock = event -> {};
    
    /**
     * Called on every {@link Action#isLeftClick()}
     */
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftClick = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftClickAir = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftClickBlock = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftSneakClick = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftSneakClickAir = event -> {};
    @Builder.Default
    private @NotNull Consumer<PlayerInteractEvent> onLeftSneakClickBlock = event -> {};
    
    public Wand(@NotNull ItemStack wandItem) {
        this.wandItem = wandItem;
    }
    
    public Wand(
            @NotNull Material material, 
            @NotNull Component displayName, 
            @NotNull List<Component> lore) {
        this(createWandItem(material, displayName, lore));
    }
    
    /**
     * @param material the material
     * @param displayName the display name
     * @param lore the lore
     * @return an ItemStack using the given attributes
     */
    public static ItemStack createWandItem(@NotNull Material material, @NotNull Component displayName, @NotNull List<Component> lore) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(displayName);
            meta.lore(lore);
        });
        return item;
    }
    
    public static ItemStack createWandItem(@NotNull Material material, @NotNull String displayName, @NotNull List<Component> lore) {
        return createWandItem(material, Component.text(displayName), lore);
    }
    
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        ItemStack usedItem = event.getItem();
        if (usedItem == null) {
            return;
        }
        if (!usedItem.getType().equals(wandItem.getType())) {
            return;
        }
        if (!usedItem.getItemMeta().equals(wandItem.getItemMeta())) {
            return;
        }
        if (event.useItemInHand() != Event.Result.DENY) {
            onInteract.accept(event);
            Action action = event.getAction();
            if (event.getPlayer().isSneaking()) {
                switch (action) {
                    case RIGHT_CLICK_AIR -> {
                        onRightSneakClick.accept(event);
                        onRightSneakClickAir.accept(event);
                    }
                    case RIGHT_CLICK_BLOCK -> {
                        onRightSneakClick.accept(event);
                        onRightSneakClickBlock.accept(event);
                    }
                    case LEFT_CLICK_AIR -> {
                        onLeftSneakClick.accept(event);
                        onLeftSneakClickAir.accept(event);
                    }
                    case LEFT_CLICK_BLOCK -> {
                        onLeftSneakClick.accept(event);
                        onLeftSneakClickBlock.accept(event);
                    }
                }
            } else {
                switch (action) {
                    case RIGHT_CLICK_AIR -> {
                        onRightClick.accept(event);
                        onRightClickAir.accept(event);
                    }
                    case RIGHT_CLICK_BLOCK -> {
                        onRightClick.accept(event);
                        onRightClickBlock.accept(event);
                    }
                    case LEFT_CLICK_AIR -> {
                        onLeftClick.accept(event);
                        onLeftClickAir.accept(event);
                    }
                    case LEFT_CLICK_BLOCK -> {
                        onLeftClick.accept(event);
                        onLeftClickBlock.accept(event);
                    }
                }
            }
        }
        
    }
    
    
}
