/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2012  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.bdd;

import java.util.Set;

import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.util.predicates.NamedRegionManager;
import org.sosy_lab.cpachecker.util.predicates.interfaces.Region;

public class BDDState implements AbstractState {

  private Region currentState;
  private final NamedRegionManager manager;
  private Set<String> currentVars;
  private BDDState functionCallState;
  private String functionName;

  public BDDState(NamedRegionManager mgr, BDDState functionCallElement,
      Region state, Set<String> vars, String functionName) {
    this.currentState = state;
    this.currentVars = vars;
    this.functionCallState = functionCallElement;
    this.functionName = functionName;
    this.manager = mgr;
  }

  public Region getRegion() {
    return currentState;
  }

  public Set<String> getVars() {
    return currentVars;
  }

  public BDDState getFunctionCallState() {
    return functionCallState;
  }

  public String getFunctionName() {
    return functionName;
  }

  public boolean isLessOrEqual(BDDState other) {
    assert this.functionName.equals(other.functionName) : "same function needed: "
        + this.functionName + " vs " + other.functionName;

    return manager.entails(this.currentState, other.currentState);
  }

  public BDDState join(BDDState other) {
    assert this.functionName.equals(other.functionName) : "same function needed: "
        + this.functionName + " vs " + other.functionName;
    this.currentVars.addAll(other.currentVars); // some vars more make no difference

    Region result = manager.makeOr(this.currentState, other.currentState);

    // FIRST check the other element
    if (result.equals(other.currentState)) {
      return other;

      // THEN check this element
    } else if (result.equals(this.currentState)) {
      return this;

    } else {
      return new BDDState(this.manager, this.functionCallState, result,
          this.currentVars, this.functionName);
    }
  }

  @Override
  public String toString() {
    return manager.dumpRegion(currentState) + "\n"
        + manager.regionToDot(currentState);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof BDDState) {
      BDDState other = (BDDState) o;
      return this.functionName.equals(other.functionName) &&
          this.currentState.equals(other.currentState);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return currentState.hashCode();
  }
}