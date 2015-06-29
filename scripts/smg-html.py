#!/usr/bin/python

import os
import re
import json
import argparse
import shutil
import subprocess


# directory type for argument parsing
def rw_directory(directory):
    if not os.path.isdir(directory):
        raise argparse.ArgumentTypeError("{0} is not a valid directory".format(directory))
    if not os.access(directory, os.W_OK):
        raise argparse.ArgumentTypeError("{0} is not writable".format(directory))
    if not os.access(directory, os.R_OK):
        raise argparse.ArgumentTypeError("{0} is not readable".format(directory))
    return directory

# parsing input arguments
parser = argparse.ArgumentParser(description="Visualises CPAlien analysis using HTML")
parser.add_argument("dir", help="location of dot files to be visualised", type=rw_directory)

args = parser.parse_args()

# delete existing directories and recreate them
dirMain = os.path.abspath(os.path.join(args.dir, "smg-html/"))
dirImg = os.path.abspath(os.path.join(args.dir, "smg-html/img/"))
dirOutput = os.path.abspath(args.dir)

if os.path.exists(dirMain):
    shutil.rmtree(dirMain)
os.makedirs(dirMain)
os.makedirs(dirImg)


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
smgRegex = re.compile("^smg.*\.dot$")
for fileName in sorted(os.listdir(dirOutput)):
    if smgRegex.match(fileName):
        filePath = os.path.join(dirOutput, fileName)
        (name, ext) = os.path.splitext(fileName)
        destPath = os.path.join(dirImg, name + ".svg")
        dotCmd = "dot -Tsvg " + filePath + " -o " + destPath
        subprocess.Popen(dotCmd.split())
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
    """.format(smg.fileName + ".svg")
    htmlText += """
    </body>
    </html>
    """
    with open(os.path.join(dirMain, getHtmlFile(number)), "w") as htmlFile:
        htmlFile.write(htmlText)
