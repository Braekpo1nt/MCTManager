package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyPlayerMock extends PlayerMock {
    
    public MyPlayerMock(@NotNull ServerMock server, @NotNull String name) {
        super(server, name);
    }
    
    public MyPlayerMock(@NotNull ServerMock server, @NotNull String name, @NotNull UUID uuid) {
        super(server, name, uuid);
    }
    
    @Override
    public void lookAt(double x, double y, double z, @NotNull LookAnchor playerAnchor) {}
    
    /**
     * Asserts that the plaintext version of the next message sent to the player is equal to the 
     * expected message regardless of formatting.
     * @param expected The expected plaintext message
     */
    public void assertSaidPlaintext(@NotNull String expected) {
        Component comp = nextComponentMessage();
        if (comp == null) {
            Assertions.fail("No more messages were sent. Expected \"" + expected + "\"");
        } else {
            String plainText = TestUtils.toPlainText(comp);
            Assertions.assertEquals(expected, plainText);
        }
    }
    
    /**
     * Checks if the given message was sent to the player, ignoring formatting
     * This searches through all messages sent to the player by making calls to {@link PlayerMock#nextComponentMessage()} until it finds the expected message, or there are no more messages. This will re-send the messages to the player with use of the {@link PlayerMock#sendMessage(String)} method, in the appropriate order. 
     * @param expected The message to search for
     * @return True if the expected message was ever sent to the player, false if not
     */
    public boolean receivedMessagePlaintext(@NotNull String expected) {
        Component comp = nextComponentMessage();
        List<Component> sentMessages = new ArrayList<>();
        boolean messageWasSent = false;
        while (comp != null) {
            sentMessages.add(comp);
            String plainText = TestUtils.toPlainText(comp);
            if (plainText.equals(expected)) {
                messageWasSent = true;
            }
            comp = nextComponentMessage();
        }
        for (Component sentMessage : sentMessages) {
            sendMessage(sentMessage);
        }
        return messageWasSent;
    }
    
}
