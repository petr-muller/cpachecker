/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2013  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.automaton;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.sosy_lab.cpachecker.cfa.ast.c.CAstNode;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.CLabelNode;
import org.sosy_lab.cpachecker.core.interfaces.AbstractQueryableState;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonASTComparator.ASTMatcher;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.exceptions.InvalidQueryException;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCFAEdgeException;
import org.sosy_lab.cpachecker.util.AbstractStates;

import com.google.common.base.Optional;

/**
 * Implements a boolean expression that evaluates and returns a <code>MaybeBoolean</code> value when <code>eval()</code> is called.
 * The Expression can be evaluated multiple times.
 */
interface AutomatonBoolExpr extends AutomatonExpression {
  static final ResultValue<Boolean> CONST_TRUE = new ResultValue<>(Boolean.TRUE);
  static final ResultValue<Boolean> CONST_FALSE = new ResultValue<>(Boolean.FALSE);

  @Override
  abstract ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException;

  public class MatchProgramExit implements AutomatonBoolExpr {

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      if (pArgs.getCfaEdge().getSuccessor().getNumLeavingEdges() == 0) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

  }

  /**
   * Implements a regex match on the label after the current CFAEdge.
   * The eval method returns false if there is no label following the CFAEdge.
   * (".*" in java-regex means "any characters")
   */
  static class MatchLabelRegEx implements AutomatonBoolExpr {

    private final Pattern pattern;

    public MatchLabelRegEx(String pPattern) {
      pattern = Pattern.compile(pPattern);
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      CFANode successorNode = pArgs.getCfaEdge().getSuccessor();
      if (successorNode instanceof CLabelNode) {
        String label = ((CLabelNode)successorNode).getLabel();
        if (pattern.matcher(label).matches()) {
          return CONST_TRUE;
        } else {
          return CONST_FALSE;
        }
      } else {
        return CONST_FALSE;
        //return new ResultValue<>("cannot evaluate if the CFAEdge is not a CLabelNode", "MatchLabelRegEx.eval(..)");
      }
    }

    @Override
    public String toString() {
      return "MATCH LABEL [" + pattern + "]";
    }
  }


  /**
   * This is a efficient implementation of the ASTComparison (it caches the generated ASTs for the pattern).
   * It also displays error messages if the AST contains problems/errors.
   * The AST Comparison evaluates the pattern (coming from the Automaton Definition) and the C-Statement on the CFA Edge to ASTs and compares these with a Tree comparison algorithm.
   */
  static class MatchCFAEdgeASTComparison implements AutomatonBoolExpr {

    private final ASTMatcher patternAST;

    public MatchCFAEdgeASTComparison(ASTMatcher pPatternAST) throws InvalidAutomatonException {
      this.patternAST = pPatternAST;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws UnrecognizedCFAEdgeException {
      Optional<?> ast = pArgs.getCfaEdge().getRawAST();
      if (ast.isPresent()) {
        if (!(ast.get() instanceof CAstNode)) {
          throw new UnrecognizedCFAEdgeException(pArgs.getCfaEdge());
        }
        // some edges do not have an AST node attached to them, e.g. BlankEdges
        if (patternAST.matches((CAstNode)ast.get(), pArgs)) {
          return CONST_TRUE;
        } else {
          return CONST_FALSE;
        }
      }
      return CONST_FALSE;
    }

    @Override
    public String toString() {
      return "MATCH {" + patternAST + "}";
    }
  }


  static class MatchCFAEdgeRegEx implements AutomatonBoolExpr {

    private final Pattern pattern;

    public MatchCFAEdgeRegEx(String pPattern) {
      pattern = Pattern.compile(pPattern);
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      if (pattern.matcher(pArgs.getCfaEdge().getRawStatement()).matches()) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return "MATCH [" + pattern + "]";
    }
  }


  static class MatchCFAEdgeExact implements AutomatonBoolExpr {

    private final String pattern;

