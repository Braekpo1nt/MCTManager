# Configuration Files (Config files)

This section describes how to use the various config files for MCTManager.

## Location

You can find the config files in the `<server>/plugins/MCTManager/` directory of your server. 

## Files
- [gameState.json](#gamestatejson)
- [mechaConfig.json](#mechaconfigjson)
- [parkourPathwayConfig.json](#parkourpathwayconfigjson)


### gameState.json

This is where the state of the game is saved. `gameState.json` keeps track of
- What teams exist, their names and colors
- Who the participants are and what team they are on
- Who the admins are
- What games have been played during an event

### mechaConfig.json

This is the config file for MECHA. It allows you to configure:
- The location of the spawn chests (the chests in the center of the map)
- The location of the map chests (the chests throughout the MECHA map)
- The spawn chest loot table
- The weighted loot tables for the map chests
- The stages of the border

#### Example:

This is an example of a `mechaConfig.json` file (elipses `...` mean that you can add as many as you want in the list)
```json
{
  "spawnChestCoords": [
    {
      "x": -1.0,
      "y": -45.0,
      "z": 1.0
    },
    ...
  ],
  "mapChestCoords": [
    {
      "x": -18.0,
      "y": -45.0,
      "z": -15.0
    },
    ...
  ],
  "spawnLootTable": {
    "namespace": "mctdatapack",
    "key": "mecha/spawn-chest"
  },
  "weightedMechaLootTables": [
    {
      "namespace": "mctdatapack",
      "key": "mecha/better-chest",
      "weight": 2
    },
    ...
    }
  ],
  "borderStages": [
    {
      "size": 180,
      "delay": 90,
      "duration": 25
    },
    ...
  ]
}
```

- `"spawnChestCoords"` is a list of `x,y,z` coordinates indicating the locations of the spawn chests. 
  - Spawn chests will be filled at the start of the game and emptied at the end of the game
  - Spawn chests will be given loot according to the `"spawnLootTable"` specified in the config (see below)
- `"mapChestCoords"` is a list of `x,y,z` coordinates indication the locations of the map chests. 
  - Map chests will be filled at the start of the game and emptied at the end of the game
  - Map chests will be given loot from a random loot table in the `"weightedMechaLootTables"` list (see below)
- `"spawnLootTable"` the loot table for the spawn chests
- `"weightedMechaLootTables"` the loot tables for the map chests
  - a loot table for each chest will be randomly selected from these. The likelyhood of a particular loot table being picked is determined by its weight. Weights can be as low as 1, and must be integers. The lower the weight, the more likely it is to be chosen. Items with identical weights will be equally likely to be chosen. 
- `"borderStages"` a list of stages for the world border during the game. 
  - The first stage is the initial state.
  - `"size"` is the size of the world border in blocks.
  - `"delay"` is how many seconds the border will stay the current size before shrinking
  - `"duration"` is how many seconds the border will take to shrink to the next size, once it starts shrinking
  - The border will progress through the stages in the list until it reaches the last stage, where it will stay still. 

### parkourPathwayConfig.json

This is the config file for Parkour Pathway. It allows you to configure:
- The checkpoints along the parkour map
- The world that the map is located in

This is an example of a `parkourPathwayConfig.json` file

```json
{
  "checkpoints": [
    {
      "yValue": 0,
      "min": {
        "x": 998,
        "y": -1,
        "z": -8
      },
      "max": {
        "x": 1007,
        "y": 5,
        "z": 8
      },
      "respawn": {
        "x": 1003,
        "y": 0,
        "z": 0
      }
    },
    ...
  ],
  "world": "FT"
}
```

- `"checkpoints"` defines a list of checkpoints. 
  - The first checkpoint is the initial spawn location when players are teleported in
  - Checkpoints are progressed in order
  - The last checkpoint is the finish line
    - It might not make sense to have a `"yValue"` field for the finish line. Suffice it to say, this is the easy way. Just set it lower than the player can go.
  - `"yValue"` is the lower limit of the checkpoint. If the player falls below that y height, they'll be teleported back to `"respawn"`
  - `"min"` and `"max"` are the minimum and maximum corners for the bounding box that defines the detection region for the checkpoint. If a player steps into that bounding box, then they have "reached" that checkpoint. 
    - Think of this like the `/fill` command, where the first set of coordinates is `"min"` and the second set is `"max"`
  - `"respawn"` the position of the respawn point
    - The player will be teleported here if the player falls below the `"yValue"` after reaching this checkpoint
- `"world"` specifies the name of the world the Parkour Map is in





