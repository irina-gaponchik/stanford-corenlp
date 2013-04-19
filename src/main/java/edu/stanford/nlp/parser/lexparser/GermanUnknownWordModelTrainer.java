package edu.stanford.nlp.parser.lexparser;

import java.util.Map;

import ca.gedge.radixtree.RadixTree;
import edu.stanford.nlp.ling.Label;

public class GermanUnknownWordModelTrainer
  extends BaseUnknownWordModelTrainer 
{
  protected UnknownWordModel buildUWM() {
    RadixTree<Float> unknownGT = null;
    if (useGT) {
      unknownGT = unknownGTTrainer.unknownGT;
    }
    return new GermanUnknownWordModel(op, lex, wordIndex, tagIndex, 
                                      unSeenCounter, tagHash, 
                                      unknownGT, seenEnd);
  }
}