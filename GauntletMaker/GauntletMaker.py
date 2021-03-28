import svgutils.transform as sg
import sys
import re

def UpdateGaunt(gauntPath, decoPath, outputPath, decoBelow, rotation, xOffset, yOffset, xScale, yScale):
    gaunt = sg.fromfile(gauntPath)
    deco = sg.fromfile(decoPath)

    #Not actualy sure what this does.
    gauntPlot = gaunt.getroot()
    decoPlot = deco.getroot()

    #Reposition and rotate the Deco.
    decoPlot.moveto(xOffset, yOffset, xScale, yScale)
    decoPlot.rotate(rotation)

    gauntWidth = gaunt.width
    gauntHeight = gaunt.height

    #Generate a "Figure" that is the Gaunt and Deco combined.
    finGaunt = sg.SVGFigure(gauntWidth, gauntHeight)

    if(decoBelow):
        finGaunt.append([decoPlot, gauntPlot])
    else:
        finGaunt.append([gauntPlot, decoPlot])


    #print(finGaunt.get_size())

    finGaunt.save(outputPath)

row = []
refFile = open("data/Reference.txt", 'r')

#52524b
mainColor = "#cf0e48"
#2c2c27
shadowColor = "#761432"

i = 0
files = refFile.readlines() 
while i < 33:
    #Path to Base Fingers
    gauntPath = "data/GauntsBase/" + str(i) + ".svg"

    #Data from Reference.txt
    thisLine = files[i].split(",")
    #Remove the /n at the end of thisLine[5]
    thisLine[5] = thisLine[5][0:len(thisLine[5]) - 1]
    print(thisLine)

    #Path to the Decoration to add.
    decoPath = "data/GauntsDeco/" + thisLine[0] + "Angle.svg"

    #Path to output file.
    outputPath = "data/finished/" + str(i) + ".svg"
    
    #Decide based on Reference file if the Decoration should be below the Fingers.
    decoBelow = True
    if thisLine[0] == "G" or thisLine[0] == "G2" or thisLine[0] == "W":
        decoBelow = False

    UpdateGaunt(gauntPath, decoPath, outputPath, decoBelow, float(thisLine[3]), thisLine[1], thisLine[2], float(thisLine[4]), float(thisLine[5]))

    #Begin finger recoloring.
    with open(outputPath,"r") as f:
        newline=[]
        for word in f.readlines():        
            newline.append(word.replace("#52524b",mainColor))  # Replace Light   
    
    with open(outputPath,"w") as f:
        for line in newline:
            f.writelines(line)

    #I don't like running this twice for LIght and Shadow, but I also don't care enough to figure out a better way.
    with open(outputPath,"r") as f:
        newline=[]
        for word in f.readlines():        
            newline.append(word.replace("#2c2c27",shadowColor))  # Replace Shadow  
    
    with open(outputPath,"w") as f:
        for line in newline:
            f.writelines(line)

    i += 1

print("Done")