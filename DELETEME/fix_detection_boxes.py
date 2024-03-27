import json

from classes import BoundingBox, Vector


input_json = """
{
    "checkpoints": [
        {
            "yValue": 0,      
            "detectionBox": { 
                "minX": 998,  
                "minY": -1,   
                "minZ": -8,   
                "maxX": 1007, 
                "maxY": 5,    
                "maxZ": 8     
            },
            "respawn": {
                "x": 1003,
                "y": 0,
                "z": 0
            }
        },
        {
            "yValue": 8,
            "detectionBox": {
                "minX": 1017,
                "minY": 7,
                "minZ": -6,
                "maxX": 1020,
                "maxY": 10,
                "maxZ": 6
            },
            "respawn": {
                "x": 1017,
                "y": 8,
                "z": 4
            }
        },
        {
            "yValue": 5,
            "detectionBox": {
                "minX": 1046,
                "minY": 7,
                "minZ": -4,
                "maxX": 1048,
                "maxY": 11,
                "maxZ": 3
            },
            "respawn": {
                "x": 1046,
                "y": 8,
                "z": 2
            }
        },
        {
            "yValue": 9,
            "detectionBox": {
                "minX": 1074,
                "minY": 9,
                "minZ": -3,
                "maxX": 1077,
                "maxY": 13,
                "maxZ": 4
            },
            "respawn": {
                "x": 1076,
                "y": 9,
                "z": 0
            }
        },
        {
            "yValue": 9,
            "detectionBox": {
                "minX": 1099,
                "minY": 9,
                "minZ": -7,
                "maxX": 1101,
                "maxY": 11,
                "maxZ": 7
            },
            "respawn": {
                "x": 1101,
                "y": 9,
                "z": 0
            }
        },
        {
            "yValue": 32,
            "detectionBox": {
                "minX": 1106,
                "minY": 32,
                "minZ": -10,
                "maxX": 1109,
                "maxY": 38,
                "maxZ": 10
            },
            "respawn": {
                "x": 1107,
                "y": 33,
                "z": -6
            }
        },
        {
            "yValue": 35,
            "detectionBox": {
                "minX": 1107,
                "minY": 34,
                "minZ": -3,
                "maxX": 1109,
                "maxY": 38,
                "maxZ": 3
            },
            "respawn": {
                "x": 1108,
                "y": 35,
                "z": 0
            }
        },
        {
            "yValue": 42,
            "detectionBox": {
                "minX": 1099,
                "minY": 43,
                "minZ": -1,
                "maxX": 1101,
                "maxY": 45,
                "maxZ": 1
            },
            "respawn": {
                "x": 1100,
                "y": 43,
                "z": 0
            }
        },
        {
            "yValue": 42,
            "detectionBox": {
                "minX": 1077,
                "minY": 43,
                "minZ": -3,
                "maxX": 1079,
                "maxY": 48,
                "maxZ": 3
            },
            "respawn": {
                "x": 1078,
                "y": 44,
                "z": 0
            }
        },
        {
            "yValue": 42,
            "detectionBox": {
                "minX": 1034,
                "minY": 43,
                "minZ": -3,
                "maxX": 1038,
                "maxY": 48,
                "maxZ": 3
            },
            "respawn": {
                "x": 1036,
                "y": 44,
                "z": 0
            }
        },
        {
            "yValue": 42,
            "detectionBox": {
                "minX": 1017,
                "minY": 44,
                "minZ": -5,
                "maxX": 1020,
                "maxY": 51,
                "maxZ": 6
            },
            "respawn": {
                "x": 1019,
                "y": 45,
                "z": 0
            }
        },
        {
            "yValue": 44,
            "detectionBox": {
                "minX": 1003,
                "minY": 43,
                "minZ": -3,
                "maxX": 1005,
                "maxY": 48,
                "maxZ": 5
            },
            "respawn": {
                "x": 1004,
                "y": 44,
                "z": 1
            }
        },
        {
            "yValue": 42,
            "detectionBox": {
                "minX": 981,
                "minY": 43,
                "minZ": -3,
                "maxX": 984,
                "maxY": 48,
                "maxZ": 5
            },
            "respawn": {
                "x": 982,
                "y": 44,
                "z": 1
            }
        },
        {
            "yValue": 34,
            "detectionBox": {
                "minX": 917,
                "minY": 40,
                "minZ": -3,
                "maxX": 923,
                "maxY": 47,
                "maxZ": 5
            },
            "respawn": {
                "x": 917,
                "y": 41,
                "z": 1
            }
        },
        {
            "yValue": 65,
            "detectionBox": {
                "minX": 897,
                "minY": 72,
                "minZ": -7,
                "maxX": 900,
                "maxY": 74,
                "maxZ": 10
            },
            "respawn": {
                "x": 897,
                "y": 72,
                "z": 1
            }
        },
        {
            "yValue": 67,
            "detectionBox": {
                "minX": 866,
                "minY": 69,
                "minZ": -1,
                "maxX": 867,
                "maxY": 72,
                "maxZ": 3
            },
            "respawn": {
                "x": 866,
                "y": 69,
                "z": 1
            }
        },
        {
            "yValue": 57,
            "detectionBox": {
                "minX": 845,
                "minY": 69,
                "minZ": -1,
                "maxX": 847,
                "maxY": 75,
                "maxZ": 3
            },
            "respawn": {
                "x": 846,
                "y": 70,
                "z": 1
            }
        },
        {
            "yValue": 48,
            "detectionBox": {
                "minX": 820,
                "minY": 59,
                "minZ": -3,
                "maxX": 822,
                "maxY": 62,
                "maxZ": 3
            },
            "respawn": {
                "x": 819,
                "y": 58,
                "z": 0
            }
        },
        {
            "yValue": 49,
            "detectionBox": {
                "minX": 825,
                "minY": 49,
                "minZ": -2,
                "maxX": 826,
                "maxY": 53,
                "maxZ": 2
            },
            "respawn": {
                "x": 826,
                "y": 50,
                "z": 0
            }
        },
        {
            "yValue": 31,
            "detectionBox": {
                "minX": 860,
                "minY": 50,
                "minZ": -3,
                "maxX": 861,
                "maxY": 53,
                "maxZ": 3
            },
            "respawn": {
                "x": 861,
                "y": 32,
                "z": 0
            }
        },
        {
            "yValue": 16,
            "detectionBox": {
                "minX": 824,
                "minY": 33,
                "minZ": -2,
                "maxX": 827,
                "maxY": 36,
                "maxZ": 2
            },
            "respawn": {
                "x": 825,
                "y": 27,
                "z": 0
            }
        },
        {
            "yValue": 3,
            "detectionBox": {
                "minX": 799,
                "minY": 17,
                "minZ": -6,
                "maxX": 800,
                "maxY": 20,
                "maxZ": 6
            },
            "respawn": {
                "x": 794,
                "y": 5,
                "z": 0
            }
        },
        {
            "yValue": -2,
            "detectionBox": {
                "minX": 819,
                "minY": 5,
                "minZ": -2,
                "maxX": 820,
                "maxY": 7,
                "maxZ": 3
            },
            "respawn": {
                "x": 820,
                "y": 5,
                "z": 0
            }
        },
        {
            "yValue": -13,
            "detectionBox": {
                "minX": 861,
                "minY": 5,
                "minZ": -3,
                "maxX": 862,
                "maxY": 8,
                "maxZ": 3
            },
            "respawn": {
                "x": 864,
                "y": 5,
                "z": 0
            }
        },
        {
            "yValue": -10,
            "detectionBox": {
                "minX": 892,
                "minY": -8,
                "minZ": 0,
                "maxX": 893,
                "maxY": -6,
                "maxZ": 2
            },
            "respawn": {
                "x": 893,
                "y": -8,
                "z": 1
            }
        },
        {
            "yValue": -10,
            "detectionBox": {
                "minX": 925,
                "minY": -9,
                "minZ": -1,
                "maxX": 927,
                "maxY": -3,
                "maxZ": 3
            },
            "respawn": {
                "x": 926,
                "y": -8,
                "z": 1
            }
        },
        {
            "yValue": -10,
            "detectionBox": {
                "minX": 939,
                "minY": -9,
                "minZ": -1,
                "maxX": 941,
                "maxY": -3,
                "maxZ": 3
            },
            "respawn": {
                "x": 940,
                "y": -8,
                "z": 1
            }
        },
        {
            "yValue": -9,
            "detectionBox": {
                "minX": 953,
                "minY": -8,
                "minZ": -2,
                "maxX": 955,
                "maxY": -6,
                "maxZ": 4
            },
            "respawn": {
                "x": 954,
                "y": -8,
                "z": 1
            }
        },
        {
            "yValue": -9,
            "detectionBox": {
                "minX": 971,
                "minY": -8,
                "minZ": -2,
                "maxX": 972,
                "maxY": -5,
                "maxZ": 2
            },
            "respawn": {
                "x": 971,
                "y": -7,
                "z": 0
            }
        },
        {
            "yValue": -15,
            "detectionBox": {
                "minX": 986,
                "minY": -9,
                "minZ": -2,
                "maxX": 988,
                "maxY": -6,
                "maxZ": 0
            },
            "respawn": {
                "x": 987,
                "y": -8,
                "z": -1
            }
        }
    ]
}
"""

