package edu.stanford.nlp.dcoref.sievepasses;

public class MarkRole extends DeterministicCorefSieve {
  public MarkRole() {
      flags.USE_ROLE_SKIP = true;
  }
}
