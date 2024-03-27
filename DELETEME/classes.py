import json

class BoundingBox:
    def __init__(self, minX, minY, minZ, maxX, maxY, maxZ):
        self.minX = minX
        self.minY = minY
        self.minZ = minZ
        self.maxX = maxX
        self.maxY = maxY
        self.maxZ = maxZ
    
    def serialize(self):
        return {
            "minX": self.minX,
            "minY": self.minY,
            "minZ": self.minZ,
            "maxX": self.maxX,
            "maxY": self.maxY,
            "maxZ": self.maxZ
        }
    
    def clone(self):
        return BoundingBox(self.minX, self.minY,self. minZ, self.maxX, self.maxY, self.maxZ)
        
    def equals(self, other):
        return  self.minX == other.minX \
            and self.minY == other.minY \
            and self.minZ == other.minZ \
            and self.maxX == other.maxX \
            and self.maxY == other.maxY \
            and self.maxZ == other.maxZ
    
    @classmethod
    def deserialize(cls, data):
        return cls(data["minX"], data["minY"], data["minZ"], data["maxX"], data["maxY"], data["maxZ"])

class Vector:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z
    
    def serialize(self):
        return {
            "x": self.x,
            "y": self.y,
            "z": self.z
        }
    
    @classmethod
    def deserialize(cls, data):
        return cls(data["x"], data["y"], data["z"])

class CheckPoint:
    def __init__(self, detectionArea, respawn):
        self.detectionArea = detectionArea
        self.respawn = respawn
    
    def serialize(self):
        return {
            "detectionArea": self.detectionArea.serialize(),
            "respawn": self.respawn.serialize()
        }
    
    @classmethod
    def deserialize(cls, data):
        detection_area = BoundingBox.deserialize(data["detectionArea"])
        respawn = Vector.deserialize(data["respawn"])
        return cls(detection_area, respawn)

class Puzzle:
    def __init__(self, in_bounds, checkpoints):
        self.in_bounds = in_bounds
        self.checkpoints = checkpoints
    
    def serialize(self):
        return {
            "inBounds": self.in_bounds.serialize(),
            "checkPoints": [checkpoint.serialize() for checkpoint in self.checkpoints]
        }
    
    @classmethod
    def deserialize(cls, data):
        in_bounds = BoundingBox.deserialize(data["inBounds"])
        checkpoints = [CheckPoint.deserialize(cp) for cp in data["checkPoints"]]
        return cls(in_bounds, checkpoints)

# Example usage:
json_data = '''
{
    "inBounds": {
        "minX": 0,
        "minY": 0,
        "minZ": 0,
        "maxX": 100,
        "maxY": 100,
        "maxZ": 100
    },
    "checkPoints": [
        {
            "detectionArea": {
                "minX": 1007,
                "minY": 5,
                "minZ": 8,
                "maxX": 1003,
                "maxY": 0,
                "maxZ": -8
            },
            "respawn": {
                "x": 998,
                "y": 0,
                "z": 0
            }
        }
    ]
}
'''

data = json.loads(json_data)
puzzle = Puzzle.deserialize(data)
# print(puzzle.serialize())

# Example usage:
in_bounds = BoundingBox(0, 0, 0, 100, 100, 100)

detection_area = BoundingBox(1007, 5, 8, 1003, 0, -8)
respawn_vector = Vector(998, 0, 0)
checkpoint = CheckPoint(detection_area, respawn_vector)

puzzle = Puzzle(in_bounds, [checkpoint])
# print(puzzle.serialize())
