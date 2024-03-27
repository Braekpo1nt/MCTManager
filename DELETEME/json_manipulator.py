import json

from classes import BoundingBox, Vector, CheckPoint, Puzzle

# Load the input JSON string
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
                "maxX": 1102,
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
                "minX": 819,
                "minY": 58,
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
                "maxX": 827,
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
                "minY": 32,
                "minZ": -3,
                "maxX": 862,
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
                "minY": 27,
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
                "minX": 794,
                "minY": 5,
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
                "maxX": 821,
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
                "maxX": 865,
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
                "maxX": 894,
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

def get_min_max_vectors(bbox1, bbox2) -> tuple[Vector, Vector]:
    """
    get the min corner of both boxes, and the max corner of both boxes
    """
    min_x = min(bbox1.minX, bbox2.minX)
    min_y = min(bbox1.minY, bbox2.minY)
    min_z = min(bbox1.minZ, bbox2.minZ)
    max_x = max(bbox1.maxX, bbox2.maxX)
    max_y = max(bbox1.maxY, bbox2.maxY)
    max_z = max(bbox1.maxZ, bbox2.maxZ)
    
    min_vector = Vector(min_x, min_y, min_z)
    max_vector = Vector(max_x, max_y, max_z)
    
    return min_vector, max_vector

def create_finish_in_in_bounds (box_a: BoundingBox, y_value: float) -> BoundingBox:
    min_y = min(box_a.minY, y_value)
    return BoundingBox(box_a.minX, min_y, box_a.minZ, box_a.maxX, box_a.maxY, box_a.maxZ)

def create_in_bounds(box_a: BoundingBox, box_b: BoundingBox, y_value: float) -> BoundingBox:
    if (box_a == None or box_b == None or y_value == None):
        Exception("these objects can't be null")
        exit()
    
    (min_corner, max_corner) = get_min_max_vectors(box_a, box_b)
    min_y = min(min_corner.y, y_value)
    return BoundingBox(min_corner.x, min_y, min_corner.z, max_corner.x, max_corner.y, max_corner.z)


# Parse the JSON string
original_data = json.loads(input_json)

puzzles = []

old_checkpoints = original_data['checkpoints']
for n in range(0, len(old_checkpoints)):
    old_checkpoint = old_checkpoints[n]
    
    old_y_value = old_checkpoint['yValue']
    old_detection_box = BoundingBox.deserialize(old_checkpoint['detectionBox'])
    old_respawn = Vector.deserialize(old_checkpoint['respawn'])
    
    if n+1 < len(old_checkpoints):
        next_old_checkpoint = old_checkpoints[n+1]
        next_detection_box = BoundingBox.deserialize(next_old_checkpoint['detectionBox'])
        in_bounds = create_in_bounds(old_detection_box, next_detection_box, old_y_value)
    else:
        in_bounds = create_finish_in_in_bounds(old_detection_box, old_y_value)
    
    
    check_point = CheckPoint(old_detection_box, old_respawn)
    puzzle = Puzzle(in_bounds, [check_point])
    puzzles.append(puzzle.serialize())

output_json = json.dumps(puzzles, indent=4)
print(output_json)

# for puzzle in puzzles:
#     print(puzzle.serialize() + ',')
