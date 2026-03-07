package org.braekpo1nt.mctmanager.commands.manager.brigadier;

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
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("test")
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("test"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void doubleNodeTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void tripleNodeTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                        .then(PermissionedWrapper.literal("ref"))
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.bar.ref");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void doubleNodeBranchTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                )
                .then(PermissionedWrapper.literal("ref"))
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.ref");
        assertThat(command.getChildren()).hasSize(2);
    }
    
    @Test
    void tripleNodeBranchTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                        .then(PermissionedWrapper.literal("pan"))
                )
                .then(PermissionedWrapper.literal("ref"))
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.ref", "foo.bar.pan");
        assertThat(command.getChildren()).hasSize(2);
    }
    
    @Test
    void doubleNodeMixedTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(Commands.literal("bar")
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void tripleNodeMixedTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
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
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
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
}
