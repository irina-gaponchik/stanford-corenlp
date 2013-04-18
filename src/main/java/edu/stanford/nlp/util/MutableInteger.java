package edu.stanford.nlp.util;

/**
 * A class for Integer objects that you can change.
 *
 * @author Dan Klein
 */
public final class MutableInteger extends Number implements Comparable<MutableInteger> {

  private int i;

  // Mutable
  public void set(int i) {
    this.i = i;
  }

  @Override
  public int hashCode() {
    return i;
  }

  /**
   * Compares this object to the specified object.  The result is
   * {@code true} if and only if the argument is not
   * {@code null} and is an {@code MutableInteger} object that
   * contains the same {@code int} value as this object.
   * Note that a MutableInteger isn't and can't be equal to an Integer.
   *
   * @param obj the object to compare with.
   * @return {@code true} if the objects are the same;
   *         {@code false} otherwise.
   */
  @Override
  public boolean equals(Object obj) {
      return obj instanceof MutableInteger && i == ((MutableInteger) obj).i;
  }

  @Override
  public String toString() {
    return Integer.toString(i);
  }

  // Comparable interface

  /**
   * Compares two {@code MutableInteger} objects numerically.
   *
   * @param anotherMutableInteger the {@code MutableInteger} to be
   *                              compared.
   * @return The value {@code 0} if this {@code MutableInteger} is
   *         equal to the argument {@code MutableInteger}; a value less than
   *         {@code 0} if this {@code MutableInteger} is numerically less
   *         than the argument {@code MutableInteger}; and a value greater
   *         than {@code 0} if this {@code MutableInteger} is numerically
   *         greater than the argument {@code MutableInteger} (signed
   *         comparison).
   */
  public int compareTo(MutableInteger anotherMutableInteger) {
    int thisVal = this.i;
    int anotherVal = anotherMutableInteger.i;
    return thisVal < anotherVal ? -1 : thisVal == anotherVal ? 0 : 1;
  }


  // Number interface
  @Override
  public int intValue() {
    return i;
  }

  @Override
  public long longValue() {
    return i;
  }

  @Override
  public short shortValue() {
    return (short) i;
  }

  @Override
  public byte byteValue() {
    return (byte) i;
  }

  @Override
  public float floatValue() {
    return i;
  }

  @Override
  public double doubleValue() {
    return i;
  }

  /** Add the argument to the value of this integer.  A convenience method.
   *
   * @param val Value to be added to this integer
   */
  public void incValue(int val) {
    i += val;
  }

  public MutableInteger() {
    this(0);
  }

  public MutableInteger(int i) {
    this.i = i;
  }

  private static final long serialVersionUID = 624465615824626762L;
}
