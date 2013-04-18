package edu.stanford.nlp.optimization;

/**
 * The interface for one variable function minimizers.
 *
 * @author Jenny Finkel
 */
public interface LineSearcher {

  /**
   * Attempts to find an unconstrained minimum of the objective
   * {@code function} starting at {@code initial}, within
   * {@code functionTolerance}.
   *
   * @param function          the objective function
   */
  double minimize(edu.stanford.nlp.util.Function<Double, Double> function) ;
}
