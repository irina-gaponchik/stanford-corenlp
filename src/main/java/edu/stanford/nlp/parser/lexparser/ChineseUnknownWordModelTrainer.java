package edu.stanford.nlp.parser.lexparser;

import java.util.Map;
import java.util.Set;

import ca.gedge.radixtree.RadixTree;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Tag;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.util.Index;
import javolution.util.FastMap;
import javolution.util.FastSet;

public class ChineseUnknownWordModelTrainer 
  extends AbstractUnknownWordModelTrainer 
{
  // Records the number of times word/tag pair was seen in training data.
  ClassicCounter<IntTaggedWord> seenCounter;
  ClassicCounter<IntTaggedWord> unSeenCounter;

  // c has a map from tags as Label to a Counter from word
  // signatures to Strings; it is used to collect counts that will
  // initialize the probabilities in tagHash
  Map<Label,ClassicCounter<String>> c;
  // tc record the marginal counts for each tag as an unknown.  It
  // should be the same as c's totalCount ??
  ClassicCounter<Label> tc;

  boolean useFirst, useGT, useUnicodeType;

  Map<Label, ClassicCounter<String>> tagHash;

  Set<String> seenFirst;

  double indexToStartUnkCounting;

  UnknownGTTrainer unknownGTTrainer;
  
  IntTaggedWord iTotal = new IntTaggedWord(nullWord, nullTag);

  UnknownWordModel model;
  
  @Override
  public void initializeTraining(Options op, Lexicon lex, 
                                 Index<String> wordIndex, 
                                 Index<String> tagIndex, double totalTrees) {
    super.initializeTraining(op, lex, wordIndex, tagIndex, totalTrees);

    boolean useGoodTuringUnknownWordModel = ChineseTreebankParserParams.DEFAULT_USE_GOOD_TURNING_UNKNOWN_WORD_MODEL;
    useFirst = true;
    useGT = op.lexOptions.useUnknownWordSignatures == 0;
    if (lex instanceof ChineseLexicon) {
      useGoodTuringUnknownWordModel = ((ChineseLexicon) lex).useGoodTuringUnknownWordModel;
    } else if (op.tlpParams instanceof ChineseTreebankParserParams) {
      useGoodTuringUnknownWordModel = ((ChineseTreebankParserParams) op.tlpParams).useGoodTuringUnknownWordModel;
    }
    if (useGoodTuringUnknownWordModel) {
      this.useGT = true;
      this.useFirst = false;
    }

    this.useUnicodeType = op.lexOptions.useUnicodeType;

    if (useFirst) {
      System.err.println("ChineseUWM: treating unknown word as the average of their equivalents by first-character identity. useUnicodeType: " + useUnicodeType);
    }
    if (useGT) {
      System.err.println("ChineseUWM: using Good-Turing smoothing for unknown words.");
    }

      this.c = new FastMap<>();
    this.tc = new ClassicCounter<>();
    this.unSeenCounter = new ClassicCounter<>();
    this.seenCounter = new ClassicCounter<>();
      this.seenFirst = new FastSet<>();
      this.tagHash = new FastMap<>();
    
    this.indexToStartUnkCounting = totalTrees * op.trainOptions.fractionBeforeUnseenCounting;
    
    this.unknownGTTrainer = useGT ? new UnknownGTTrainer() : null;

    RadixTree<Float> unknownGT = null;
    if (useGT) {
      unknownGT = unknownGTTrainer.unknownGT;
    }
    this.model = new ChineseUnknownWordModel(op, lex, wordIndex, tagIndex, 
                                             unSeenCounter, tagHash, 
                                             unknownGT, useGT, seenFirst);
  }
  
  /**
   * trains the first-character based unknown word model.
   *
   * @param tw The word we are currently training on
   * @param loc The position of that word
   * @param weight The weight to give this word in terms of training
   */
  public void train(TaggedWord tw, int loc, double weight) {
    if (useGT) {
      unknownGTTrainer.train(tw, weight);
    }
    
    String word = tw.word();
    Label tagL = new Tag(tw.tag());
    String first = word.substring(0, 1);
    if (useUnicodeType) {
      char ch = word.charAt(0);
      int type = Character.getType(ch);
      if (type != Character.OTHER_LETTER) {
        // standard Chinese characters are of type "OTHER_LETTER"!!
        first = Integer.toString(type);
      }
    }
    String tag = tw.tag();
    
    if ( ! c.containsKey(tagL)) {
      c.put(tagL, new ClassicCounter<String>());
    }
    c.get(tagL).incrementCount(first, weight);
    
    tc.incrementCount(tagL, weight);
          
    seenFirst.add(first);
    
    IntTaggedWord iW = new IntTaggedWord(word, IntTaggedWord.ANY, wordIndex, tagIndex);
    seenCounter.incrementCount(iW, weight);
    if (treesRead > indexToStartUnkCounting) {
      // start doing this once some way through trees; 
      // treesRead is 1 based counting
      if (seenCounter.get(iW) < 2) {
        IntTaggedWord iT = new IntTaggedWord(IntTaggedWord.ANY, tag, wordIndex, tagIndex);
        unSeenCounter.incrementCount(iT, weight);
        unSeenCounter.incrementCount(iTotal, weight);
      }
    }
  }
  
  public UnknownWordModel finishTraining() {
    RadixTree<Float> unknownGT = null;
    if (useGT) {
      unknownGTTrainer.finishTraining();
      unknownGT = unknownGTTrainer.unknownGT;
    }
    
    for (Map.Entry<Label, ClassicCounter<String>> labelClassicCounterEntry : c.entrySet()) {
      // outer iteration is over tags as Labels
      ClassicCounter<String> wc = labelClassicCounterEntry.getValue(); // counts for words given a tag
      
      if ( ! tagHash.containsKey(labelClassicCounterEntry.getKey())) {
        tagHash.put(labelClassicCounterEntry.getKey(), new ClassicCounter<String>());
      }
      
      // the UNKNOWN first character is assumed to be seen once in
      // each tag
      // this is really sort of broken!  (why??)
      tc.incrementCount(labelClassicCounterEntry.getKey());
      wc.setCount(unknown, 1.0);
      
      // inner iteration is over words  as strings
      for (String first : wc.keySet()) {
        double prob = Math.log(wc.get(first) / tc.get(labelClassicCounterEntry.getKey()));
        tagHash.get(labelClassicCounterEntry.getKey()).setCount(first, prob);
        //if (Test.verbose)
        //EncodingPrintWriter.out.println(tag + " rewrites as " + first + " firstchar with probability " + prob,encoding);
      }
    }

    return model;
  }
}

