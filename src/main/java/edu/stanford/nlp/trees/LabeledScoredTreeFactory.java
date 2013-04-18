package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;

import java.util.List;

/**
 * A {@code LabeledScoredTreeFactory} acts as a factory for creating
 * trees with labels and scores.  Unless another {@code LabelFactory}
 * is supplied, it will use a {@code CoreLabel} by default.
 *
 * @author Christopher Manning
 */
public class LabeledScoredTreeFactory extends SimpleTreeFactory {

  private LabelFactory lf;

  /**
   * Make a TreeFactory that produces LabeledScoredTree trees.
   * The labels are of class {@code CoreLabel}.
   */
  public LabeledScoredTreeFactory() {
    this(CoreLabel.factory());
  }

  /**
   * Make a TreeFactory that uses LabeledScoredTree trees, where the
   * labels are as specified by the user.
   *
   * @param lf the {@code LabelFactory} to be used to create labels
   */
  public LabeledScoredTreeFactory(LabelFactory lf) {
    this.lf = lf;
  }

  @Override
  public Tree newLeaf(String word) {
    return new LabeledScoredTreeNode(lf.newLabel(word));
  }

  /**
   * Create a new leaf node with the given label
   *
   * @param label the label for the leaf node
   * @return A new tree leaf
   */
  @Override
  public Tree newLeaf(Label label) {
    return new LabeledScoredTreeNode(lf.newLabel(label));
  }

  @Override
  public Tree newTreeNode(String parent, List<Tree> children) {
    return new LabeledScoredTreeNode(lf.newLabel(parent), children);
  }

  /**
   * Create a new non-leaf tree node with the given label
   *
   * @param parentLabel The label for the node
   * @param children    A {@code List} of the children of this node,
   *                    each of which should itself be a {@code LabeledScoredTree}
   * @return A new internal tree node
   */
  @Override
  public Tree newTreeNode(Label parentLabel, List<Tree> children) {
    return new LabeledScoredTreeNode(lf.newLabel(parentLabel), children);
  }
}

