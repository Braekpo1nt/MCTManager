# Dependencies
Dependencies that are require to run this plugin

## Data Packs
As of release `v0.1.1-alpha`, this plugin expects the following data packs in the server's `world/datapacks/` directory:
- [mctdatapack](https://github.com/Braekpo1nt/mctdatapack)

## Plugins
- This plugin depends on [Multiverse-Core v4.3.1](https://github.com/Multiverse/Multiverse-Core/releases/tag/v4.3.1)


# Running the event

## Participants
In order to host an event, you need to [add participants](#adding-a-new-participant) (the plugin has to know who is a participant and who is an admin).

### Starting a game
You can start a game with the following command:

- `/mct game start <game>`
  - `<game>` the game to start. Must be a valid game name in the event. See [list of games](#games-list)

### Stopping a game
If a game is running, you can manually stop a game with the following command:

- `/mct game stop`

This will stop the game, and return all players to the beginning. As of the time of writing this, the points for playing the game will be retained. 

### Adding a new team
Every participant must be on a team, so you must first have at least one team. To add a new team, use the following command:

- `/mct team add <team> <displayname> <color>`
    - `<team>` the internal name of the team (All lowercase)
    - `<displayname>` the display name of the team
    - `<color>` the color of the team



### Adding a new participant
Participants must be on a team, so you must first [add a new team](#adding-a-new-team)

To add a new participant, use the following command:

- `/mct team join <team> <member>`
  - `<team>` the internal name of the team. The team must already exist.
  - `<member>` the name of the player to add. Must be an online player.

## Games List
This is a list of the currently implemented games. See above for how to [start a game](#starting-a-game)

- Foot Race
  - Start game with `/mct startgame foot-race`


# Development/Contributions

**Important note about building the project:**
The dependency `fr.mrmicky:fastboard:1.2.1` from [FastBoard](https://github.com/MrMicky-FR/FastBoard) needs to be included in the jar file with *Shadow Jar*. 

To do this, simply run `gradle shadowJar` instead of `gradle build`. Otherwise, you'll get an error like the following:
```java
java.lang.NoClassDefFoundError: fr/mrmicky/fastboard/FastBoard
at org.braekpo1nt.mctmanager.Main.onEnable(Main.java:45) ~[MCTManager-0.1.0.jar:?]
at org.bukkit.plugin.java.JavaPlugin.setEnabled(JavaPlugin.java:264) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.plugin.java.JavaPluginLoader.enablePlugin(JavaPluginLoader.java:371) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.plugin.SimplePluginManager.enablePlugin(SimplePluginManager.java:544) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.craftbukkit.v1_19_R2.CraftServer.enablePlugin(CraftServer.java:578) ~[paper-1.19.3.jar:git-Paper-384]
at org.bukkit.craftbukkit.v1_19_R2.CraftServer.enablePlugins(CraftServer.java:492) ~[paper-1.19.3.jar:git-Paper-384]
at org.bukkit.craftbukkit.v1_19_R2.CraftServer.reload(CraftServer.java:1038) ~[paper-1.19.3.jar:git-Paper-384]
at org.bukkit.Bukkit.reload(Bukkit.java:930) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.command.defaults.ReloadCommand.execute(ReloadCommand.java:54) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.command.SimpleCommandMap.dispatch(SimpleCommandMap.java:155) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.craftbukkit.v1_19_R2.CraftServer.dispatchCommand(CraftServer.java:929) ~[paper-1.19.3.jar:git-Paper-384]
at org.bukkit.craftbukkit.v1_19_R2.CraftServer.dispatchServerCommand(CraftServer.java:892) ~[paper-1.19.3.jar:git-Paper-384]
at net.minecraft.server.dedicated.DedicatedServer.handleConsoleInputs(DedicatedServer.java:494) ~[paper-1.19.3.jar:git-Paper-384]
at net.minecraft.server.dedicated.DedicatedServer.tickChildren(DedicatedServer.java:441) ~[paper-1.19.3.jar:git-Paper-384]
at net.minecraft.server.MinecraftServer.tickServer(MinecraftServer.java:1397) ~[paper-1.19.3.jar:git-Paper-384]
at net.minecraft.server.MinecraftServer.runServer(MinecraftServer.java:1173) ~[paper-1.19.3.jar:git-Paper-384]
at net.minecraft.server.MinecraftServer.lambda$spin$0(MinecraftServer.java:316) ~[paper-1.19.3.jar:git-Paper-384]
at java.lang.Thread.run(Thread.java:833) ~[?:?]
Caused by: java.lang.ClassNotFoundException: fr.mrmicky.fastboard.FastBoard
at org.bukkit.plugin.java.PluginClassLoader.loadClass0(PluginClassLoader.java:177) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at org.bukkit.plugin.java.PluginClassLoader.loadClass(PluginClassLoader.java:124) ~[paper-api-1.19.3-R0.1-SNAPSHOT.jar:?]
at java.lang.ClassLoader.loadClass(ClassLoader.java:520) ~[?:?]
... 18 more
```


