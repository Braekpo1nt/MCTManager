package org.braekpo1nt.mctmanager.games.editor.wand;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Setter
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
    
    public CommandResult onPlayerInteract(@NotNull PlayerInteractEvent event, @NotNull T user) {
        ItemStack usedItem = event.getItem();
        if (usedItem == null) {
            return null;
        }
        if (!usedItem.getType().equals(wandItem.getType())) {
            return null;
        }
        if (!usedItem.getItemMeta().equals(wandItem.getItemMeta())) {
            return null;
        }
        if (event.useItemInHand() == Event.Result.DENY) {
            return null;
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
            return null;
        }
        user.sendMessage(message);
        return result;
    }
    
}
