package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

class BrigadierTest {
    
    PretendPluginManager pluginManager;
    
    @BeforeEach
    void beforeEach() {
        this.pluginManager = new PretendPluginManager();
    }
    
    @Test
    void singleNodeTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("test")
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("test"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void doubleNodeTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void tripleNodeTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                        .then(Permissioned.literal("ref"))
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.bar.ref");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void doubleNodeBranchTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                )
                .then(Permissioned.literal("ref"))
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.ref");
        assertThat(command.getChildren()).hasSize(2);
    }
    
    @Test
    void tripleNodeBranchTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                        .then(Permissioned.literal("pan"))
                )
                .then(Permissioned.literal("ref"))
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.ref", "foo.bar.pan");
        assertThat(command.getChildren()).hasSize(2);
    }
    
    @Test
    void doubleNodeMixedTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Commands.literal("bar")
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void tripleNodeMixedTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Commands.literal("bar")
                        .then(Commands.literal("ref"))
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet())
                .describedAs("accepts official argument types, but does not build their sub-permission nodes, only the first non-PermissionedWrapper")
                .containsExactlyInAnyOrder("foo", "foo.bar")
        ;
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void tripleNodeLastNormalTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                        .then(Commands.literal("ref"))
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet())
                .describedAs("accepts official argument types, but does not build their sub-permission nodes, only the first non-PermissionedWrapper")
                .containsExactlyInAnyOrder("foo", "foo.bar", "foo.bar.ref")
        ;
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void singleArgTest() {
        CommandNode<CommandSourceStack> command = Permissioned.argument("test", StringArgumentType.word())
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("test"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void singleNodeSingleArgTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.argument("bar", StringArgumentType.word()))
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("foo", "foo.bar"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void singleNodeSingleArgSingleNormalTest() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.argument("bar", StringArgumentType.word())
                        .then(Commands.argument("ref", StringArgumentType.word()))
                )
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("foo", "foo.bar", "foo.bar.ref"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void multiMixTest1() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("foo")
                .then(Permissioned.literal("bar")
                        .then(Permissioned.argument("tag", StringArgumentType.word())
                                .then(Commands.literal("ref")
                                        .then(Commands.literal("ignore"))
                                )
                                .then(Permissioned.literal("mes"))
                        )
                )
                .then(Permissioned.literal("car")
                        .executes(ctx -> Command.SINGLE_SUCCESS)
                )
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("foo", "foo.bar", "foo.car", "foo.bar.tag", "foo.bar.tag.ref", "foo.bar.tag.mes"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void onlyRootPermissioned() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("mct")
                .then(Commands.literal("foo"))
                .then(Commands.literal("bar")
                        .then(Commands.argument("ref", StringArgumentType.word()))
                )
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("mct", "mct.foo", "mct.bar"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void buildWithPluginManager() {
        CommandNode<CommandSourceStack> command = Permissioned.literal("test")
                .pluginManager(pluginManager)
                .build();
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("test"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void buildWithoutPluginManager() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            Permissioned.literal("test")
                    .build();
        });
    }
}
