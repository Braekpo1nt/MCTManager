package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import com.google.gson.*;
import net.kyori.adventure.text.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Takes in a Component with 1 or more children, and converts it to a plaintext string without formatting.
     * Assumes it is made up of TextComponents and empty components.
     * @param component The component to get the plaintext version of
     * @return The concatenation of the contents() of the TextComponent children that this component is made of. Null if the component is null
     */
    public static @Nullable String toPlainText(@Nullable Component component) {
        if (component == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        
        if (component instanceof TextComponent textComponent) {
            builder.append(textComponent.content());
        }
        else if (component instanceof TranslatableComponent) {
            for (Component arg : ((TranslatableComponent) component).args()) {
                builder.append(toPlainText(arg));
            }
        } else if (component instanceof ScoreComponent scoreComponent) {
            builder.append(scoreComponent.name());
        } else if (component instanceof SelectorComponent selectorComponent) {
            builder.append(selectorComponent.pattern());
        } else if (component instanceof KeybindComponent keybindComponent) {
            builder.append(keybindComponent.keybind());
        } else if (component instanceof NBTComponent<?, ?> nbtComponent) {
            builder.append(nbtComponent.nbtPath());
        }
        
        for (Component child : component.children()) {
            builder.append(toPlainText(child));
        }
        
        return builder.toString();
    }
    
    /**
     * Asserts that the given component's plaintext is equal to the expected string.
     * @param expected The plaintext string you're expecting
     * @param actual The actual component to check the plaintext of
     */
    public static void assertComponentPlaintextEquals(String expected, Component actual) {
        String actualString = toPlainText(actual);
        Assertions.assertEquals(expected, actualString);
    }
    
    public static boolean receivedMessagePlaintext(@NotNull ConsoleCommandSenderMock sender, @NotNull String expected) {
        Component comp = sender.nextComponentMessage();
        List<Component> sentMessages = new ArrayList<>();
        boolean messageWasSent = false;
        while (comp != null) {
            sentMessages.add(comp);
            String plainText = TestUtils.toPlainText(comp);
            if (plainText.equals(expected)) {
                messageWasSent = true;
            }
            comp = sender.nextComponentMessage();
        }
        for (Component sentMessage : sentMessages) {
            sender.sendMessage(sentMessage);
        }
        return messageWasSent;
    }

    public static void copyInputStreamToFile(InputStream inputStream, File destinationFile) {
        Assertions.assertNotNull(inputStream);
        try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Assertions.fail(String.format("Unable to copy stream to %s \n%s", destinationFile, e));
        }
    }

    public static void createFileInDirectory(File directory, String fileName, String fileContents) {
        File newFile = new File(directory, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(fileContents);
        } catch (IOException e) {
            Assertions.fail(String.format("Unable to create file %s in %s with contents %s", fileName, directory, fileContents));
        }
    }
    
    public static JsonObject inputStreamToJson(InputStream inputStream) {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return element.getAsJsonObject();
    }
    
    public static void saveJsonToFile(JsonObject json, File toFile) {
        // Write the modified JSON to a new file
        try (FileWriter writer = new FileWriter(toFile)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
