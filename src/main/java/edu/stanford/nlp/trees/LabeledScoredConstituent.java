package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.ling.StringLabel;

/**
 * A {@code LabeledScoredConstituent} object defines an edge in a graph
 * with a label and a score.
 *
 * @author Christopher Manning
 */
public class LabeledScoredConstituent extends LabeledConstituent {

  private double score;

  /**
   * Create an empty {@code LabeledScoredConstituent} object.
   */
  public LabeledScoredConstituent() {
    // implicitly super();
  }


  /**
   * Create a {@code LabeledScoredConstituent} object with given
   * values.
   *
   * @param start start node of edge
   * @param end   end node of edge
   */
  public LabeledScoredConstituent(int start, int end) {
    super(start, end);
  }


  /**
   * Create a {@code LabeledScoredConstituent} object with given
   * values.
   *
   * @param start start node of edge
   * @param end   end node of edge
   */
  public LabeledScoredConstituent(int start, int end, Label label, double score) {
    super(start, end, label);
    this.score = score;
  }


  /**
   * Returns the score associated with the current node, or Nan
   * if there is no score
   *
   * @return the score
   */
  @Override
  public double score() {
    return score;
  }


  /**
   * Sets the score associated with the current node, if there is one
   */
  @Override
  public void setScore(double score) {
    this.score = score;
  }


  /**
   * A {@code LabeledScoredConstituentLabelFactory} object makes a
   * {@code LabeledScoredConstituent} with a {@code StringLabel}
   * label (or of the type of label passed in for the final constructor).
   */
  private static class LabeledScoredConstituentLabelFactory implements LabelFactory {

    /**
     * Make a new {@code LabeledScoredConstituent}.
     *
     * @param labelStr A string
     * @return The created label
     */
    public Label newLabel(String labelStr) {
      return new LabeledScoredConstituent(0, 0, new StringLabel(labelStr), 0.0);
    }


    /**
     * Make a new {@code LabeledScoredConstituent}.
     *
     * @param labelStr A string.
     * @param options  The options are ignored.
     * @return The created label
     */
    public Label newLabel(String labelStr, int options) {
      return newLabel(labelStr);
    }


    /**
     * Make a new {@code LabeledScoredConstituent}.
     *
     * @param labelStr A string that
     * @return The created label
     */
    public Label newLabelFromString(String labelStr) {
      return newLabel(labelStr);
    }


    /**
     * Create a new {@code LabeledScoredConstituent}.
     *
     * @param oldLabel A {@code Label}.
     * @return A new {@code LabeledScoredConstituent}
     */
    public Label newLabel(Label oldLabel) {
      return new LabeledScoredConstituent(0, 0, oldLabel, 0.0);
    }

  }


  // extra class guarantees correct lazy loading (Bloch p.194)
  private static class LabelFactoryHolder {
    static final LabelFactory lf = new LabeledScoredConstituentLabelFactory();
  }

  /**
   * Return a factory for this kind of label.
   * The factory returned is always the same one (a singleton)
   *
   * @return the label factory
   */
  @Override
  public LabelFactory labelFactory() {
    return LabelFactoryHolder.lf;
  }


  // extra class guarantees correct lazy loading (Bloch p.194)
  private static class ConstituentFactoryHolder {

    private static final ConstituentFactory cf = new LabeledScoredConstituentFactory();

  }


  /**
   * Return a factory for this kind of constituent.
   * The factory returned is always the same one (a singleton).
   *
   * @return The constituent factory
   */
  @Override
  public ConstituentFactory constituentFactory() {
    return ConstituentFactoryHolder.cf;
  }


  /**
   * Return a factory for this kind of constituent.
   * The factory returned is always the same one (a singleton).
   *
   * @return The constituent factory
   */
  public static ConstituentFactory factory() {
    return ConstituentFactoryHolder.cf;
  }

}
