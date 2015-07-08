/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cpa.smgfork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sosy_lab.common.io.Files;
import org.sosy_lab.common.io.Path;
import org.sosy_lab.common.io.Paths;
import org.sosy_lab.cpachecker.cpa.smgfork.SMGTransferRelation.SMGKnownExpValue;
import org.sosy_lab.cpachecker.cpa.smgfork.SMGTransferRelation.SMGKnownSymValue;
import org.sosy_lab.cpachecker.cpa.smgfork.graphs.CLangSMG;
import org.sosy_lab.cpachecker.cpa.smgfork.objects.SMGObject;
import org.sosy_lab.cpachecker.cpa.smgfork.objects.SMGObjectVisitor;
import org.sosy_lab.cpachecker.cpa.smgfork.objects.SMGRegion;
import org.sosy_lab.cpachecker.cpa.smgfork.objects.sll.SMGSingleLinkedList;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sun.org.apache.xpath.internal.operations.Bool;

final class SMGObjectNode {
  private final String name;
  private final String definition;
  static private int counter = 0;

  public SMGObjectNode(String pType, String pDefinition) {
    name = "node_" + pType + "_" + counter++;
    definition = pDefinition;
  }

  public SMGObjectNode(String pName) {
    name = pName;
    definition = null;
  }

  public String getName() {
    return name;
  }

  public String getDefinition() {
    return name + "[" + definition + "];";
  }
}

class SMGNodeDotVisitor implements SMGObjectVisitor {
  final private CLangSMG smg;
  private SMGObjectNode node = null;

  public SMGNodeDotVisitor(CLangSMG pSmg) {
    smg = pSmg;
  }

  private String defaultDefinition(String pColor, String pShape, String pStyle, String pFontcolor,
      SMGObject pObject) {
    return "color=" + pColor + ", fontcolor=" + pFontcolor + ", shape=" + pShape + ", style=" + pStyle
        + ", label =\"" + pObject.toString() + "\"";
  }

  @Override
  public void visit(SMGRegion pRegion) {
    String shape = "rectangle";
    String color;
    String style;
    String fontcolor = smg.isAdded(pRegion) ? "green" : "black";
    if (smg.isRemoved(pRegion)) {
      color = "grey";
      style = "solid";
      fontcolor = "grey";
    } else if (smg.isObjectValid(pRegion)) {
      color = smg.isAdded(pRegion) ? "green" : "black";
      style = "solid";
    } else {
      color = "red";
      style = "dotted";
    }

    node = new SMGObjectNode("region", defaultDefinition(color, shape, style, fontcolor, pRegion));
  }

  @Override
  public void visit(SMGSingleLinkedList pSll) {
    String shape = "rectangle";
    String color = "blue";

    if (smg.isRemoved(pSll)) {
      color = " grey";
    } else if (! smg.isObjectValid(pSll)) {
      color="red";
    }

    String style = "dashed";
    String fontcolor = smg.isAdded(pSll) ? "green" : "black";
    node = new SMGObjectNode("sll", defaultDefinition(color, shape, style, fontcolor, pSll));
  }

  @Override
  public void visit (SMGObject pObject) {
    if (pObject.notNull()) {
      pObject.accept(this);
    } else {
      node = new SMGObjectNode("NULL");
    }
  }

  public SMGObjectNode getNode() {
    return node;
  }
}

public final class SMGPlotter {
  static public void debuggingPlot(CLangSMG pSmg, String pId, Map<SMGKnownSymValue, SMGKnownExpValue> explicitValues) throws IOException {
    Path exportSMGFilePattern = Paths.get("smg-debug-%s.dot");
    pId = pId.replace("\"", "");
    Path outputFile = Paths.get(String.format(exportSMGFilePattern.toAbsolutePath().getPath(), pId));
    SMGPlotter plotter = new SMGPlotter();

    Files.writeFile(outputFile, plotter.smgAsDot(pSmg, pId, "debug plot", explicitValues));
  }

  private final HashMap <SMGObject, SMGObjectNode> objectIndex = new HashMap<>();
  static private int nulls = 0;
  private int offset = 0;

  public SMGPlotter() {} /* utility class */

  static public String convertToValidDot(String original) {
    return original.replaceAll("[:]", "_");
  }

