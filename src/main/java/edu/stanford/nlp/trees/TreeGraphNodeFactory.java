package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;


/**
 * A {@code TreeGraphNodeFactory} acts as a factory for creating
 * nodes in a {@link TreeGraph {@code TreeGraph}}.  Unless
 * another {@link LabelFactory {@code LabelFactory}} is
 * supplied, it will use a CoreLabelFactory
 * by default.
 *
 * @author Bill MacCartney
 */
public class TreeGraphNodeFactory implements TreeFactory {

  private LabelFactory mlf;

  /**
   * Make a {@code TreeFactory} that produces
   * {@code TreeGraphNode}s.  The labels are of class
   * {@code CoreLabel}.
   */
  public TreeGraphNodeFactory() {
    this(CoreLabel.factory());
  }

  public TreeGraphNodeFactory(LabelFactory mlf) {
    this.mlf = mlf;
  }

  // docs inherited
  public Tree newLeaf(String word) {
    return newLeaf(mlf.newLabel(word));
  }

  // docs inherited
  public Tree newLeaf(Label label) {
    return new TreeGraphNode(label);
  }

  // docs inherited
  public Tree newTreeNode(String parent, List<Tree> children) {
    return newTreeNode(mlf.newLabel(parent), children);
  }

  // docs inherited
  public Tree newTreeNode(Label parentLabel, List<Tree> children) {
    return new TreeGraphNode(parentLabel, children);
  }

}

