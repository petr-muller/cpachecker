/*
 * CPAchecker is a tool for configurable software verification.
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
package org.sosy_lab.cpachecker.util.predicates;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.exceptions.SolverException;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.FormulaManagerView;

/**
 * The class <code>PredicatePartition</code> represents a partition of predicates that are similar to each other.
 * Two predicates are similar if they have at least one variable in common.
 * <p/>
 * It is used by the {@link AbstractionManager} to generate a variable ordering for BDDs where a BDD variable
 * represents a predicate.
 */
public class PredicatePartition {
  private static int partitionCounter = 0;

  private final LogManager logger;
  private final int partitionID = partitionCounter++;
  private final FormulaManagerView fmgr;
  private LinkedList<AbstractionPredicate> predicates;
  // mapping varID -> predicate in partition
  private HashMap<Integer, AbstractionPredicate> varIDToPredicate;
  private HashMap<Integer, HashMap<Integer, Integer>> similarityRelation;
  private Solver solver;
  private PriorityQueue<AbstractionPredicate> predicatesFreq;

  public PredicatePartition(FormulaManagerView fmgr, Solver solver, LogManager logger) {
    this.fmgr = fmgr;
    this.solver = solver;
    this.logger = logger;
    predicates = new LinkedList<>();
    varIDToPredicate = new HashMap<>();
    similarityRelation = new HashMap<>();

    // priority queue for sort by number of variables
    Comparator<AbstractionPredicate> comparator = new Comparator<AbstractionPredicate>() {

      @Override
      public int compare(AbstractionPredicate pred1, AbstractionPredicate pred2) {
        return pred2.getVariableNumber() - pred1.getVariableNumber();
      }};
    predicatesFreq = new PriorityQueue<>(1, comparator );
  }

  /**
   * Inserts a new predicate in the partition using a certain insertion method.
   *
   * @param newPred the predicate that should be inserted.
   */
  public void insertPredicate(AbstractionPredicate newPred) {
    varIDToPredicate.put(newPred.getVariableNumber(), newPred);  // has to be done anyway
    //insertPredicateByImplication(newPred);
    //insertPredicateByImplicationReversed(newPred);
    //insertPredicateBySimilarity(newPred);
    insertPredicateByNumberOfVariables(newPred);
  }

  /**
   * Inserts a new predicate such that predicates with a higher number of variables are left
   * and predicates with a lower number of variables are right of the new predicate.
   *
   * @param newPred the new predicate to be inserted in the partition
   */
  private void insertPredicateByNumberOfVariables(AbstractionPredicate newPred) {
    predicatesFreq.add(newPred);
  }

  /**
   * Inserts a new predicate before the most left predicate of the partition that is implied by the new predicate.
   *
   * @param newPred the predicate that should be inserted.
   */
  private void insertPredicateByImplication(AbstractionPredicate newPred) {
    // solver does caching
    // find lowest position of a predicate that is implied by newPred, insert newPred before that predicate
    int lowestImplied = predicates.size();
    int elementIndex = predicates.size() - 1;
    LinkedList<AbstractionPredicate> predicatesCopy = new LinkedList<>(predicates);
    Collections.reverse(predicatesCopy);
    for (AbstractionPredicate oldPred : predicatesCopy) {
      try {
        if (solver.implies(newPred.getSymbolicAtom(), oldPred.getSymbolicAtom())) {
          lowestImplied = elementIndex;
        }

        if (solver.implies(oldPred.getSymbolicAtom(), newPred.getSymbolicAtom())) {
          break;
        }

        elementIndex--;
      } catch (SolverException | InterruptedException e) {
        logger.log(java.util.logging.Level.WARNING, "Error while adding the predicate ", newPred, " by implications to the list of predicates");
      }
    }

    predicates.add(lowestImplied, newPred);
  }

  /**
   * Inserts a new predicate before the most left predicate of the partition that implies the new predicate.
   *
   * @param newPred the predicate that should be inserted.
   */
  private void insertPredicateByImplicationReversed(AbstractionPredicate newPred) {
    // solver does caching
    // find lowest position of a predicate that is implied by newPred, insert newPred before that predicate
    int lowestImplier = predicates.size();
    int elementIndex = predicates.size() - 1;
    LinkedList<AbstractionPredicate> predicatesCopy = new LinkedList<>(predicates);
    Collections.reverse(predicatesCopy);
    for (AbstractionPredicate oldPred : predicatesCopy) {
      try {
        if (solver.implies(oldPred.getSymbolicAtom(), newPred.getSymbolicAtom())) {
          lowestImplier = elementIndex;
        }
        if (solver.implies(newPred.getSymbolicAtom(), oldPred.getSymbolicAtom())) {
          break;
        }

        elementIndex--;
      } catch (SolverException | InterruptedException e) {
        logger.log(java.util.logging.Level.WARNING, "Error while adding the predicate ", newPred, " by implications to the list of predicates");
      }
    }

    predicates.add(lowestImplier, newPred);
  }

