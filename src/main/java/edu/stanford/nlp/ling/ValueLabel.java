package edu.stanford.nlp.ling;

import java.io.Serializable;

/**
 * A {@code ValueLabel} object acts as a Label with linguistic
 * attributes.  This is an abstract class, which doesn't actually store
 * or return anything.  It returns {@code null} to any requests. However,
 * it does
 * stipulate that equals() and compareTo() are defined solely with respect to
 * value(); this should not be changed by subclasses.
 * Other fields of a ValueLabel subclass should be regarded
 * as secondary facets (it is almost impossible to override equals in
 * a useful way while observing the contract for equality defined for Object,
 * in particular, that equality must by symmetric).
 * This class is designed to be extended.
 *
 * @author Christopher Manning
 */
public abstract class ValueLabel implements Label, Comparable<ValueLabel>, Serializable {

  protected ValueLabel() {
  }


  /**
   * Return the value of the label (or null if none).
   * The default value returned by an {@code ValueLabel} is
   * always {@code null}
   *
   * @return the value for the label
   */
  public String value() {
    return null;
  }


  /**
   * Set the value for the label (if one is stored).
   *
   * @param value - the value for the label
   */
  public void setValue(String value) {
  }


  /**
   * Return a string representation of the label.  This will just
   * be the {@code value()} if it is non-{@code null},
   * and the empty string otherwise.
   *
   * @return The string representation
   */
  @Override
  public String toString() {
    String val = value();
    return val == null ? "" : val;
  }


  public void setFromString(String labelStr) {
    throw new UnsupportedOperationException();
  }


  /**
   * Equality for {@code ValueLabel}s is defined in the first instance
   * as equality of their {@code String} {@code value()}.
   * Now rewritten to correctly enforce the contract of equals in Object.
   * Equality for a {@code ValueLabel} is determined simply by String
   * equality of its {@code value()}.  Subclasses should not redefine
   * this to include other aspects of the {@code ValueLabel}, or the
   * contract for {@code equals()} is broken.
   *
   * @param obj the object against which equality is to be checked
   * @return true if {@code this} and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    String val = value();
    return obj instanceof ValueLabel && (val == null ? ((Label) obj).value() == null : val.equals(((Label) obj).value()));
  }


  /**
   * Return the hashCode of the String value providing there is one.
   * Otherwise, returns an arbitrary constant for the case of
   * {@code null}.
   */
  @Override
  public int hashCode() {
    String val = value();
    return val == null ? 3 : val.hashCode();
  }


  /**
   * Orders by {@code value()}'s lexicographic ordering.
   *
   * @param valueLabel object to compare to
   * @return result (positive if this is greater than obj)
   */
  public int compareTo(ValueLabel valueLabel) {
    return value().compareTo(valueLabel.value());
  }


  /**
   * Returns a factory that makes Labels of the appropriate sort.
   *
   * @return the {@code LabelFactory}
   */
  public abstract LabelFactory labelFactory();


  private static final long serialVersionUID = -1413303679077285530L;


}
