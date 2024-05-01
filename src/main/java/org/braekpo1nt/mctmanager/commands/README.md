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
    - <command>/
      - <sub-command-class>.java
    - <command-manager-class>.java
    - <sub-commmand-class>.java
```

For example, let's say your command manager is `/example`, so all commands related to that feature are in the form `/example <options...>`. `plugin.yml` will be:

```yml
#...
commands:
  example:
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
    - ExampleCommandManager.java
    - foo/
      - FooCommand.java
    - bar/
      - BarCommand.java
    - action/
      - ActionCommand.java
      - JumpSubCommand.java
      - RollSubCommand.java
```

This way, I know that if there is a problem with the `jump` option in `/example action jump <height>`, I know to go to the `commands/example/action/JumpSubCommand.java`

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
        this.subCommands.put("<subcommand>", new MySubCommand());
    }
    ```
  where `<subcommand>` is the name of the command you want. This will be shown on tab completion, and you will call it with `/mct <subcommand>`

- If your class is a `TabExecutor`, then implement the `onTabComplete()` method as you would any other `TabExecutor`, and that code will be called as you continue tab completing.