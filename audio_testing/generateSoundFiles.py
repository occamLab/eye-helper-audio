# generate wav file containing sine waves
# FB36 - 20120617
import math
import wave
import array
import pyaudio
import struct
import numpy

data = array.array('h') # signed short integer (-32768 to 32767) data

numChan = 2 # of channels (1: mono, 2: stereo)
dataSize = 2 # 2 bytes because of using signed short integers => bit depth = 16

leftSound = []
rightSound = []

headWidth = 0.15

file_read = wave.open('dovecooing.wav', 'r')
sampleRate = file_read.getframerate()

tempSignal = file_read.readframes(-1)
soundArray = numpy.fromstring(tempSignal, 'Int16')

print soundArray[0]

for angleSection in range(18):
	angle = 10 * angleSection - 85
	soundPosition = (1, angle)
	speedOfSound = 343

	distanceToLeftEar = math.sqrt((soundPosition[0]*math.cos(math.radians(soundPosition[1])))**2 + (soundPosition[0]*(math.sin(math.radians(soundPosition[1])))-headWidth)**2)
	print distanceToLeftEar
	samplesToDelayBy = (soundPosition[0] - distanceToLeftEar)/speedOfSound * sampleRate # If delay is positive, delay left, otherwise, delay right
	delay = numpy.zeros(int(abs(samplesToDelayBy)))

	print(soundArray)

	if samplesToDelayBy > 0:
		leftSound = numpy.concatenate([delay,soundArray])
		rightSound = numpy.concatenate([soundArray,delay])
	else:
		leftSound = numpy.concatenate([soundArray,delay])
		rightSound = numpy.concatenate([delay,soundArray])
	
	numSamples = rightSound.size

	f = wave.open('angle' + str(angle) + '.wav', 'w')
	f.setparams((numChan, dataSize, sampleRate, numSamples + int(samplesToDelayBy), "NONE", "Uncompressed"))
	#f.writeframes(leftSound.tostring(), rightSound.tostring())
	for left,right in zip(leftSound,rightSound):
	    f.writeframes(struct.pack('h', int(right)))
	    f.writeframes(struct.pack('h', int(left)))

	f.close()