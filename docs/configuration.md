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





