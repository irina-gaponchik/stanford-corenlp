package edu.stanford.nlp.dcoref.sievepasses;

public class ExactStringMatch extends DeterministicCorefSieve {
  public ExactStringMatch() {
      flags.USE_EXACTSTRINGMATCH = true;
  }
}
