package edu.stanford.nlp.trees;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.util.Scored;
import javolution.text.TextBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@code Constituent} object defines a generic edge in a graph.
 * The {@code Constituent} class is designed to be extended.  It
 * implements the {@code Comparable} interface in order to allow
 * graphs to be topologically sorted by the ordinary {@code Collection}
 * library in {@code java.util}, keying primarily on right-hand
 * node ID number.  The {@code Constituent} class implements most
 * of the functionality of the the {@code Label}
 * interface by passing all requests down to the {@code Label} which
 * might be contained in the {@code Constituent}.  This allows one
 * to put a {@code Constituent} anywhere that a {@code Label} is
 * required.  A {@code Constituent} is always {@code Scored}.
 *
 * @author Christopher Manning
 */
public abstract class Constituent implements Labeled, Scored, Label {

  protected Constituent() {}

  /**
   * access start node.
   */
  public abstract int start();

  /**
   * set start node.
   */
  public abstract void setStart(int start);


  /**
   * access end node.
   */
  public abstract int end();


  /**
   * set end node.
   */
  public abstract void setEnd(int end);


  /**
   * access label
   */
  public Label label() {
    return null;
  }


  /**
   * Sets the label associated with the current Constituent,
   * if there is one.
   */
  public void setLabel(Label label) {
    // a noop
  }


  /**
   * Access labels -- actually always a singleton here.
   */
  public Collection<Label> labels() {
    return Collections.singletonList(label());
  }


  public void setLabels(Collection<Label> labels) {
    throw new UnsupportedOperationException("Constituent can't be multilabeled");
  }


  /**
   * access score
   */
  public double score() {
    return Double.NaN;
  }


  /**
   * Sets the score associated with the current node, if there is one
   */
  public void setScore(double score) {
    // a no-op
  }


  /**
   * Return a string representation of a {@code Constituent}.
   *
   * @return The full string representation.
   */
  @Override
  public String toString() {
    StringBuilder sb;
    Label lab = label();
      sb = lab != null ? new StringBuilder(lab.toString()) : new StringBuilder();
    sb.append('(').append(start()).append(',').append(end()).append(')');
    return sb.toString();
  }


  /**
   * Return the length of a {@code Constituent}
   */
  public int size() {
    return end() - start();
  }


  /**
   * Compare with another Object for equality.
   * Two Constituent objects are equal if they have the same start and end,
   * and, if at least one of them has a non-null label, then their labels are equal.
   * The score of a Constituent is not considered in the equality test.
   * This seems to make sense for most of the applications we have in mind
   * where one wants to assess equality independent of score, and then if
   * necessary to relax a constituent if one with a better score is found.
   * (Note, however, that if you do want to compare Constituent scores for
   * equality, then you have to be careful,
   * because two {@code double} NaN values are considered unequal in
   * Java.)
   * The general contract of equals() implies that one can't have a
   * subclass of a concrete [non-abstract] class redefine equals() to use
   * extra aspects, so subclasses shouldn't override this in ways that
   * make use of extra fields.
   *
   * @param obj The object being compared with
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(Object obj) {
    // unclear if this will be a speedup in general
    // if (this == o)
    //      return true;
    if (obj instanceof Constituent) {
      Constituent c = (Constituent) obj;
      // System.out.println("Comparing " + this + " to " + c + "\n  " +
      //	"start: " + (start() == c.start()) + " end: " +
      //	(end() == c.end()) + " score: " + (score() == c.score()));
      if (start() == c.start() && end() == c.end()) {
        Label lab1 = label();
        Label lab2 = c.label();
        if (lab1 == null) {
          return lab2 == null;
        }

        String lv1 = lab1.value();
        String lv2 = lab2.value();
        if (lv1 == null && lv2 == null) {
          return true;
        }
        if (lv1  != null && lv2 != null) {
          return lab1.value().equals(lab2.value());
        }
      }
    }
    return false;
  }


  /**
   * A hashCode for Constituents done by shifting and or'ing for speed.
   * Now includes the label if the constituent has one (otherwise things
   * would work very badly if you were hashing constituents over the
   * same span....).
   *
   * @return the integer hashCode
   */
  @Override
  public int hashCode() {
    int hash = start() << 16 | end();
    Label lab = label();
    return lab == null || lab.value() == null ? hash : hash ^ lab.value().hashCode();
  }


  /**
   * Detects whether this constituent overlaps a constituent without
   * nesting, that is, whether they "cross".
   *
   * @param c The constituent to check against
   * @return True if the two constituents cross
   */
  public boolean crosses(Constituent c) {
    return start() < c.start() && c.start() < end() && end() < c.end() || c.start() < start() && start() < c.end() && c.end() < end();
  }


  /**
   * Detects whether this constituent overlaps any of a Collection of
   * Constituents without
   * nesting, that is, whether it "crosses" any of them.
   *
   * @param constColl The set of constituent to check against
   * @return True if some constituent in the collection is crossed
   * @throws ClassCastException If some member of the Collection isn't
   *                            a Constituent
   */
  public boolean crosses(Collection<Constituent> constColl) {
    for (Constituent c : constColl) {
      if (crosses(c)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Detects whether this constituent contains a constituent, that is
   * whether they are nested.  That is, the other constituent's yield is
   * a sublist of this constituent's yield.
   *
   * @param c The constituent to check against
   * @return True if the other Constituent is contained in this one
   */
  public boolean contains(Constituent c) {
    return start() <= c.start() && end() >= c.end();
  }



  // -- below here is stuff to implement the Label interface

  /**
   * Return the value of the label (or null if none).
   *
   * @return String the value for the label
   */
  public String value() {
    Label lab = label();
    if (lab == null) {
      return null;
    }
    return lab.value();
  }


  /**
   * Set the value for the label (if one is stored).
   *
   * @param value The value for the label
   */
  public void setValue(String value) {
    Label lab = label();
    if (lab != null) {
      lab.setValue(value);
    }
  }


  /**
   * Make a new label with this {@code String} as the "name", perhaps
   * by doing some appropriate decoding of the string.
   *
   * @param labelStr the String that translates into the content of the
   *                 label
   */
  public void setFromString(String labelStr) {
    Label lab = label();
    if (lab != null) {
      lab.setFromString(labelStr);
    }
  }


  /**
   * Print out as a string the subpart of a sentence covered
   * by this {@code Constituent}.
   *
   * @return The subpart of the sentence
   */
  // TODO: genericize this!
  public String toSentenceString(ArrayList s) {
    TextBuilder sb = new TextBuilder();
    for (int wordNum = start(), end = end(); wordNum <= end; wordNum++) {
      sb.append(s.get(wordNum));
      if (wordNum != end) {
        sb.append(' ');
      }
    }
    return sb.toString();
  }

}
