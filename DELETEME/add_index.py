import json


input_json = """
{
    "puzzles": [
    {
      "inBounds": {
        "minX": 998.0,
        "minY": -1.0,
        "minZ": -8.0,
        "maxX": 1020.0,
        "maxY": 15.0,
        "maxZ": 8.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 998.0,
            "minY": -1.0,
            "minZ": -8.0,
            "maxX": 1007.0,
            "maxY": 5.0,
            "maxZ": 8.0
          },
          "respawn": {
            "x": 1003.0,
            "y": 0.0,
            "z": 0.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1016.0,
        "minY": 7.5,
        "minZ": -10.0,
        "maxX": 1049.0,
        "maxY": 16.0,
        "maxZ": 10.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1017.0,
            "minY": 8.0,
            "minZ": 3.0,
            "maxX": 1020.0,
            "maxY": 10.0,
            "maxZ": 6.0
          },
          "respawn": {
            "x": 1018.0,
            "y": 8.0,
            "z": 5.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        },
        {
          "detectionArea": {
            "minX": 1017.0,
            "minY": 8.0,
            "minZ": -6.0,
            "maxX": 1020.0,
            "maxY": 10.0,
            "maxZ": -3.0
          },
          "respawn": {
            "x": 1018.0,
            "y": 8.0,
            "z": -4.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1046.0,
        "minY": 5.0,
        "minZ": -6.0,
        "maxX": 1077.0,
        "maxY": 16.0,
        "maxZ": 6.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1046.0,
            "minY": 8.0,
            "minZ": 1.0,
            "maxX": 1049.0,
            "maxY": 11.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 1047.0,
            "y": 8.0,
            "z": 2.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        },
        {
          "detectionArea": {
            "minX": 1046.0,
            "minY": 8.0,
            "minZ": -2.0,
            "maxX": 1049.0,
            "maxY": 11.0,
            "maxZ": 0.0
          },
          "respawn": {
            "x": 1047.0,
            "y": 8.0,
            "z": -1.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1074.0,
        "minY": 8.5,
        "minZ": -6.0,
        "maxX": 1102.0,
        "maxY": 14.0,
        "maxZ": 7.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1074.0,
            "minY": 9.0,
            "minZ": -3.0,
            "maxX": 1077.0,
            "maxY": 13.0,
            "maxZ": 4.0
          },
          "respawn": {
            "x": 1076.5,
            "y": 9.0,
            "z": 0.5,
            "yaw": -90.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1099.0,
        "minY": 9.0,
        "minZ": -10.0,
        "maxX": 1111.0,
        "maxY": 42.0,
        "maxZ": 10.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1099.0,
            "minY": 9.0,
            "minZ": -2.0,
            "maxX": 1102.0,
            "maxY": 12.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 1101.0,
            "y": 9.0,
            "z": 0.5,
            "yaw": -90.0,
            "pitch": -45.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1100.0,
        "minY": 32.0,
        "minZ": -9.0,
        "maxX": 1130.0,
        "maxY": 47.0,
        "maxZ": 10.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1106.0,
            "minY": 33.0,
            "minZ": -9.0,
            "maxX": 1109.0,
            "maxY": 38.0,
            "maxZ": -3.0
          },
          "respawn": {
            "x": 1107.5,
            "y": 33.0,
            "z": -6.0,
            "yaw": -90.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1099.0,
        "minY": 34.0,
        "minZ": -3.0,
        "maxX": 1109.0,
        "maxY": 45.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1107.0,
            "minY": 34.0,
            "minZ": -3.0,
            "maxX": 1109.0,
            "maxY": 38.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 1108.0,
            "y": 35.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1077.0,
        "minY": 42.0,
        "minZ": -3.0,
        "maxX": 1101.0,
        "maxY": 48.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1099.0,
            "minY": 43.0,
            "minZ": -1.0,
            "maxX": 1101.0,
            "maxY": 45.0,
            "maxZ": 1.0
          },
          "respawn": {
            "x": 1100.0,
            "y": 43.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1034.0,
        "minY": 42.0,
        "minZ": -3.0,
        "maxX": 1079.0,
        "maxY": 48.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1077.0,
            "minY": 43.0,
            "minZ": -3.0,
            "maxX": 1079.0,
            "maxY": 48.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 1078.0,
            "y": 44.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1017.0,
        "minY": 42.0,
        "minZ": -5.0,
        "maxX": 1038.0,
        "maxY": 51.0,
        "maxZ": 6.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1034.0,
            "minY": 43.0,
            "minZ": -3.0,
            "maxX": 1038.0,
            "maxY": 48.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 1036.0,
            "y": 44.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 1003.0,
        "minY": 42.0,
        "minZ": -5.0,
        "maxX": 1020.0,
        "maxY": 51.0,
        "maxZ": 6.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1017.0,
            "minY": 44.0,
            "minZ": -5.0,
            "maxX": 1020.0,
            "maxY": 51.0,
            "maxZ": 6.0
          },
          "respawn": {
            "x": 1019.0,
            "y": 45.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 981.0,
        "minY": 43.0,
        "minZ": -3.0,
        "maxX": 1005.0,
        "maxY": 48.0,
        "maxZ": 5.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 1003.0,
            "minY": 43.0,
            "minZ": -3.0,
            "maxX": 1005.0,
            "maxY": 48.0,
            "maxZ": 5.0
          },
          "respawn": {
            "x": 1004.0,
            "y": 44.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 917.0,
        "minY": 40.0,
        "minZ": -3.0,
        "maxX": 984.0,
        "maxY": 48.0,
        "maxZ": 5.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 981.0,
            "minY": 43.0,
            "minZ": -3.0,
            "maxX": 984.0,
            "maxY": 48.0,
            "maxZ": 5.0
          },
          "respawn": {
            "x": 982.0,
            "y": 44.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 897.0,
        "minY": 34.0,
        "minZ": -7.0,
        "maxX": 923.0,
        "maxY": 74.0,
        "maxZ": 10.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 917.0,
            "minY": 40.0,
            "minZ": -3.0,
            "maxX": 923.0,
            "maxY": 47.0,
            "maxZ": 5.0
          },
          "respawn": {
            "x": 917.0,
            "y": 41.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 866.0,
        "minY": 65.0,
        "minZ": -7.0,
        "maxX": 900.0,
        "maxY": 74.0,
        "maxZ": 10.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 897.0,
            "minY": 72.0,
            "minZ": -7.0,
            "maxX": 900.0,
            "maxY": 74.0,
            "maxZ": 10.0
          },
          "respawn": {
            "x": 897.0,
            "y": 72.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 845.0,
        "minY": 67.0,
        "minZ": -1.0,
        "maxX": 867.0,
        "maxY": 75.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 866.0,
            "minY": 69.0,
            "minZ": -1.0,
            "maxX": 867.0,
            "maxY": 72.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 866.0,
            "y": 69.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 819.0,
        "minY": 57.0,
        "minZ": -3.0,
        "maxX": 847.0,
        "maxY": 75.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 845.0,
            "minY": 69.0,
            "minZ": -1.0,
            "maxX": 847.0,
            "maxY": 75.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 846.0,
            "y": 70.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 819.0,
        "minY": 48.0,
        "minZ": -3.0,
        "maxX": 827.0,
        "maxY": 62.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 819.0,
            "minY": 58.0,
            "minZ": -3.0,
            "maxX": 822.0,
            "maxY": 62.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 819.0,
            "y": 58.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 825.0,
        "minY": 32.0,
        "minZ": -3.0,
        "maxX": 862.0,
        "maxY": 53.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 825.0,
            "minY": 49.0,
            "minZ": -2.0,
            "maxX": 827.0,
            "maxY": 53.0,
            "maxZ": 2.0
          },
          "respawn": {
            "x": 826.0,
            "y": 50.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 824.0,
        "minY": 27.0,
        "minZ": -3.0,
        "maxX": 862.0,
        "maxY": 53.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 860.0,
            "minY": 32.0,
            "minZ": -3.0,
            "maxX": 862.0,
            "maxY": 53.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 861.0,
            "y": 32.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 794.0,
        "minY": 5.0,
        "minZ": -6.0,
        "maxX": 827.0,
        "maxY": 36.0,
        "maxZ": 6.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 824.0,
            "minY": 27.0,
            "minZ": -2.0,
            "maxX": 827.0,
            "maxY": 36.0,
            "maxZ": 2.0
          },
          "respawn": {
            "x": 825.0,
            "y": 27.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 794.0,
        "minY": 3.0,
        "minZ": -6.0,
        "maxX": 821.0,
        "maxY": 20.0,
        "maxZ": 6.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 794.0,
            "minY": 5.0,
            "minZ": -6.0,
            "maxX": 800.0,
            "maxY": 20.0,
            "maxZ": 6.0
          },
          "respawn": {
            "x": 794.0,
            "y": 5.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 819.0,
        "minY": -2.0,
        "minZ": -3.0,
        "maxX": 865.0,
        "maxY": 8.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 819.0,
            "minY": 5.0,
            "minZ": -2.0,
            "maxX": 821.0,
            "maxY": 7.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 820.0,
            "y": 5.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 861.0,
        "minY": -13.0,
        "minZ": -3.0,
        "maxX": 894.0,
        "maxY": 8.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 861.0,
            "minY": 5.0,
            "minZ": -3.0,
            "maxX": 865.0,
            "maxY": 8.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 864.0,
            "y": 5.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 892.0,
        "minY": -10.0,
        "minZ": -1.0,
        "maxX": 927.0,
        "maxY": -3.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 892.0,
            "minY": -8.0,
            "minZ": 0.0,
            "maxX": 894.0,
            "maxY": -6.0,
            "maxZ": 2.0
          },
          "respawn": {
            "x": 893.0,
            "y": -8.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 925.0,
        "minY": -10.0,
        "minZ": -1.0,
        "maxX": 941.0,
        "maxY": -3.0,
        "maxZ": 3.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 925.0,
            "minY": -9.0,
            "minZ": -1.0,
            "maxX": 927.0,
            "maxY": -3.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 926.0,
            "y": -8.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 939.0,
        "minY": -10.0,
        "minZ": -2.0,
        "maxX": 955.0,
        "maxY": -3.0,
        "maxZ": 4.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 939.0,
            "minY": -9.0,
            "minZ": -1.0,
            "maxX": 941.0,
            "maxY": -3.0,
            "maxZ": 3.0
          },
          "respawn": {
            "x": 940.0,
            "y": -8.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 953.0,
        "minY": -9.0,
        "minZ": -2.0,
        "maxX": 972.0,
        "maxY": -5.0,
        "maxZ": 4.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 953.0,
            "minY": -8.0,
            "minZ": -2.0,
            "maxX": 955.0,
            "maxY": -6.0,
            "maxZ": 4.0
          },
          "respawn": {
            "x": 954.0,
            "y": -8.0,
            "z": 1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 971.0,
        "minY": -9.0,
        "minZ": -2.0,
        "maxX": 988.0,
        "maxY": -5.0,
        "maxZ": 2.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 971.0,
            "minY": -8.0,
            "minZ": -2.0,
            "maxX": 972.0,
            "maxY": -5.0,
            "maxZ": 2.0
          },
          "respawn": {
            "x": 971.0,
            "y": -7.0,
            "z": 0.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    },
    {
      "inBounds": {
        "minX": 986.0,
        "minY": -15.0,
        "minZ": -2.0,
        "maxX": 988.0,
        "maxY": -6.0,
        "maxZ": 0.0
      },
      "checkPoints": [
        {
          "detectionArea": {
            "minX": 986.0,
            "minY": -9.0,
            "minZ": -2.0,
            "maxX": 988.0,
            "maxY": -6.0,
            "maxZ": 0.0
          },
          "respawn": {
            "x": 987.0,
            "y": -8.0,
            "z": -1.0,
            "yaw": 0.0,
            "pitch": 0.0
          }
        }
      ]
    }
  ]
}
"""

original_data = json.loads(input_json)

n = 0
for puzzle in original_data['puzzles']:
    in_bounds = puzzle.pop('inBounds')
    check_points = puzzle.pop('checkPoints')
    puzzle['index'] = n
    puzzle['inBounds'] = in_bounds
    puzzle['checkPoints'] = check_points
    n += 1

output_json = json.dumps(original_data, indent=2)
print(output_json)