  public String smgAsDot(CLangSMG smg, String name, String location, Map<SMGKnownSymValue, SMGKnownExpValue> explicitValues) {
    StringBuilder sb = new StringBuilder();

    sb.append("digraph gr_" + name.replace('-', '_') + "{\n");
    offset += 2;
    sb.append(newLineWithOffset("label = \"Location: " + location.replace("\"", "\\\"") + "\";"));

    addStackSubgraph(smg, sb);

    SMGNodeDotVisitor visitor = new SMGNodeDotVisitor(smg);

    for (SMGObject heapObject : smg.getHeapObjects()) {
      if (! objectIndex.containsKey(heapObject)) {
        visitor.visit(heapObject);
        objectIndex.put(heapObject, visitor.getNode());
      }
      if (heapObject.notNull()) {
        sb.append(newLineWithOffset(objectIndex.get(heapObject).getDefinition()));
      }
    }

    for (SMGObject heapObject : smg.getRemovedHeapObjects()) {
      if (! objectIndex.containsKey(heapObject)) {
        visitor.visit(heapObject);
        objectIndex.put(heapObject, visitor.getNode());
      }
      if (heapObject.notNull()) {
        sb.append(newLineWithOffset(objectIndex.get(heapObject).getDefinition()));
      }
    }

    addGlobalObjectSubgraph(smg, sb);

    for (int value : smg.getValues()) {
      if (value != smg.getNullValue()) {
        sb.append(newLineWithOffset(smgValueAsDot(value, explicitValues, smg, false)));
      }
    }

    for (int value : smg.getRemovedValues()) {
      if (value != smg.getNullValue()) {
        sb.append(newLineWithOffset(smgValueAsDot(value, explicitValues, smg, true)));
      }
    }

    Set<Integer> processed = new HashSet<>();
    for (Integer value : smg.getValues()) {
      if (value != smg.getNullValue()) {
        for (Integer neqValue : smg.getNeqsForValue(value)) {
          if (! processed.contains(neqValue)) {
            sb.append(newLineWithOffset(neqRelationAsDot(value, neqValue)));
          }
        }
        processed.add(value);
      }
    }

    for (SMGEdgeHasValue edge: smg.getHVEdges()) {
      sb.append(newLineWithOffset(smgHVEdgeAsDot(edge, smg, false)));
    }

    for (SMGEdgePointsTo edge: smg.getPTEdges().values()) {
      if (edge.getValue() != smg.getNullValue()) {
        sb.append(newLineWithOffset(smgPTEdgeAsDot(edge, smg, false)));
      }
    }

    for (SMGEdgeHasValue edge: smg.getRemovedHvEdges()) {
      sb.append(newLineWithOffset(smgHVEdgeAsDot(edge, smg, true)));
    }

    for (SMGEdgePointsTo edge: smg.getRemovedPtEdges()) {
      if (edge.getValue() != smg.getNullValue()) {
        sb.append(newLineWithOffset(smgPTEdgeAsDot(edge, smg, true)));
      }
    }

    sb.append("}");

    return sb.toString();
  }

  private void addStackSubgraph(CLangSMG pSmg, StringBuilder pSb) {
    pSb.append(newLineWithOffset("subgraph cluster_stack {"));
    offset += 2;
    pSb.append(newLineWithOffset("label=\"Stack\";"));

    int i = pSmg.getStackFrames().size();
    int j = pSmg.getRemovedStackFrames().size() + i;
    for (CLangStackFrame stack_item : pSmg.getStackFrames()) {
      addStackItemSubgraph(pSmg, stack_item, pSb, i, false);
      i--;
    }
    for (CLangStackFrame stack_item : pSmg.getRemovedStackFrames()) {
      addStackItemSubgraph(pSmg, stack_item, pSb, j, true);
      j--;
    }
    offset -= 2;
    pSb.append(newLineWithOffset("}"));
  }

  private void addStackItemSubgraph(CLangSMG pSmg, CLangStackFrame pStackFrame, StringBuilder pSb,
      int pIndex, Boolean removed) {
    pSb.append(newLineWithOffset("subgraph cluster_stack_" + pStackFrame.getFunctionDeclaration().getName() + "{"));
    offset += 2;
    String color;
    if (removed) {
      color = "grey";
    } else {
      color = pSmg.isAdded(pStackFrame) ? "green" : "black";
    }
    pSb.append(newLineWithOffset("color=" + color + ";"));
    pSb.append(newLineWithOffset("fontcolor=blue;"));
    pSb.append(newLineWithOffset(
        "label=\"#" + pIndex + ": " + pStackFrame.getFunctionDeclaration().toASTString() + "\";"));

    HashMap<String, SMGRegion> to_print = new HashMap<>();
    to_print.putAll(pStackFrame.getVariables());

    SMGRegion returnObject = pStackFrame.getReturnObject();
    if (returnObject != null) {
      to_print.put(CLangStackFrame.RETVAL_LABEL, returnObject);
    }

    if (removed)
      pSb.append(smgScopeFrameAsDot(pSmg, to_print, String.valueOf(pIndex), true));
    else
      pSb.append(smgScopeFrameAsDot(pSmg, to_print, String.valueOf(pIndex), false));

    offset -= 2;
    pSb.append(newLineWithOffset("}"));

  }