    public MatchCFAEdgeExact(String pPattern) {
      pattern = pPattern;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      if (pArgs.getCfaEdge().getRawStatement().equals(pattern)) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return "MATCH \"" + pattern + "\"";
    }
  }

  /**
   * Sends a query string to all available AbstractStates.
   * Returns TRUE if one Element returned TRUE;
   * Returns FALSE if all Elements returned either FALSE or an InvalidQueryException.
   * Returns MAYBE if no Element is available or the Variables could not be replaced.
   */
  public static class ALLCPAQuery implements AutomatonBoolExpr {
    private final String queryString;

    public ALLCPAQuery(String pString) {
      queryString = pString;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      if (pArgs.getAbstractStates().isEmpty()) {
        return new ResultValue<>("No CPA elements available", "AutomatonBoolExpr.ALLCPAQuery");
      } else {
        // replace transition variables
        String modifiedQueryString = pArgs.replaceVariables(queryString);
        if (modifiedQueryString == null) {
          return new ResultValue<>("Failed to modify queryString \"" + queryString + "\"", "AutomatonBoolExpr.ALLCPAQuery");
        }
        for (AbstractState ae : pArgs.getAbstractStates()) {
          if (ae instanceof AbstractQueryableState) {
            AbstractQueryableState aqe = (AbstractQueryableState) ae;
            try {
              Object result = aqe.evaluateProperty(modifiedQueryString);
              if (result instanceof Boolean) {
                if (((Boolean)result).booleanValue()) {
                  String message = "CPA-Check succeeded: ModifiedCheckString: \"" +
                  modifiedQueryString + "\" CPAElement: (" + aqe.getCPAName() + ") \"" +
                  aqe.toString() + "\"";
                  pArgs.getLogger().log(Level.FINER, message);
                  return CONST_TRUE;
                }
              }
            } catch (InvalidQueryException e) {
              // do nothing;
            }
          }
        }
        return CONST_FALSE;
      }
    }
  }
  /**
   * Sends a query-String to an <code>AbstractState</code> of another analysis and returns the query-Result.
   */
  static class CPAQuery implements AutomatonBoolExpr {
    private final String cpaName;
    private final String queryString;

    public CPAQuery(String pCPAName, String pQuery) {
      cpaName = pCPAName;
      queryString = pQuery;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      // replace transition variables
      String modifiedQueryString = pArgs.replaceVariables(queryString);
      if (modifiedQueryString == null) {
        return new ResultValue<>("Failed to modify queryString \"" + queryString + "\"", "AutomatonBoolExpr.CPAQuery");
      }

      for (AbstractState ae : pArgs.getAbstractStates()) {
        if (ae instanceof AbstractQueryableState) {
          AbstractQueryableState aqe = (AbstractQueryableState) ae;
          if (aqe.getCPAName().equals(cpaName)) {
            try {
              Object result = aqe.evaluateProperty(modifiedQueryString);
              if (result instanceof Boolean) {
                if (((Boolean)result).booleanValue()) {
                  String message = "CPA-Check succeeded: ModifiedCheckString: \"" +
                  modifiedQueryString + "\" CPAElement: (" + aqe.getCPAName() + ") \"" +
                  aqe.toString() + "\"";
                  pArgs.getLogger().log(Level.FINER, message);
                  return CONST_TRUE;
                } else {
                  String message = "CPA-Check failed: ModifiedCheckString: \"" +
                  modifiedQueryString + "\" CPAElement: (" + aqe.getCPAName() + ") \"" +
                  aqe.toString() + "\"";
                  pArgs.getLogger().log(Level.FINER, message);
                  return CONST_FALSE;
                }
              } else {
                pArgs.getLogger().log(Level.WARNING,
                    "Automaton got a non-Boolean value during Query of the "
                    + cpaName + " CPA on Edge " + pArgs.getCfaEdge().getDescription() +
                    ". Assuming FALSE.");
                return CONST_FALSE;
              }
            } catch (InvalidQueryException e) {
              pArgs.getLogger().logException(Level.WARNING, e,
                  "Automaton encountered an Exception during Query of the "
                  + cpaName + " CPA on Edge " + pArgs.getCfaEdge().getDescription());
              return CONST_FALSE;
            }
          }
        }
      }
      return new ResultValue<>("No State of CPA \"" + cpaName + "\" was found!", "AutomatonBoolExpr.CPAQuery");
    }

