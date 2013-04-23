/**
 * Title:       StanfordMaxEnt<p>
 * Description: A Maximum Entropy Toolkit<p>
 * Copyright:   The Board of Trustees of The Leland Stanford Junior University
 * Company:     Stanford University<p>
 */

package edu.stanford.nlp.tagger.maxent;

import edu.stanford.nlp.ling.WordTag;
import javolution.util.FastTable;

import java.util.*;

/** A simple class that maintains a list of WordTag pairs which are interned
 *  as they are added.  This stores a tagged corpus.
 *
 *  @author Kristina Toutanova
 *  @version 1.0
 */
public class PairsHolder {

  private final  List<WordTag> arr = FastTable.newInstance();

    public void setSize(int s) {
    while (arr.size() < s) arr.add(new WordTag(null, "NN"));  // todo: remove NN.  NA okay?
  }

  public int getSize() {
    return arr.size();
  }

  void clear() {
    arr.clear();
  }

  void add(WordTag wordtag) {
    arr.add(wordtag);
  }

  void setWord(int pos, String word) {
    arr.get(pos).setWord(word);
  }

  void setTag(int pos, String tag) {
    arr.get(pos).setTag(tag);
  }


  String getTag(int position) {
    return arr.get(position).tag();
  }
  String getWord(int position) {
    return arr.get(position).word();
  }

  String getWord(History h, int position) {
      return h.current + position >= h.start && h.current + position <= h.end ? arr.get(h.current + position).word() : "NA";
  }

  String getTag(History h, int position) {
      return h.current + position >= h.start && h.current + position <= h.end ? arr.get(h.current + position).tag() : "NA";
  }
}
