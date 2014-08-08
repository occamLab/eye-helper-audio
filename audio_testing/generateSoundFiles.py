# generate wav file containing sine waves
# FB36 - 20120617
import math
import wave
import array
import pyaudio
import struct
import numpy
import os

#The base of the file
fileBaseName = 'Piano'

data = array.array('h') # signed short integer (-32768 to 32767) data

numChan = 2 # of channels (1: mono, 2: stereo)
dataSize = 2 # 2 bytes because of using signed short integers => bit depth = 16

leftSound = []
rightSound = []

headWidth = 0.15

#Make the folder to put the files in
if not os.path.exists('GeneratedSoundFiles'):
	os.makedirs('GeneratedSoundFiles')	

for h in range (8):
	height = h
	print height

	file_read = wave.open('SoundFiles_WAV/' + fileBaseName + str(height) + '.wav', 'r')
	sampleRate = file_read.getframerate()

	tempSignal = file_read.readframes(-1)
	soundArray = numpy.fromstring(tempSignal, 'Int16')

	# if not os.path.exists('GeneratedSoundFiles/' + fileBaseName + str(height)):
	# 	os.makedirs('GeneratedSoundFiles/' + fileBaseName + str(height))	

	#How many sound files we want (Here it's 18)
	for angleSection in range(18):
		#The math to shift so they go from -85 degrees to 85 degrees
		angle = 10 * angleSection - 85
		soundPosition = (1, angle)
		speedOfSound = 343

		#So we know where we are in the code
		print angle

		#The math -- Don't cange this  (unless we determine it is wrong)
		distanceToLeftEar = math.sqrt((soundPosition[0]*math.cos(math.radians(soundPosition[1])))**2 + (soundPosition[0]*(math.sin(math.radians(soundPosition[1])))-headWidth)**2)
		samplesToDelayBy = (soundPosition[0] - distanceToLeftEar)/speedOfSound * sampleRate # If delay is positive, delay left, otherwise, delay right
		delay = numpy.zeros(int(abs(samplesToDelayBy)))

		if samplesToDelayBy > 0:
			leftSound = numpy.concatenate([delay,soundArray])
			rightSound = numpy.concatenate([soundArray,delay])
		else:
			leftSound = numpy.concatenate([soundArray,delay])
			rightSound = numpy.concatenate([delay,soundArray])
		
		numSamples = rightSound.size
		#Write the files (underscore means negative)
		if angle < 0:
			# f = wave.open('GeneratedSoundFiles/' + fileBaseName + str(height) + '/angle_' + str(abs(angle)) + '.wav', 'w')
			f = wave.open('GeneratedSoundFiles/height' + str(height) + 'angle_' + str(abs(angle)) + '.wav', 'w')
		else:
			# f = wave.open('GeneratedSoundFiles/' + fileBaseName + str(height) + '/angle' + str(angle) + '.wav', 'w')
			f = wave.open('GeneratedSoundFiles/height' + str(height) + 'angle' + str(abs(angle)) + '.wav', 'w')
		f.setparams((numChan, dataSize, sampleRate, numSamples + int(samplesToDelayBy), "NONE", "Uncompressed"))
		#f.writeframes(leftSound.tostring(), rightSound.tostring())
		for left,right in zip(leftSound,rightSound):
		    f.writeframes(struct.pack('h', int(right)))
		    f.writeframes(struct.pack('h', int(left)))

	f.close()