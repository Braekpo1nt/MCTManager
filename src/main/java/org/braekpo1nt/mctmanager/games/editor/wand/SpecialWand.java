package org.braekpo1nt.mctmanager.games.editor.wand;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class SpecialWand<T extends Audience> {
    
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
    
    public SpecialWand(@NotNull ItemStack wandItem) {
        this.wandItem = wandItem;
    }
    
    public SpecialWand(@NotNull Material material, @NotNull String displayName, @NotNull List<Component> lore) {
        this.wandItem = Wand.createWandItem(material, displayName, lore);
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
        event.setUseItemInHand(Event.Result.DENY);
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
    
    public SpecialWand<T> onInteract(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onInteract) {
        this.onInteract = onInteract;
        return this;
    }
    
    public SpecialWand<T> onRightClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClick) {
        this.onRightClick = onRightClick;
        return this;
    }
    
    public SpecialWand<T> onRightClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickAir) {
        this.onRightClickAir = onRightClickAir;
        return this;
    }
    
    public SpecialWand<T> onRightClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightClickBlock) {
        this.onRightClickBlock = onRightClickBlock;
        return this;
    }
    
    public SpecialWand<T> onRightSneakClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClick) {
        this.onRightSneakClick = onRightSneakClick;
        return this;
    }
    
    public SpecialWand<T> onRightSneakClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickAir) {
        this.onRightSneakClickAir = onRightSneakClickAir;
        return this;
    }
    
    public SpecialWand<T> onRightSneakClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onRightSneakClickBlock) {
        this.onRightSneakClickBlock = onRightSneakClickBlock;
        return this;
    }
    
    public SpecialWand<T> onLeftClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClick) {
        this.onLeftClick = onLeftClick;
        return this;
    }
    
    public SpecialWand<T> onLeftClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickAir) {
        this.onLeftClickAir = onLeftClickAir;
        return this;
    }
    
    public SpecialWand<T> onLeftClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftClickBlock) {
        this.onLeftClickBlock = onLeftClickBlock;
        return this;
    }
    
    public SpecialWand<T> onLeftSneakClick(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClick) {
        this.onLeftSneakClick = onLeftSneakClick;
        return this;
    }
    
    public SpecialWand<T> onLeftSneakClickAir(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickAir) {
        this.onLeftSneakClickAir = onLeftSneakClickAir;
        return this;
    }
    
    public SpecialWand<T> onLeftSneakClickBlock(@NotNull BiFunction<PlayerInteractEvent, T, CommandResult> onLeftSneakClickBlock) {
        this.onLeftSneakClickBlock = onLeftSneakClickBlock;
        return this;
    }
}
