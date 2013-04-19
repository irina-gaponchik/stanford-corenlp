/**
 * Title:        StanfordMaxEnt<p>
 * Description:  A Maximum Entropy Toolkit<p>
 * Copyright:    Copyright (c) Kristina Toutanova<p>
 * Company:      Stanford University<p>
 */
package edu.stanford.nlp.tagger.maxent;

import ca.gedge.radixtree.RadixTree;
import edu.stanford.nlp.io.InDataStreamFile;
import edu.stanford.nlp.stats.IntCounter;
import javolution.util.FastMap;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;


/** Maintains a map from words to tags and their counts.
 *
 *  @author Kristina Toutanova
 *  @version 1.0
 */
public class Dictionary {

  private final RadixTree<TagCount> dict = new RadixTree<>();
    private final Map<Integer,CountWrapper> partTakingVerbs = new FastMap<>();
    private static final String naWord = "NA";
  private static final boolean VERBOSE = false;

    void fillWordTagCounts(RadixTree< IntCounter<String>> wordTagCounts) {
    for (Map.Entry<String, IntCounter<String>> stringIntCounterEntry : wordTagCounts.entrySet()) {
      TagCount count = new TagCount(stringIntCounterEntry.getValue());
      dict.put(stringIntCounterEntry.getKey(), count);
    }
  }


    protected int getCount(CharSequence word, String tag) {
    TagCount count = dict.get(word);
      return count == null ? 0 : count.get(tag);
  }


  protected String[] getTags(CharSequence word) {
    TagCount count = get(word);
    if (count == null) {
      return null;
    }
    return count.getTags();
  }


  protected TagCount get(CharSequence word) {
    return dict.get(word);
  }


  String getFirstTag(String word) {
    TagCount count = dict.get(word);
    if (count != null) {
      return count.getFirstTag();
    }
    return null;
  }


  protected int sum(CharSequence word) {
    TagCount count = dict.get(word);
    if (count != null) {
      return count.sum();
    }
    return 0;
  }

  boolean isUnknown(String word) {
    return ! dict.containsKey(word);
  }



  void save(DataOutputStream file) {
    String[] arr = dict.keySet().toArray(new String[dict.keySet().size()]);
    try {
      file.writeInt(arr.length);
      System.err.println("Saving dictionary of " + arr.length + " words ...");
      for (String word : arr) {
        TagCount count = get(word);
        file.writeUTF(word);
        count.save(file);
      }
      Integer[] arrverbs = this.partTakingVerbs.keySet().toArray(new Integer[partTakingVerbs.keySet().size()]);
      file.writeInt(arrverbs.length);
      for (Integer iO : arrverbs) {
        CountWrapper tC = this.partTakingVerbs.get(iO);
        file.writeInt(iO.intValue());
        tC.save(file);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void read(DataInputStream rf, String filename) throws IOException {
    // Object[] arr=dict.keySet().toArray();

    int maxNumTags = 0;
    int len = rf.readInt();
    if (VERBOSE) {
      System.err.println("Reading Dictionary of " + len + " words from " + filename + '.');
    }

    for (int i = 0; i < len; i++) {
      String word = rf.readUTF();
      TagCount count = new TagCount();
      count.read(rf);
      int numTags = count.numTags();
      if (numTags > maxNumTags) {
        maxNumTags = numTags;
      }
      this.dict.put(word, count);
      if (VERBOSE) {
        System.err.println("  " + word + " [idx=" + i + "]: " + count);
      }
    }
    if (VERBOSE) {
      System.err.println("Read dictionary of " + len + " words; max tags for word was " + maxNumTags + '.');
    }
  }

  private void readTags(DataInputStream rf) throws IOException {
    // Object[] arr=dict.keySet().toArray();

    int maxNumTags = 0;
    int len = rf.readInt();
    if (VERBOSE) {
      System.err.println("Reading Dictionary of " + len + " words.");
    }

    for (int i = 0; i < len; i++) {
      String word = rf.readUTF();
      TagCount count = new TagCount();
      count.read(rf);
      int numTags = count.numTags();
      if (numTags > maxNumTags) {
        maxNumTags = numTags;
      }
      this.dict.put(word, count);
      if (VERBOSE) {
        System.err.println("  " + word + " [idx=" + i + "]: " + count);
      }
    }
    if (VERBOSE) {
      System.err.println("Read dictionary of " + len + " words; max tags for word was " + maxNumTags + '.');
    }
  }

  protected void read(String filename) {
    try {
      InDataStreamFile rf = new InDataStreamFile(filename);
      read(rf, filename);

      int len1 = rf.readInt();
      for (int i = 0; i < len1; i++) {
        int iO = rf.readInt();
        CountWrapper tC = new CountWrapper();
        tC.read(rf);

        this.partTakingVerbs.put(iO, tC);
      }
      rf.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void read(DataInputStream file) {
    try {
      readTags(file);

      int len1 = file.readInt();
      for (int i = 0; i < len1; i++) {
        int iO = file.readInt();
        CountWrapper tC = new CountWrapper();
        tC.read(file);

        this.partTakingVerbs.put(iO, tC);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This makes ambiguity classes from all words in the dictionary and remembers
   * their classes in the TagCounts
   */
  protected void setAmbClasses(AmbiguityClasses ambClasses, int veryCommonWordThresh, TTags ttags) {
    for (Map.Entry<String,TagCount> entry : dict.entrySet()) {
      String w = entry.getKey();
      TagCount count = entry.getValue();
      int ambClassId = ambClasses.getClass(w, this, veryCommonWordThresh, ttags);
      count.setAmbClassId(ambClassId);
    }
  }

  protected int getAmbClass(String word) {
    if (word.equals(naWord)) {
      return -2;
    }
    if (get(word) == null) {
      return -1;
    }
    return get(word).getAmbClassId();
  }

  public static void main(String... args) {
    String s = "word";
    String tag = "tag";
    Dictionary d = new Dictionary();

    System.out.println(d.getCount(s, tag));
    System.out.println(d.getFirstTag(s));
  }

}
