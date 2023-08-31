package org.braekpo1nt.mctmanager.games.game.footrace.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.logging.Level;

class FootRaceStorageUtilTest {
    
    String validConfigFile = "validFootRaceConfig.json";
    String invalidConfigFile = "invalidFootRaceConfig.json";
    String configFileName = "footRaceConfig.json";
    Main plugin;
    FootRaceStorageUtil storageUtil;
    private ServerMock server;
    
    @BeforeEach
    void setupServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new FootRaceStorageUtil(plugin.getDataFolder());
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void configDoesNotExist() {
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
    
    @Test
    void malformedJson() {
        TestUtils.createFileInDirectory(plugin.getDataFolder(), configFileName, "{,");
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
    
    @Test
    void wellFormedJsonValidData() {
        InputStream inputStream = getClass().getResourceAsStream(validConfigFile);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertTrue(storageUtil.loadConfig());
    }
    
    @Test
    void wellFormedJsonInvalidData() {
        InputStream inputStream = getClass().getResourceAsStream(invalidConfigFile);
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), configFileName));
        Assertions.assertThrows(IllegalArgumentException.class, storageUtil::loadConfig);
    }
    
    @Test
    void tellraw() {
        String tellrawString = "{\"text\":\"scores have been fixed\",\"bold\":true,\"color\":\"green\"}";
        Component componentFromTellraw = GsonComponentSerializer.gson().deserialize(tellrawString);
        server.getConsoleSender().sendMessage(componentFromTellraw);
    
        String json = GsonComponentSerializer.gson().serialize(Component.text("Test one")
                .append(Component.text(" with sub")
                        .decorate(TextDecoration.BOLD))
                .color(NamedTextColor.YELLOW));
        Component componentFromJson = GsonComponentSerializer.gson().deserialize(json);
        server.getConsoleSender().sendMessage(componentFromJson);
    }
    
    @Test
    void componentTellraw() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("config.json");
        File configFile = new File(plugin.getDataFolder(), "config.json");
        TestUtils.copyInputStreamToFile(inputStream, configFile);
        Reader reader = new FileReader(configFile);
        Gson gson = new Gson();
        Config newConfig = gson.fromJson(reader, Config.class);
        reader.close();
        server.getConsoleSender().sendMessage(newConfig.getTellrawComponent());
    }
    
    static class Config {
        
        @JsonAdapter(ComponentTypeAdapter.class) // Use the custom TypeAdapter for this field
        private Component tellrawComponent;
        
        public Component getTellrawComponent() {
            return tellrawComponent;
        }
        
        public void setTellrawComponent(Component tellrawComponent) {
            this.tellrawComponent = tellrawComponent;
        }
    }
    
    static class ComponentTypeAdapter extends TypeAdapter<Component> {
        
        @Override
        public void write(JsonWriter out, Component value) throws IOException {
            throw new UnsupportedOperationException("Serializing components not supported");
        }
        
        @Override
        public Component read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            
            String json = in.nextString();
            return GsonComponentSerializer.gson().deserialize(json);
        }
        
        
    }
    
}
