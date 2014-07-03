# generate wav file containing sine waves
# FB36 - 20120617
import math
import wave
import array
import pyaudio
import struct

duration = 3 # seconds
freq = 440 # of cycles per second (Hz) (frequency of the sine waves)
volume = 100 # percent

data = array.array('h') # signed short integer (-32768 to 32767) data
sampleRate = 44100 # of samples per second (standard)
numChan = 2 # of channels (1: mono, 2: stereo)
dataSize = 2 # 2 bytes because of using signed short integers => bit depth = 16
numSamplesPerCyc = int(sampleRate / freq)
numSamples = sampleRate * duration
beepSound = []

leftSound = []
rightSound = []

headWidth = 0.15
soundPosition = (1, 90)
speedOfSound = 331.4

distanceToLeftEar = math.sqrt((soundPosition[0]*math.cos(math.radians(soundPosition[1])))**2 + (soundPosition[0]*(math.sin(math.radians(soundPosition[1])))-headWidth)**2)
samplesToDelayBy = (soundPosition[0] - distanceToLeftEar)/speedOfSound * sampleRate # If delay is positive, delay left, otherwise, delay right
delay = [0] * int(samplesToDelayBy)






for i in range(numSamples):
    sample = 32767 * float(volume) / 100
    sample *= math.sin(math.pi * 2 * (i % numSamplesPerCyc) / numSamplesPerCyc)
    beepSound.append(sample)

if samplesToDelayBy > 0:
    leftSound = delay + beepSound
    rightSound = beepSound + delay
else:
    leftSound = beepSound + delay
    rightSound = delay + beepSound



f = wave.open('test_file.wav', 'w')
f.setparams((numChan, dataSize, sampleRate, numSamples + int(samplesToDelayBy), "NONE", "Uncompressed"))
#f.writeframes(leftSound.tostring(), rightSound.tostring())
for left,right in zip(leftSound,rightSound):
    f.writeframes(struct.pack('h', int(right)))
    f.writeframes(struct.pack('h', int(left)))

f.close()