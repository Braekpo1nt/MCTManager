# MCTCommand
[MCTCommand](./MCTCommand.java) is a super command with other sub commands. You run the command like so:
```mclang
/mct <sub command> [<options>]
```

## Directory Structure
Please adhere to the following directory structure style:
```
- commands/
    - <command-manager>/
        - <comand-manager>.java
        - <command-manager-1>/
            - <command-manager-1>.java # implements CommandManager
            - <sub-command-1.1>.java # implements SubCommand
            - <sub-command-1.2>.java # implements SubCommand
        - <command-manager-2>/
            - <command-manager-2>.java # implements CommandManager
            - <sub-command-2.1>.java # implements SubCommand
            - <sub-command-2.2>.java # implements TabSubCommand
            - <command-manager-3>/
                - <command-manager-3>.java
                - <sub-command-3.1>.java
                - <sub-command-3.2>.java
```

For example, let's say your command manager is `/example`, and the usage is `/example <foo|bar|action>`. `plugin.yml` will be:

```yml
#...
commands:
  example: # the name of the command for /example
    description: example description
#...
```

Your super-command (`CommandManager` implementation), `/example`, has a few sub commands and arguments, such as:
- `/example foo <opt1> <opt2>`
- `/example bar <num>`
- `/example action jump <height>`
- `/example action roll <distance>`

Conceptually, for each command in `/example <command>`, there should be a directory. Thus, your directory structure should look like this:

```
- commands/
    - example/
        - ExampleCommand.java
        - FooSubCommand.java
        - BarSubCommand.java
        - action/
            - ActionCommand.java
            - JumpSubCommand.java
            - RollSubCommand.java
```

This way, I know that if there is a problem with the `jump` option in `/example action jump <height>`, I know to go to the `commands/example/action/JumpSubCommand.java` class.

## Making a new sub command
To make a new sub command that can be in the `<sub command>` position for `/mct <sub command>`, follow this procedure:
- Make a new Java class that implements the `CommandExecutor` or one of its subclasses:

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
        addSubCommand(new MySubCommand("<subcommand>"));
    }
    ```
  where `<subcommand>` is the name of the command you want. This will be shown on tab completion, and you will call it with `/mct <subcommand>`

- If your class is a `TabExecutor`, then implement the `onTabComplete()` method as you would any other `TabExecutor`, and that code will be called as you continue tab completing.