package org.braekpo1nt.mctmanager.games.editor.wand;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("UnusedReturnValue")
public class Wand<T extends Audience> {
    
    @Getter
    protected final @NotNull ItemStack wandItem;
    
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onInteract = (event, user) -> CommandResult.success();
    
    /**
     * Called on every {@link Action#isRightClick()}
     */
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClick = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickAir = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickBlock = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClick = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickAir = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickBlock = (event, user) -> CommandResult.success();
    
    /**
     * Called on every {@link Action#isLeftClick()}
     */
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClick = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickAir = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickBlock = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClick = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickAir = (event, user) -> CommandResult.success();
    private @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickBlock = (event, user) -> CommandResult.success();
    
    private @NotNull BiFunction<PlayerDropItemEvent, T, CommandResult> onDrop = (event, user) -> CommandResult.success();
    
    public Wand(@NotNull ItemStack wandItem) {
        this.wandItem = wandItem;
    }
    
    public Wand(@NotNull Material material, @NotNull String displayName, @NotNull List<Component> lore) {
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
    
    public void onPlayerInteract(@NotNull PlayerInteractEvent event, @NotNull T user) {
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
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        List<CommandResult> results = new ArrayList<>();
        event.setCancelled(true);
        results.add(onInteract.apply(event, user));
        Action action = event.getAction();
        if (event.getPlayer().isSneaking()) {
            switch (action) {
                case RIGHT_CLICK_AIR -> {
                    results.add(onRightSneakClick.apply(event, user));
                    results.add(onRightSneakClickAir.apply(event, user));
                }
                case RIGHT_CLICK_BLOCK -> {
                    results.add(onRightSneakClick.apply(event, user));
                    results.add(onRightSneakClickBlock.apply(event, user));
                }
                case LEFT_CLICK_AIR -> {
                    results.add(onLeftSneakClick.apply(event, user));
                    results.add(onLeftSneakClickAir.apply(event, user));
                }
                case LEFT_CLICK_BLOCK -> {
                    results.add(onLeftSneakClick.apply(event, user));
                    results.add(onLeftSneakClickBlock.apply(event, user));
                }
            }
        } else {
            switch (action) {
                case RIGHT_CLICK_AIR -> {
                    results.add(onRightClick.apply(event, user));
                    results.add(onRightClickAir.apply(event, user));
                }
                case RIGHT_CLICK_BLOCK -> {
                    results.add(onRightClick.apply(event, user));
                    results.add(onRightClickBlock.apply(event, user));
                }
                case LEFT_CLICK_AIR -> {
                    results.add(onLeftClick.apply(event, user));
                    results.add(onLeftClickAir.apply(event, user));
                }
                case LEFT_CLICK_BLOCK -> {
                    results.add(onLeftClick.apply(event, user));
                    results.add(onLeftClickBlock.apply(event, user));
                }
            }
        }
        CommandResult result = CompositeCommandResult.all(results);
        Component message = result.getMessage();
        if (message == null) {
            return;
        }
        user.sendMessage(message);
    }
    
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event, @NotNull T user) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (!droppedItem.getType().equals(wandItem.getType())) {
            return;
        }
        if (!droppedItem.getItemMeta().equals(wandItem.getItemMeta())) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(true);
        CommandResult result = onDrop.apply(event, user);
        Component message = result.getMessage();
        if (message == null) {
            return;
        }
        user.sendMessage(message);
    }
    
    public Wand<T> onInteract(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onInteract) {
        this.onInteract = onInteract;
        return this;
    }
    
    public Wand<T> onRightClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClick) {
        this.onRightClick = onRightClick;
        return this;
    }
    
    public Wand<T> onRightClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickAir) {
        this.onRightClickAir = onRightClickAir;
        return this;
    }
    
    public Wand<T> onRightClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickBlock) {
        this.onRightClickBlock = onRightClickBlock;
        return this;
    }
    
    public Wand<T> onRightSneakClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClick) {
        this.onRightSneakClick = onRightSneakClick;
        return this;
    }
    
    public Wand<T> onRightSneakClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickAir) {
        this.onRightSneakClickAir = onRightSneakClickAir;
        return this;
    }
    
    public Wand<T> onRightSneakClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickBlock) {
        this.onRightSneakClickBlock = onRightSneakClickBlock;
        return this;
    }
    
    public Wand<T> onLeftClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClick) {
        this.onLeftClick = onLeftClick;
        return this;
    }
    
    public Wand<T> onLeftClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickAir) {
        this.onLeftClickAir = onLeftClickAir;
        return this;
    }
    
    public Wand<T> onLeftClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickBlock) {
        this.onLeftClickBlock = onLeftClickBlock;
        return this;
    }
    
    public Wand<T> onLeftSneakClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClick) {
        this.onLeftSneakClick = onLeftSneakClick;
        return this;
    }
    
    public Wand<T> onLeftSneakClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickAir) {
        this.onLeftSneakClickAir = onLeftSneakClickAir;
        return this;
    }
    
    public Wand<T> onLeftSneakClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickBlock) {
        this.onLeftSneakClickBlock = onLeftSneakClickBlock;
        return this;
    }
    
    public Wand<T> onDrop(@NotNull BiFunction<PlayerDropItemEvent, T, CommandResult> onDrop) {
        this.onDrop = onDrop;
        return this;
    }
}
