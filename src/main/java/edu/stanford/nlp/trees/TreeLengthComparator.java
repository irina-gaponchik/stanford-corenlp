package edu.stanford.nlp.trees;

import java.util.Comparator;

/**
 * A {@code TreeLengthComparator} orders trees by their yield sentence
 * lengths.
 *
 * @author Christopher Manning
 * @version 2003/03/24
 */
public class TreeLengthComparator implements Comparator<Tree> {


    /**
   * Compare the two objects.
   */
  public int compare(Tree t1, Tree t2) {
    if (t1.equals(t2)) {
      return 0;
    }
    int len1 = t1.yield().size();
    int len2 = t2.yield().size();
    if (len1 > len2) {
      return 1;
    } else return len1 < len2 ? -1 : 0;
  }

}
