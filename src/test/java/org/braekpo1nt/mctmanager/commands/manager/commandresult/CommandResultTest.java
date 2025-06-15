package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.braekpo1nt.mctmanager.games.editor.wand.SpecialWand;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandResultTest {
    
    @Test
    void successEmpty() {
        CommandResult result = CommandResult.success();
        Component message = result.getMessage();
        Assertions.assertNull(message);
    }
    
    @Test
    void compositeEmpty() {
        CommandResult result = CompositeCommandResult.all(Collections.emptyList());
        Component message = result.getMessage();
        Assertions.assertNull(message);
    }
    
    @Test
    void compositeSingle() {
        CommandResult result = CompositeCommandResult.all(List.of(CommandResult.success(Component.text("First line"))));
        Component message = result.getMessage();
        Assertions.assertNotNull(message);
        String serialize = PlainTextComponentSerializer.plainText().serialize(message);
        Assertions.assertFalse(serialize.contains("\n"));
    }
    
    @Test
    void compositeSingleSuccess() {
        CommandResult result = CompositeCommandResult.all(List.of(
                CommandResult.success()
        ));
        Component message = result.getMessage();
        Assertions.assertNull(message);
    }
    
    @Test
    void compositeTwo() {
        CommandResult result = CompositeCommandResult.all(List.of(
                CommandResult.success(Component.text("First line")),
                CommandResult.success(Component.text("Second line"))
        ));
        Component message = result.getMessage();
        Assertions.assertNotNull(message);
        String serialize = PlainTextComponentSerializer.plainText().serialize(message);
        Assertions.assertTrue(serialize.contains("\n"));
    }
    
    @Test
    void compositeTwoSecondNull() {
        CommandResult result = CompositeCommandResult.all(List.of(
                CommandResult.success(Component.text("First line")),
                CommandResult.success()
        ));
        Component message = result.getMessage();
        Assertions.assertNotNull(message);
        String serialize = PlainTextComponentSerializer.plainText().serialize(message);
        Assertions.assertFalse(serialize.contains("\n"));
    }
    
    @Test
    void specialWand() {
        ItemStack wandItem = Wand.createWandItem(Material.STICK, "None", Collections.emptyList());
        SpecialWand<Player> wand = new SpecialWand<>(wandItem);
        wand.setOnLeftClick((event, player) -> {
            return CommandResult.success(Component.text("Clicked block"));
        });
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        Player player = mock(Player.class);
        when(event.getItem()).thenReturn(wandItem);
        when(event.getPlayer()).thenReturn(player);
        when(event.getAction()).thenReturn(Action.LEFT_CLICK_AIR);
        when(player.isSneaking()).thenReturn(false);
        CommandResult interactResult = wand.onPlayerInteract(event, player);
        Assertions.assertNotNull(interactResult);
        Component message = interactResult.getMessage();
        Assertions.assertNotNull(message);
        String serialize = PlainTextComponentSerializer.plainText().serialize(message);
        System.out.println(serialize);
        Assertions.assertFalse(serialize.contains("\n"));
    }
    
}