package edu.stanford.nlp.util;

import java.util.Iterator;

/**
 * Iterator with {@code remove()} defined to throw an
 * {@code UnsupportedOperationException}.
 */
abstract public class AbstractIterator<E> implements Iterator<E> {

  abstract public boolean hasNext();

  abstract public E next();

  /**
   * Throws an {@code UnupportedOperationException}.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
