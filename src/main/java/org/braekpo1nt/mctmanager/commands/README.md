# MCTCommand
[MCTCommand](./MCTCommand.java) is a super command with other sub commands. You run the command like so:
```mclang
/mct <sub command> [<options>]
```

## Making a new sub command
To make a new sub command that can be in the `<sub command>` position for `/mct <sub command>`, follow this proceedure:
- Make a new Java class that implements the `CommandExecutor` or one of its sub classes:

    ```java
    import org.bukkit.command.CommandExecutor;
    
    public class MySubCommand implements CommandExecutor {
        // Normal CommandExecutor code
    }
    ```

- Add `MySubCommand` to the [MCTCommand](./MCTCommand.java)'s `subCommands` map
    ```java
    public MCTCommand(...args...) {
        //other subcommands and code
        this.subCommands.put("<subcommand>", new MySubCommand());
    }
    ```
  where `<subcommand>` is the name of the command you want. This will be shown on tab completion, and you will call it with `/mct <subcommand>`

- If your class is a `TabExecutor`, then implement the `onTabComplete()` method as you would any other `TabExecutor`, and that code will be called as you continue tab completing.