    @Override
    public String toString() {
      return "CHECK(" + cpaName + "(\"" + queryString + "\"))";
    }
  }

  static enum CheckAllCpasForTargetState implements AutomatonBoolExpr {
    INSTANCE;

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      if (pArgs.getAbstractStates().isEmpty()) {
        return new ResultValue<>("No CPA elements available", "AutomatonBoolExpr.CheckAllCpasForTargetState");
      } else {
        for (AbstractState ae : pArgs.getAbstractStates()) {
          if (AbstractStates.isTargetState(ae)) {
            return CONST_TRUE;
          }
        }
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return "CHECK(IS_TARGET_STATE)";
    }
  }

  /** Constant for true.
   */
  static AutomatonBoolExpr TRUE = new AutomatonBoolExpr() {
    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      return CONST_TRUE;
    }

    @Override
    public String toString() {
      return "TRUE";
    }
  };

  /** Constant for false.
   */
  static AutomatonBoolExpr FALSE = new AutomatonBoolExpr() {
    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      return CONST_FALSE;
    }

    @Override
    public String toString() {
      return "FALSE";
    }
  };


  /** Tests the equality of the values of two instances of {@link AutomatonIntExpr}.
   */
  static class IntEqTest implements AutomatonBoolExpr {

    private final AutomatonIntExpr a;
    private final AutomatonIntExpr b;

    public IntEqTest(AutomatonIntExpr pA, AutomatonIntExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      ResultValue<Integer> resA = a.eval(pArgs);
      ResultValue<Integer> resB = b.eval(pArgs);
      if (resA.canNotEvaluate()) {
        return new ResultValue<>(resA);
      }
      if (resB.canNotEvaluate()) {
        return new ResultValue<>(resB);
      }
      if (resA.getValue().equals(resB.getValue())) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return a + " == " + b;
    }
  }


  /** Tests whether two instances of {@link AutomatonIntExpr} evaluate to different integers.
   */
  static class IntNotEqTest implements AutomatonBoolExpr {

    private final AutomatonIntExpr a;
    private final AutomatonIntExpr b;

    public IntNotEqTest(AutomatonIntExpr pA, AutomatonIntExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) {
      ResultValue<Integer> resA = a.eval(pArgs);
      ResultValue<Integer> resB = b.eval(pArgs);
      if (resA.canNotEvaluate()) {
        return new ResultValue<>(resA);
      }
      if (resB.canNotEvaluate()) {
        return new ResultValue<>(resB);
      }
      if (! resA.getValue().equals(resB.getValue())) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return a + " != " + b;
    }
  }


  /** Computes the disjunction of two {@link AutomatonBoolExpr} (lazy evaluation).
   */
  static class Or implements AutomatonBoolExpr {

    private final AutomatonBoolExpr a;
    private final AutomatonBoolExpr b;

    public Or(AutomatonBoolExpr pA, AutomatonBoolExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    public @Override ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      /* OR:
       * True  || _ -> True
       * _ || True -> True
       * false || false -> false
       * every other combination returns the result that can not evaluate
       */
      ResultValue<Boolean> resA = a.eval(pArgs);
      if (resA.canNotEvaluate()) {
        ResultValue<Boolean> resB = b.eval(pArgs);
        if ((!resB.canNotEvaluate()) && resB.getValue().equals(Boolean.TRUE)) {
          return resB;
        } else {
          return resA;
        }
      } else {
        if (resA.getValue().equals(Boolean.TRUE)) {
          return resA;
        } else {
          ResultValue<Boolean> resB = b.eval(pArgs);
          if (resB.canNotEvaluate()) {
            return resB;
          }
          if (resB.getValue().equals(Boolean.TRUE)) {
            return resB;
          } else {
            return resA;
          }
        }
      }
    }

    @Override
    public String toString() {
      return "(" + a + " || " + b + ")";
    }
  }


  /** Computes the conjunction of two {@link AutomatonBoolExpr} (lazy evaluation).
   */
  static class And implements AutomatonBoolExpr {

    private final AutomatonBoolExpr a;
    private final AutomatonBoolExpr b;

    public And(AutomatonBoolExpr pA, AutomatonBoolExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      /* AND:
       * false && _ -> false
       * _ && false -> false
       * true && true -> true
       * every other combination returns the result that can not evaluate
       */
      ResultValue<Boolean> resA = a.eval(pArgs);
      if (resA.canNotEvaluate()) {
        ResultValue<Boolean> resB = b.eval(pArgs);
        if ((! resB.canNotEvaluate()) && resB.getValue().equals(Boolean.FALSE)) {
          return resB;
        } else {
          return resA;
        }
      } else {
        if (resA.getValue().equals(Boolean.FALSE)) {
          return resA;
        } else {
          ResultValue<Boolean> resB = b.eval(pArgs);
          if (resB.canNotEvaluate()) {
            return resB;
          }
          if (resB.getValue().equals(Boolean.FALSE)) {
            return resB;
          } else {
            return resA;
          }
        }
      }
    }

    @Override
    public String toString() {
      return "(" + a + " && " + b + ")";
    }
  }


  /**
   * Negates the result of a {@link AutomatonBoolExpr}. If the result is MAYBE it is returned unchanged.
   */
  static class Negation implements AutomatonBoolExpr {

    private final AutomatonBoolExpr a;

    public Negation(AutomatonBoolExpr pA) {
      this.a = pA;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      ResultValue<Boolean> resA = a.eval(pArgs);
      if (resA.canNotEvaluate()) {
        return resA;
      }
      if (resA.getValue().equals(Boolean.TRUE)) {
        return CONST_FALSE;
      } else {
        return CONST_TRUE;
      }
    }

    @Override
    public String toString() {
      return "!" + a;
    }
  }


  /**
   * Boolean Equality
   */
  static class BoolEqTest implements AutomatonBoolExpr {

    private final AutomatonBoolExpr a;
    private final AutomatonBoolExpr b;

    public BoolEqTest(AutomatonBoolExpr pA, AutomatonBoolExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      ResultValue<Boolean> resA = a.eval(pArgs);
      if (resA.canNotEvaluate()) {
        return resA;
      }
      ResultValue<Boolean> resB = b.eval(pArgs);
      if (resB.canNotEvaluate()) {
        return resB;
      }
      if (resA.getValue().equals(resB.getValue())) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return a + " == " + b;
    }
  }


  /**
   * Boolean !=
   */
  static class BoolNotEqTest implements AutomatonBoolExpr {

    private final AutomatonBoolExpr a;
    private final AutomatonBoolExpr b;

    public BoolNotEqTest(AutomatonBoolExpr pA, AutomatonBoolExpr pB) {
      this.a = pA;
      this.b = pB;
    }

    @Override
    public ResultValue<Boolean> eval(AutomatonExpressionArguments pArgs) throws CPATransferException {
      ResultValue<Boolean> resA = a.eval(pArgs);
      if (resA.canNotEvaluate()) {
        return resA;
      }
      ResultValue<Boolean> resB = b.eval(pArgs);
      if (resB.canNotEvaluate()) {
        return resB;
      }
      if (! resA.getValue().equals(resB.getValue())) {
        return CONST_TRUE;
      } else {
        return CONST_FALSE;
      }
    }

    @Override
    public String toString() {
      return a + " != " + b;
    }
  }
}
