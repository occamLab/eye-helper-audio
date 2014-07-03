# generate wav file containing sine waves
# FB36 - 20120617
import math
import wave
import array
import pyaudio

duration = 3 # seconds
freq = 440 # of cycles per second (Hz) (frequency of the sine waves)
volume = 100 # percent
delayR = 0
delayL = 100
data = array.array('h') # signed short integer (-32768 to 32767) data
sampleRate = 44100 # of samples per second (standard)
numChan = 2 # of channels (1: mono, 2: stereo)
dataSize = 2 # 2 bytes because of using signed short integers => bit depth = 16
numSamplesPerCyc = int(sampleRate / freq)
numSamples = sampleRate * duration
for i in range(numSamples):
    sample1 = 32767 * float(volumeL) / 100
    sample2 = 32767 * float(volumeR) / 100
    sample1 *= math.sin(math.pi * 2 * (i % numSamplesPerCyc) / numSamplesPerCyc)
    sample2 *= math.sin(math.pi * 2 * (i % numSamplesPerCyc) / numSamplesPerCyc)
    data.append(int(sample1))
    data.append(int(sample2))
f = wave.open('test_file.wav', 'w')
f.setparams((numChan, dataSize, sampleRate, numSamples, "NONE", "Uncompressed"))
f.writeframes(data.tostring())
f.close()

CHUNK = 1024

wf = wave.open('test_file.wav', 'rb')

p = pyaudio.PyAudio()

stream = p.open(format=p.get_format_from_width(wf.getsampwidth()),
                channels=wf.getnchannels(),
                rate=wf.getframerate(),
                output=True)

data = wf.readframes(CHUNK)

while data != '':
    stream.write(data)
    data = wf.readframes(CHUNK)

stream.stop_stream()
stream.close()

p.terminate()