package edu.stanford.nlp.trees.international.pennchinese;

import java.io.Reader;

import edu.stanford.nlp.trees.*;


/**
 * The {@code CTBTreeReaderFactory} is a factory for creating a
 * TreeReader suitable for the Penn CTB.
 *
 * @author Christopher Manning
 */
public class CTBTreeReaderFactory implements TreeReaderFactory {

  private final TreeNormalizer tn;
  private final boolean discardFrags;

  public CTBTreeReaderFactory() {
    this(new TreeNormalizer());
  }

  public CTBTreeReaderFactory(TreeNormalizer tn) {
    this(tn, false);
  }

  public CTBTreeReaderFactory(TreeNormalizer tn, boolean discardFrags) {
    this.tn = tn;
    this.discardFrags = discardFrags;
  }

  /**
   * Create a new {@code TreeReader} using the provided
   * {@code Reader}.
   *
   * @param in The {@code Reader} to build on
   * @return The new TreeReader
   */
  public TreeReader newTreeReader(Reader in) {
      return discardFrags ? new FragDiscardingPennTreeReader(in, new LabeledScoredTreeFactory(), tn, new CHTBTokenizer(in)) : new PennTreeReader(in, new LabeledScoredTreeFactory(), tn, new CHTBTokenizer(in));
  }
}
