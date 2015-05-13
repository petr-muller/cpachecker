#!/usr/bin/python

import os
import re
import json

# create directories if don't exist
dirMain = "../output/smg-html"
dirImg = "../output/smg-html/img"
dirOutput = "../output"
if not os.path.exists(dirMain):
    os.makedirs(dirMain)
if not os.path.exists(dirImg):
    os.makedirs(dirImg)

# clear main directory
for fileName in os.listdir(dirMain):
    filePath = os.path.join(dirMain, fileName)
    try:
        if os.path.isfile(filePath):
            os.unlink(filePath)
    except Exception, e:
        raise e

# clear img/ directory
for fileName in os.listdir(dirImg):
    filePath = os.path.join(dirImg, fileName)
    try:
        if os.path.isfile(filePath):
            os.unlink(filePath)
    except Exception, e:
        raise e


# class for tree representing the SMG anaysis structure
class SMGDot(object):
    def __init__(self):
        self.name = None
        self.number = None

# list to store SMGs
dotList = []


# parse dot file name, create corresponding SMGDot structure and add it to the list
def parseName(name):
    nameRegex = re.compile("\d+")
    indexes = nameRegex.findall(name)

    # create new SMG dot object
    dot = SMGDot()
    dot.name = name

    if "initial" in name:
        # initial SMG
        dot.number = indexes[0]
        dotList.append(dot)
    else:
        # other SMGs
        dot.number = indexes[1]
        dotList.append(dot)


# create images from dot files and save them to the list
dirOutput = "../output"
smgRegex = re.compile("^smg.*\.dot$")
for fileName in sorted(os.listdir(dirOutput)):
    if smgRegex.match(fileName):
        filePath = os.path.join(dirOutput, fileName)
        (name, ext) = os.path.splitext(fileName)
        destPath = os.path.join(dirImg, name + ".png")
        dotCmd = "dot -Tpng " + filePath + " -o " + destPath
        os.system(dotCmd)
        # create new dot list item
        parseName(name)

# open JSON file and correct it to be valid
os.chdir(dirOutput)
with open("visualisationSchema.json", "rb+") as mapFile:
    mapFile.seek(-2, os.SEEK_END)
    if mapFile.readline() == ",\n":
        mapFile.seek(-2, os.SEEK_END)
        mapFile.truncate()
        with open("visualisationSchema.json", "a") as mapFile:
            mapFile.write("]")


# class for tree representing analysis map
class SMGState(object):
    def __init__(self):
        self.number = None
        self.fileName = None
        self.succ = None
        self.pred = None


# parse json file
with open("visualisationSchema.json") as mapFile:
    states = json.load(mapFile)


def getDotName(number):
    for dot in dotList:
        if dot.number == number:
            return dot.name
    return None

# create SMG states into dictionary from parsed JSON
smgDict = {}
for state in states:
    if state['state'] not in smgDict:
        # create new SMG state object
        smg = SMGState()
        smg.number = state['state']
        smg.succ = state['succ']
        smg.succ[:] = [succ for succ in smg.succ if not succ == smg.number]  # remove self-loops
        smg.fileName = getDotName(smg.number)
        # put SMG object into dictionary
        smgDict[smg.number] = smg
    else:
        # object for given state already exists - add new successors
        smgDict[state['state']].succ.extend(state['succ'])

# create predecessors
for number, smg in smgDict.items():
    if smg.succ:
        for succ in smg.succ:
            if succ in smgDict:
                smgDict[succ].pred = number

# delete states which do not have dot files created
for number, smg in sorted(smgDict.items()):
    if not smg.fileName:
        # edit predecessor succesors
        if smg.pred in smgDict:
            smgDict[smg.pred].succ.remove(number)
            smgDict[smg.pred].succ.extend(smg.succ)
        # edit successors predecesors
        for succ in smg.succ:
            if succ in smgDict:
                smgDict[succ].pred = smg.pred
        # delete state
        del smgDict[number]

# create ending states (they are not in the dictionary, but have corresponding dot)
for number, smg in smgDict.items():
    for succ in smg.succ:
        if succ not in smgDict:
            # create new SMG state item
            newSmg = SMGState()
            newSmg.number = succ
            newSmg.fileName = getDotName(newSmg.number)
            newSmg.pred = number
            # add new SMG to dictionary
            smgDict[newSmg.number] = newSmg

# print the final tree structure
# for number, smg in sorted(smgDict.items()):
#     print smg.number + ":"
#     print smg.fileName
#     if smg.succ:
#         for succ in smg.succ:
#             print succ
#     print "\n"


# create html files
def getHtmlFile(name):
    return smgDict[name].fileName + ".html"


def getStateLabel(name):
    filePath = os.path.join(dirOutput, smgDict[name].fileName + ".dot")
    with open(filePath, "r") as dotFile:
        dotFile.readline()
        line = dotFile.readline()
    locRegex = re.compile("^.*Location: (.*)\";$")
    match = locRegex.match(line)
    return match.group(1)


for number, smg in smgDict.items():
    htmlText = """<html>
    <head></head>
    <body>
    """
    # predecessor link
    if smg.pred:
        htmlText += """
            <a href="{}">{}</a><br>
        """.format(getHtmlFile(smg.pred), "Previous state (" + getStateLabel(smg.pred) + ")")
    # successors links
    if smg.succ:
        for succ in smg.succ:
            htmlText += """
                <a href="{}">{}</a><span style="display:inline-block; width: 50px;"></span>
            """.format(getHtmlFile(succ), "Next state (" + getStateLabel(succ) + ")")
    # image
    htmlText += """
        <br>
        <img src="img/{}" />
    """.format(smg.fileName + ".png")
    htmlText += """
    </body>
    </html>
    """
    with open(os.path.join(dirMain, getHtmlFile(number)), "w") as htmlFile:
        htmlFile.write(htmlText)
