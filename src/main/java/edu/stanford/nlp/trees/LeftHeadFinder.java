package edu.stanford.nlp.trees;


/**
 * HeadFinder that always returns the leftmost daughter as head.  For
 * testing purposes.
 *
 * @author Roger Levy
 */
public class LeftHeadFinder implements HeadFinder {

  /**
   * 
   */
  private static final long serialVersionUID = 8453889846239508208L;

  public Tree determineHead(Tree t) {
      return t.isLeaf() ? null : t.children()[0];
  }

  public Tree determineHead(Tree t, Tree parent) {
    return determineHead(t);
  }

}
