package org.braekpo1nt.mctmanager.games.editor.wand;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("UnusedReturnValue")
public class Wand<T extends Audience> {
    
    /**
     * Used to retrieve the persistent data of a given wand's UUID
     */
    public static final NamespacedKey WAND_ID = new NamespacedKey("mctmanager", "wand_uuid");
    
    protected final @NotNull String uuid;
    
    @Getter
    protected final @NotNull ItemStack wandItem;
    
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onInteract;
    
    /**
     * Called on every {@link Action#isRightClick()}
     */
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClick;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickAir;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickBlock;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClick;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickAir;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickBlock;
    
    /**
     * Called on every {@link Action#isLeftClick()}
     */
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClick;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickAir;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickBlock;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClick;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickAir;
    private final @NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickBlock;
    
    /**
     * Called on every {@link #onHoldTick(PlayerInventory, Audience)}
     */
    private final @NotNull Function<T, CommandResult> onHoldTick;
    
    /**
     * True if you're allowed to drop the item, false if you are not allowed to drop the item
     */
    private final boolean shouldNotDrop;
    
    @Builder
    public Wand(
            @NotNull ItemStack wandItem,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onInteract,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightClick,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickAir,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickBlock,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClick,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickAir,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickBlock,
            
            @Nullable  BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClick,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickAir,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickBlock,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClick,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickAir,
            @Nullable BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickBlock,
            
            @Nullable Function<T, CommandResult> onHoldTick,
            boolean shouldNotDrop
            ) {
        this.wandItem = Objects.requireNonNull(wandItem, "wandItem can't be null");
        this.uuid = UUID.randomUUID().toString();
        this.wandItem.editMeta(meta -> meta.getPersistentDataContainer().set(WAND_ID, PersistentDataType.STRING, uuid));
        this.onInteract = (onInteract != null) ? onInteract : (event, user) -> CommandResult.success();
        this.onRightClick = (onRightClick != null) ? onRightClick : (event, user) -> CommandResult.success();
        this.onRightClickAir = (onRightClickAir != null) ? onRightClickAir : (event, user) -> CommandResult.success();
        this.onRightClickBlock = (onRightClickBlock != null) ? onRightClickBlock : (event, user) -> CommandResult.success();
        this.onRightSneakClick = (onRightSneakClick != null) ? onRightSneakClick : (event, user) -> CommandResult.success();
        this.onRightSneakClickAir = (onRightSneakClickAir != null) ? onRightSneakClickAir : (event, user) -> CommandResult.success();
        this.onRightSneakClickBlock = (onRightSneakClickBlock != null) ? onRightSneakClickBlock : (event, user) -> CommandResult.success();
        
        this.onLeftClick = (onLeftClick != null) ? onLeftClick : (event, user) -> CommandResult.success();
        this.onLeftClickAir = (onLeftClickAir != null) ? onLeftClickAir : (event, user) -> CommandResult.success();
        this.onLeftClickBlock = (onLeftClickBlock != null) ? onLeftClickBlock : (event, user) -> CommandResult.success();
        this.onLeftSneakClick = (onLeftSneakClick != null) ? onLeftSneakClick : (event, user) -> CommandResult.success();
        this.onLeftSneakClickAir = (onLeftSneakClickAir != null) ? onLeftSneakClickAir : (event, user) -> CommandResult.success();
        this.onLeftSneakClickBlock = (onLeftSneakClickBlock != null) ? onLeftSneakClickBlock : (event, user) -> CommandResult.success();
        
        this.onHoldTick = (onHoldTick != null) ? onHoldTick : (user) -> CommandResult.success();
        this.shouldNotDrop = shouldNotDrop;
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
    
    @Contract("null -> false")
    public boolean isWandItem(@Nullable ItemStack item) {
        return item != null
                && item.getType().equals(wandItem.getType())
                && Objects.equals(
                        item.getItemMeta().getPersistentDataContainer().get(WAND_ID, PersistentDataType.STRING), 
                        uuid
                );
    }
    
    /**
     * @return True if you're allowed to drop the item, false if you are not allowed to drop the item
     */
    public boolean shouldNotDrop() {
        return shouldNotDrop;
    }
    
    public void onPlayerInteract(@NotNull PlayerInteractEvent event, @NotNull T user) {
        ItemStack usedItem = event.getItem();
        if (!isWandItem(usedItem)) {
            return;
        }
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        List<CommandResult> results = new ArrayList<>();
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
        event.setCancelled(true);
        CommandResult result = CompositeCommandResult.all(results);
        Component message = result.getMessage();
        if (message == null) {
            return;
        }
        user.sendMessage(message);
    }
    
    /**
     * Called every X ticks by the controlling context when a given user is holding this wand.
     * X is determined by the controlling context.
     * If the item in {@link PlayerInventory#getItemInMainHand()} is this {@link Wand}'s {@link Wand#isWandItem(ItemStack)},
     * then {@link #onHoldTick} is called. Otherwise, nothing happens.
     * @param user the user to check if they are holding this wand, and if they are perform the {@link #onHoldTick} action
     */
    public void onHoldTick(@NotNull PlayerInventory userInventory, @NotNull T user) {
        ItemStack heldItem = userInventory.getItemInMainHand();
        if (!isWandItem(heldItem)) {
            return;
        }
        CommandResult result = onHoldTick.apply(user);
        Component message = result.getMessage();
        if (message == null) {
            return;
        }
        user.sendMessage(message);
    }
}