# This script makes sure that each check point's detectionBox contains the respawn. the max values of the detectionBox must be greater than (not equal to) the respawn's values.
# i.e. respawn.x can't be equal to detectionBox.maxX.

def create_detection_box(old_detection_box: BoundingBox, respawn: Vector) -> BoundingBox:
    """
    returns a new BoundingBox which is identical to old_detection_box, but the (maxX, maxY, maxZ) are > (x, y, z) of the respawn. and the (minX, minY, minZ) are <= (x, y, z) of the respawn
    """
    new_detection_box = old_detection_box.clone()
    # make sure detection_area contains respawn (with at least 1 unit of clearance above the y value of respawn)
    new_detection_box.maxX = max(new_detection_box.maxX, respawn.x)
    if (new_detection_box.maxX - respawn.x == 0):
        new_detection_box.maxX += 1
    
    new_detection_box.maxY = max(new_detection_box.maxY, respawn.y)
    if (new_detection_box.maxY - respawn.y == 0):
        new_detection_box.maxY += 1
    
    new_detection_box.maxZ = max(new_detection_box.maxZ, respawn.z)
    if (new_detection_box.maxZ - respawn.z == 0):
        new_detection_box.maxZ += 1
    
    new_detection_box.minX = min(new_detection_box.minX, respawn.x)
    new_detection_box.minY = min(new_detection_box.minY, respawn.y)
    new_detection_box.minZ = min(new_detection_box.minZ, respawn.z)
    
    
    return new_detection_box
    

original_data = json.loads(input_json)

new_checkpoints = []

old_checkpoints = original_data['checkpoints']
for n in range(0, len(old_checkpoints)):
    old_checkpoint = old_checkpoints[n]
    
    y_value = old_checkpoint['yValue']
    old_detection_box = BoundingBox.deserialize(old_checkpoint['detectionBox'])
    respawn = Vector.deserialize(old_checkpoint['respawn'])
    
    new_detection_box = create_detection_box(old_detection_box, respawn)
    if not new_detection_box.equals(old_detection_box):
        print(f"checkpoint[{n}] was altered")
    
    new_checkpoint = {
        'yValue': y_value,
        'detectionBox': new_detection_box.serialize(),
        'respawn': respawn.serialize()
    }
    new_checkpoints.append(new_checkpoint)

new_json = json.dumps(new_checkpoints, indent=4)
print(new_json)
