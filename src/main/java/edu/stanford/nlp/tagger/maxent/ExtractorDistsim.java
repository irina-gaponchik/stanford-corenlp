package edu.stanford.nlp.tagger.maxent;

import ca.gedge.radixtree.RadixTree;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.Timing;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

import java.io.File;
import java.util.Map;

/**
 * Extractor for adding distsim information.
 *
 * @author rafferty
 */
public class ExtractorDistsim extends Extractor {

  private static final long serialVersionUID = 1L;

  // avoid loading the same lexicon twice but allow different lexicons
  private static final RadixTree<RadixTree<String>> lexiconMap = new RadixTree<>();

    private final RadixTree<String> lexicon;

  private static RadixTree<String> initLexicon(String path) {
    synchronized (lexiconMap) {
        RadixTree<String> lex = lexiconMap.get(path);
      if (lex != null) {
        return lex;
      } else {
        Timing.startDoing("Loading distsim lexicon from " + path);
          RadixTree<String> lexic = new RadixTree<>();
        for (String word : ObjectBank.getLineIterator(new File(path))) {
          String[] bits = word.split("\\s+");
          lexic.put(bits[0].toLowerCase(), bits[1]);
        }
        lexiconMap.put(path, (RadixTree<String>) lexic);
        Timing.endDoing();
        return (RadixTree<String>) lexic;
      }
    }
  }

  @Override
  String extract(History h, PairsHolder pH) {
    CharSequence word = super.extract(h, pH);
    String distSim = lexicon.get(String.valueOf(word).toLowerCase());
    if (distSim == null) distSim = "null";
    return distSim;
  }

  ExtractorDistsim(String distSimPath, int position) {
    super(position, false);
    lexicon = initLexicon(distSimPath);
  }

  @Override public boolean isLocal() { return position == 0; }
  @Override public boolean isDynamic() { return false; }


  public static class ExtractorDistsimConjunction extends Extractor {

    private static final long serialVersionUID = 1L;

    private final RadixTree<String> lexicon;
    private final int left;
    private final int right;
    private String name;

    @Override
    String extract(History h, PairsHolder pH) {
      TextBuilder sb = new TextBuilder();
      for (int j = left; j <= right; j++) {
        String word = pH.getWord(h, j);
        String distSim = lexicon.get(word.toLowerCase());
        if (distSim == null) distSim = "null";
        sb.append(distSim);
        if (j < right) {
          sb.append('|');
        }
      }
      return sb.toString();
    }

    ExtractorDistsimConjunction(String distSimPath, int left, int right) {
        lexicon = (RadixTree<String>) initLexicon(distSimPath);
      this.left = left;
      this.right = right;
      name = "ExtractorDistsimConjunction(" + left + ',' + right + ')';
    }

    @Override
    public String toString() {
      return name;
    }

    @Override public boolean isLocal() { return false; }
    @Override public boolean isDynamic() { return false; }

  } // end static class ExtractorDistsimConjunction

}
