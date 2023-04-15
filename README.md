# MCTManager

This plugin manages the logic and games for Minecraft Tournament (MCT). 

Check out [MCT Official on YouTube](https://www.youtube.com/channel/UCDHWFMl0D8vREh7aKzJjzow)!

## Authors
- This plugin was programmed by Braekpo1nt. Find him on [YouTube](https://www.youtube.com/@braekpo1nt), [twitter](https://twitter.com/braekpo1nt), [facebook](https://www.facebook.com/Braekpo1nt/), or [instagram](https://www.instagram.com/braekpo1nt/).
- The event this plugin supports was created by SgtShotgun. Find them on [YouTube](https://www.youtube.com/@SgtShotgun) and [twitter](https://twitter.com/SgtShotgun1) 

## Discord
Ask the authors for access to the discord

# Dependencies
Dependencies that are require to run this plugin

## Data Packs
As of release `v0.3.0-alpha`, this plugin expects the following data packs in the server's `world/datapacks/` directory:
- [mctdatapack](https://github.com/Braekpo1nt/mctdatapack)

## Plugins
Required plugins. Read this or you will get errors. 

- This plugin depends on [Multiverse-Core v4.3.1](https://github.com/Multiverse/Multiverse-Core/releases/tag/v4.3.1)
- This plugin softdepends on [LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/) or any permissions manager (only tested with LuckPerms)
  - You will need to give all players the `mv.bypass.gamemode.*`, as this is the only way to avoid Multiverse-Core forcing gamemodes on your players during your games. 
    - If you don't give this permission to all participants, they will be able to respawn by disconnecting and reconnecting, and will often be in the wrong game mode when travelling worlds. 

# Commands

See the [commands reference](./docs/commands.md) for all commands and what they do.

# Running the event

### Starting a game
You can start a game with the following command:

- [/mct game start <game>](./docs/commands.md#mct-game)

You can also start a game with a [vote](#voting)

### Stopping a game
If a game is running, you can manually stop a game with the following command:

- [/mct game stop [true|false]](./docs/commands.md#mct-game)

### Voting

You can initiate a vote for all online participants. You must specify the games you want to be voted for (must be at least one):

- [/mct game vote [one or more games]](./docs/commands.md#mct-game)

### Adding a new team
Every participant must be on a team, so you must first have at least one team. To add a new team, use the [/mct team](./docs/commands.md#mct-team) command.

### Removing a team
You can remove a team entirely using the [/mct team](./docs/commands.md#mct-team) command. Points are lost and team members are removed, also losing their points, as if you [removed them manually](#removing-a-participant)


## Participants
In order to host an event, you need to [add participants](#adding-a-new-participant) (the plugin has to know who is a participant and who is an admin).

### Adding a new participant
Participants must be on a team, so you must first [add a new team](#adding-a-new-team)

To add a new participant, use the [/mct team](./docs/commands.md#mct-team) command.

If you add a new participant while a game is going on, they will be sent to that game as a participant. 

### Removing a participant
The only way to remove a participant is to leave them from their team using the [/mct team](./docs/commands.md#mct-team) command.

### Listing teams and scores

You can list the teams, their participants, and their scores with the [/mct team](./docs/commands.md#mct-team) command.

### Modifying scores

You can add, subtract, or set the scores of players and teams with the [/mct team](./docs/commands.md#mct-team) command.

# Development/Contributions

To participate, reach out to Braekpo1nt on [YouTube](https://www.youtube.com/@braekpo1nt), [twitter](https://twitter.com/braekpo1nt), [facebook](https://www.facebook.com/Braekpo1nt/), or [instagram](https://www.instagram.com/braekpo1nt/). 

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


