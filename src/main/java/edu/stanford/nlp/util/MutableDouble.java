package edu.stanford.nlp.util;

/**
 * A class for Double objects that you can change.
 *
 * @author Dan Klein
 */
public final class MutableDouble extends Number implements Comparable<MutableDouble> {

  private double d;

  // Mutable
  public void set(double d) {
    this.d = d;
  }

  @Override
  public int hashCode() {
    long bits = Double.doubleToLongBits(d);
    return (int) (bits ^ bits >>> 32);
  }

  /**
   * Compares this object to the specified object.  The result is
   * {@code true} if and only if the argument is not
   * {@code null} and is an {@code MutableDouble} object that
   * contains the same {@code double} value as this object.
   * Note that a MutableDouble isn't and can't be equal to an Double.
   *
   * @param obj the object to compare with.
   * @return {@code true} if the objects are the same;
   *         {@code false} otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof MutableDouble && d == ((MutableDouble) obj).d;
  }

  @Override
  public String toString() {
    return Double.toString(d);
  }

  // Comparable interface

  /**
   * Compares two {@code MutableDouble} objects numerically.
   *
   * @param anotherMutableDouble the {@code MutableDouble} to be
   *                             compared.
   * @return Tthe value {@code 0} if this {@code MutableDouble} is
   *         equal to the argument {@code MutableDouble}; a value less than
   *         {@code 0} if this {@code MutableDouble} is numerically less
   *         than the argument {@code MutableDouble}; and a value greater
   *         than {@code 0} if this {@code MutableDouble} is numerically
   *         greater than the argument {@code MutableDouble} (signed
   *         comparison).
   */
  public int compareTo(MutableDouble anotherMutableDouble) {
    double thisVal = this.d;
    double anotherVal = anotherMutableDouble.d;
    return thisVal < anotherVal ? -1 : thisVal == anotherVal ? 0 : 1;
  }

  // Number interface
  @Override
  public int intValue() {
    return (int) d;
  }

  @Override
  public long longValue() {
    return (long) d;
  }

  @Override
  public short shortValue() {
    return (short) d;
  }

  @Override
  public byte byteValue() {
    return (byte) d;
  }

  @Override
  public float floatValue() {
    return (float) d;
  }

  @Override
  public double doubleValue() {
    return d;
  }

  public MutableDouble() {
    this(0.0);
  }

  public MutableDouble(double d) {
    this.d = d;
  }

  public MutableDouble(Number num) {
    this.d = num.doubleValue();
  }

  private static final long serialVersionUID = 624465615824626762L;

}
