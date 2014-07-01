import math

headWidth = 15
soundPosition = (100, -45)
speedOfSound = 331.4

distanceToVertical = soundPosition[0]*math.sin(math.radians(soundPosition[1]))
distanceToHorizontal = soundPosition[0]*math.cos(math.radians(soundPosition[1]))
leftEarTriangle = distanceToVertical - headWidth

print math.sqrt(leftEarTriangle**2 + distanceToHorizontal**2)

distanceToLeftEar = math.sqrt((soundPosition[0]*math.cos(math.radians(soundPosition[1])))**2 + (soundPosition[0]*(math.sin(math.radians(soundPosition[1])))-headWidth)**2)

print distanceToLeftEar

print (distanceToLeftEar/100) * (1/speedOfSound)
