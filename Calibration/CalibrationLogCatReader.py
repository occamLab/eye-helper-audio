import csv

Distance = 14

calibValues = []
heightAndWidthStrings = []

for lines in open('LogOutput%sIn.txt' %Distance, 'r'):
	#print lines
	if 'Width' in lines:
		heightAndWidthStrings.append(lines[93:])
		print heightAndWidthStrings[-1]
		splitString = heightAndWidthStrings[-1].split(' ')
		lastVal = splitString[-1]
		if lastVal[-1] == '\n':
			lastVal = lastVal[0:len(lastVal) -1]
		calibValues.append((Distance, splitString[2], lastVal))
		print calibValues[-1]

newFile = open('LogOutput%sIn.csv' %Distance, 'w')
newWriter = csv.writer(newFile)
for eachRow in calibValues:
	newWriter.writerow(eachRow)