  private String smgScopeFrameAsDot(CLangSMG pSmg, Map<String, SMGRegion> pNamespace, String pStructId,
      Boolean removed) {
    StringBuilder sb = new StringBuilder();

    sb.append(newLineWithOffset("struct" + pStructId + "[shape=none, label=<"));
    offset += 2;
    sb.append(newLineWithOffset("<table border=\"0\" cellborder=\"1\" cellpadding=\"9px\" cellspacing=\"0\"><tr>"));
    offset += 2;

    for (Entry<String, SMGRegion> entry : pNamespace.entrySet()) {
      String key = entry.getKey();
      SMGObject obj = entry.getValue();

      if (key.equals("node")) {
        // escape Node1
        key = "node1";
      }

      String color;
      if (removed){
        color = "grey";
      } else {
        color = pSmg.isAdded(obj) ? "green" : "black";
      }
      sb.append(newLineWithOffset("<td port=\"item_" + key + "\" color=\"" + color + "\">" +
          "<font color=\"" + color + "\">" + obj.toString() + "</font></td>"));
      objectIndex.put(obj, new SMGObjectNode("struct" + pStructId + ":item_" + key));
    }
    offset -= 2;
    sb.append(newLineWithOffset("</tr></table>>];"));
    offset -= 2;

    return sb.toString();
  }

  private void addGlobalObjectSubgraph(CLangSMG pSmg, StringBuilder pSb) {
    if (pSmg.getGlobalObjects().size() > 0) {
      pSb.append(newLineWithOffset("subgraph cluster_global{"));
      offset += 2;
      pSb.append(newLineWithOffset("label=\"Global objects\";"));
      pSb.append(newLineWithOffset(smgScopeFrameAsDot(pSmg, pSmg.getGlobalObjects(), "global", false)));
      offset -= 2;
      pSb.append(newLineWithOffset("}"));
    }
  }

  private static String newNullLabel() {
    SMGPlotter.nulls += 1;
    return "value_null_" + SMGPlotter.nulls;
  }

  private String smgHVEdgeAsDot(SMGEdgeHasValue pEdge, CLangSMG smg, Boolean removed) {
    String color;
    if (removed) {
      color = "grey";
    } else {
     color = smg.isAdded(pEdge) ? "green" : "black";
    }
    if (pEdge.getValue() == 0) {
      String newNull = newNullLabel();
      return newNull + "[color=" + color + ", fontcolor=" + color + ", shape=plaintext, label=\"NULL\"];" +
          objectIndex.get(pEdge.getObject()).getName() + " -> " + newNull + "[color=" + color +
          ", fontcolor=" + color + ", label=\"[" + pEdge.getOffset() + "]\"];";
    } else {
      return objectIndex.get(pEdge.getObject()).getName() + " -> value_" + pEdge.getValue() +
          "[color=" + color + ", fontcolor=" + color + ", label=\"[" + pEdge.getOffset() + "]\"];";
    }
  }

  private String smgPTEdgeAsDot(SMGEdgePointsTo pEdge, CLangSMG smg, Boolean removed) {
    String color;
    if (removed) {
      color = "grey";
    } else {
      color = smg.isAdded(pEdge) ? "green" : "black";
    }
    return "value_" + pEdge.getValue() + " -> " + objectIndex.get(pEdge.getObject()).getName() +
        "[color=" + color + ", fontcolor=" + color + ", label=\"+" + pEdge.getOffset() + "b\"];";
  }

  private static String smgValueAsDot(int value, Map<SMGKnownSymValue, SMGKnownExpValue> explicitValues,
      CLangSMG smg, Boolean removed) {

    String color;
    if (removed) {
      color = "grey";
    } else {
     color = smg.isAdded(value) ? "green" : "black";
    }

    String explicitValue = "";

    SMGKnownSymValue symValue =  SMGKnownSymValue.valueOf(value);

    if (explicitValues.containsKey(symValue)) {
      explicitValue = " : " + String.valueOf(explicitValues.get(symValue).getAsLong());
    }

    return "value_" + value + "[color=" + color + ", fontcolor=" + color + ", label=\"#" + value +
        explicitValue +  "\"];";
  }

  private static String neqRelationAsDot(Integer v1, Integer v2) {
    String targetNode;
    String returnString = "";
    if (v2.equals(0)) {
      targetNode = newNullLabel();
      returnString = targetNode + "[shape=plaintext, label=\"NULL\", fontcolor=\"red\"];\n";
    } else {
      targetNode = "value_" + v2;
    }
    return returnString + "value_" + v1 + " -> " + targetNode + "[color=\"red\", fontcolor=\"red\", label=\"neq\"]";
  }

  private String newLineWithOffset(String pLine) {
    return  Strings.repeat(" ", offset) + pLine + "\n";
  }
}
