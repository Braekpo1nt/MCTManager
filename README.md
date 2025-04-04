# MCTManager

This plugin manages the logic and games for Minecraft Tournament (MCT). 

Check out [MCT Official on YouTube](https://www.youtube.com/channel/UCDHWFMl0D8vREh7aKzJjzow)!

## Documentation
Comprehensive documentation for this plugin can be found [here](https://braekpo1nt.github.io/mctmanager-docs/).

## Authors
- This plugin was programmed by Braekpo1nt. Find him on [YouTube](https://www.youtube.com/@braekpo1nt), [twitter](https://twitter.com/braekpo1nt), [facebook](https://www.facebook.com/Braekpo1nt/), or [instagram](https://www.instagram.com/braekpo1nt/).
- The event this plugin supports was created by SgtShotgun. Find them on [YouTube](https://www.youtube.com/@SgtShotgun) and [twitter](https://twitter.com/SgtShotgun1) 

## Discord
The discord for [Challenger Trials](https://discord.gg/Qgs4a8r5UE) is where the developer community for this plugin communicate, and where the plugin is primarily used. 

# Dependencies
Dependencies that are require to run this plugin

## Data Packs
As of release `v0.3.0-alpha`, this plugin expects the following data packs in the server's `world/datapacks/` directory:
- [mctdatapack](https://github.com/Braekpo1nt/mctdatapack)

## Plugins
Required plugins. Read this or you will get errors. 

- Various UI features, including friendly glowing and hearts under name tags, depends on [PacketEvents](https://github.com/retrooper/packetevents) API. This is a hard dependency at the time of writing. Make sure the Bukkit/Spigot/Paper edition of the PacketEvents plugin is added to your server (whatever version of Minecraft you're running should have a matching version).
- This plugin softdepends on [LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/) or any permissions manager (only tested with LuckPerms)
- If you use [Multiverse-Core v4.3.1](https://github.com/Multiverse/Multiverse-Core/releases/tag/v4.3.1) in your server, You will need to give all players the `mv.bypass.gamemode.*`, as this is the only way to avoid Multiverse-Core forcing gamemodes on your players during your games. 
  - If you don't give this permission to all participants, they will be able to respawn by disconnecting and reconnecting, and will often be in the wrong game mode when being teleported to different worlds. 
- The leaderboard holograms depends on [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-1-papi-support-no-dependencies.96927/), another soft dependency

# Adding to your server
Download the latest official build from the [releases](https://github.com/Braekpo1nt/MCTManager/releases) page and add it to your Minecraft Paper server's `plugins/` directory.

# Minecraft Game Rules
|Rule|Value|
|---|---|
|`doImmidateRespawn`|`true`|


# Development/Contributions

To participate, reach out to Braekpo1nt on [YouTube](https://www.youtube.com/@braekpo1nt), [twitter](https://twitter.com/braekpo1nt), [facebook](https://www.facebook.com/Braekpo1nt/), or [instagram](https://www.instagram.com/braekpo1nt/).

## **Important note about building the project:**

### Gradle Wrapper
This project includes a [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). If you don't know what that means, all you need to know is that instead of running `gradle build`, you should run `./gradlew build`. You should also configure your IDE (e.g. IntelliJ) to [use the project's gradle wrapper](https://www.jetbrains.com/idea/guide/tutorials/working-with-gradle/gradle-wrapper/). 

A gradle wrapper essentially just makes it so that everyone who is developing on the project is using the same exact version of Gradle. 

### Building the plugin
You can build the plugin yourself with:

```
./gradlew build
```

and add the jar from the `<root>/build/libs` directory to your minecraft Paper server's `plugins/` directory. 




