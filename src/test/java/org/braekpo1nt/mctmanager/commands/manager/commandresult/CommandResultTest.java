package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

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
    
}