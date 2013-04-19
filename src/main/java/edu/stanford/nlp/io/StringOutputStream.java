package edu.stanford.nlp.io;

import javolution.text.TxtBuilder;

import java.io.*;

/**
 * An {@code OutputStream} that can be turned into a {@code String}.
 *
 * @author Bill MacCartney
 */
public class StringOutputStream extends OutputStream {

  TxtBuilder sb = new TxtBuilder();

    synchronized public void clear() {
    sb.setLength(0);
  }

  @Override
  synchronized public void write(int i) {
    sb.append((char) i);
  }

  @Override
  synchronized public String toString()  {
    return sb.toString();
  }
  
}
