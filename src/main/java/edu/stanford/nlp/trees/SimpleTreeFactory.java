package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.Label;

import java.util.List;


/**
 * A {@code SimpleTreeFactory} acts as a factory for creating objects
 * of class {@code SimpleTree}.
 * <p/>
 * <i>NB: A SimpleTree stores tree geometries but no node labels.  Make sure
 * this is what you really want.</i>
 *
 * @author Christopher Manning
 */
public class SimpleTreeFactory implements TreeFactory {

    public Tree newLeaf(String word) {
    return new SimpleTree();
  }

  public Tree newLeaf(Label word) {
    return new SimpleTree();
  }

  public Tree newTreeNode(String parent, List<Tree> children) {
    return new SimpleTree(null, children);
  }

  public Tree newTreeNode(Label parentLabel, List<Tree> children) {
    return new SimpleTree(parentLabel, children);
  }

}
