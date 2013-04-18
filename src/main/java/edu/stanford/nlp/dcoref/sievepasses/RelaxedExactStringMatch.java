package edu.stanford.nlp.dcoref.sievepasses;

public class RelaxedExactStringMatch extends DeterministicCorefSieve {
  public RelaxedExactStringMatch() {
      flags.USE_RELAXED_EXACTSTRINGMATCH = true;
  }
}