  /**
   * Inserts a new predicate next to the predicate of the partition that is most similar to the new one.
   *
   * @param newPred the new predicate that should be inserted.
   */
  private void insertPredicateBySimilarity(AbstractionPredicate newPred) {
    // first update the predicate similarities
    updatePredicateSimilarities(newPred);

    // calculate the predicate that is most similar to the new one
    HashMap<Integer, Integer> similarities = similarityRelation.get(newPred.getVariableNumber());
    Map.Entry<Integer, Integer> entryWithMaxSimilarity = null;
    for (Map.Entry<Integer, Integer> entry : similarities.entrySet()) {
      if (entryWithMaxSimilarity == null || entry.getValue().compareTo(entryWithMaxSimilarity.getValue()) > 0) {
        entryWithMaxSimilarity = entry;
      }
    }

    if (entryWithMaxSimilarity != null) {
      AbstractionPredicate mostSimilarPredicate = varIDToPredicate.get(entryWithMaxSimilarity.getKey());
      int indexSimilarPred = predicates.indexOf(mostSimilarPredicate);

      // the new predicate is inserted before the most similar if it contains more variables else it's the other way round
      if (fmgr.extractVariableNames(mostSimilarPredicate.getSymbolicAtom()).size() <
          fmgr.extractVariableNames(newPred.getSymbolicAtom()).size()) {
        predicates.add(indexSimilarPred, newPred);
      } else {
        predicates.add(indexSimilarPred + 1, newPred);
      }
    } else {
      predicates.add(newPred);
    }
  }

  /**
   * Calculates the similarity of the new predicate and old predicates in the partition and updates the similarity
   * relationship.
   *
   * @param newPredicate the new predicated that arrived.
   */
  private void updatePredicateSimilarities(AbstractionPredicate newPredicate) {
    int varIDNewPredicate = newPredicate.getVariableNumber();
    HashMap<Integer, Integer> similarities = new HashMap<>();
    Set<String> varsInNewPred = fmgr.extractVariableNames(newPredicate.getSymbolicAtom());

    // calculate for each of the old predicates the number of variables the old and the new predicate have in common
    for (AbstractionPredicate predInPartition : predicates) {
      Set<String> varsInPrevPred = fmgr.extractVariableNames(predInPartition.getSymbolicAtom());
      int index = predInPartition.getVariableNumber();
      // the similarity is equal to the number of variables the predicates have in common
      varsInPrevPred.retainAll(varsInNewPred);
      Integer similarity = varsInPrevPred.size();
      HashMap<Integer, Integer> similaritiesPrevPred = similarityRelation.get(index);
      similaritiesPrevPred.put(varIDNewPredicate, similarity);
      similarities.put(index, similarity);
    }

    similarityRelation.put(varIDNewPredicate, similarities);
  }

  public PredicatePartition merge(PredicatePartition newPreds) {
    if (this.partitionID != newPreds.getPartitionID()) {
      // merge the mappings varIDToPredicate of the two partitions.
      // this has to be done no matter which insertion strategy is used.
      this.varIDToPredicate.putAll(newPreds.getVarIDToPredicate());

//      // 1. implication insert: insert every predicate on its own, insertion takes care of the sorting
//      for (AbstractionPredicate newPred : newPreds.getPredicates()) {
//       this.insertPredicate(newPred);
//      }

//      // 2. similarity insert: place the partition with more predicates first and merge similarity relations.
//      if (newPreds.predicates.size() > this.predicates.size()) {
//        this.predicates.addAll(0, newPreds.predicates);
//      } else {
//        this.predicates.addAll(newPreds.predicates);
//      }
//      this.similarityRelation.putAll(newPreds.getSimilarityRelation());

      // 3. number of variables insert: insert all predicates of the other partition
      //    at the right place in the priority queue of this partition
      this.predicatesFreq.addAll(newPreds.predicatesFreq);
    }

    return this;
  }

  public int getPartitionID() {
    return partitionID;
  }

  public List<AbstractionPredicate> getPredicates() {
    if (predicatesFreq.size() != 0) {
      while (!predicatesFreq.isEmpty()) {
        predicates.add(predicatesFreq.poll());
      }
    }
    return predicates;
  }

  public HashMap<Integer, HashMap<Integer, Integer>> getSimilarityRelation() {
    return similarityRelation;
  }

  public HashMap<Integer, AbstractionPredicate> getVarIDToPredicate() {
    return varIDToPredicate;
  }
}