package edu.stanford.nlp.util;


import javolution.text.TxtBuilder;

/**
 * An instantiation of this abstract class parses a {@code String} and
 * returns an object of type {@code E}.  It's called a
 * {@code StringParsingTask} (rather than {@code StringParser})
 * because a new instance is constructed for each {@code String} to be
 * parsed.  We do this to be thread-safe: methods in
 * {@code StringParsingTask} share state information (e.g. current
 * string index) via instance variables.
 *
 * @author Bill MacCartney
 */
public abstract class StringParsingTask<E> {
  
  // This class represents a parser working on a specific string.  We
  // construct from a specific string in order 
  protected String s;
  protected int index;
  protected boolean isEOF;     // true if we tried to read past end
    
  /**
   * Constructs a new {@code StringParsingTask} from the specified
   * {@code String}.  Derived class constructors should be sure to
   * call {@code super(s)}!
   */
  protected StringParsingTask(String s) {
    this.s = s;
    index = 0;
  }
    
  /**
   * Parses the {@code String} associated with this
   * {@code StringParsingTask} and returns a object of type
   * {@code E}.
   */
  public abstract E parse();

    
  // ---------------------------------------------------------------------

  /**
   * Reads characters until {@link #isWhiteSpace(char) isWhiteSpace(ch)}or
   * {@link #isPunct(char) isPunct(ch)} or {@link #isEOF()}.  You may need
   * to override the definition of {@link #isPunct(char) isPunct(ch)} to
   * get this to work right.
   */
  protected String readName() {
    readWhiteSpace();
    TxtBuilder sb = new TxtBuilder();
    char ch = read();
    while (!isWhiteSpace(ch) && !isPunct(ch) && !isEOF) {
      sb.append(ch);
      ch = read();
    }
    unread();
    // System.err.println("Read text: ["+sb+"]");
    return sb.toString().intern();
  }

  protected String readJavaIdentifier() {
    readWhiteSpace();
    TxtBuilder sb = new TxtBuilder();
    char ch = read();
    if (Character.isJavaIdentifierStart(ch) && !isEOF) {
      sb.append(ch);
      ch = read();
      while (Character.isJavaIdentifierPart(ch) && !isEOF) {
        sb.append(ch);
        ch = read();
      }
    }
    unread();
    // System.err.println("Read text: ["+sb+"]");
    return sb.toString().intern();
  }

  // .....................................................................

  protected void readLeftParen() {
    // System.out.println("Read left.");
    readWhiteSpace();
    char ch = read();
    if (!isLeftParen(ch))
      throw new ParserException("Expected left paren!");
  }

  protected void readRightParen() {
    // System.out.println("Read right.");
    readWhiteSpace();
    char ch = read();
    if (!isRightParen(ch)) 
      throw new ParserException("Expected right paren!");
  }

  protected void readDot() {
    readWhiteSpace();
    if (isDot(peek())) read();
  }

  protected void readWhiteSpace() {
    char ch = read();
    while (isWhiteSpace(ch) && !isEOF) {
      ch = read();
    }
    unread();
  }

  // .....................................................................

  protected char read() {
    if (index >= s.length() || index < 0) {
      isEOF = true;
      return ' ';                     // arbitrary
    }
    return s.charAt(index++);
  }
  
  protected void unread() {
    index--;
  }
  
  protected char peek() {
    char ch = read();
    unread();
    return ch;
  }


  // -----------------------------------------------------------------------

  protected boolean isEOF() {
    return isEOF;
  }

  protected static boolean isWhiteSpace(char ch) {
    return ch == ' ' || ch == '\t' || ch == '\f' || ch == '\r' || ch == '\n';
  }

  protected boolean isPunct(char ch) {
    return 
      isLeftParen(ch) ||
      isRightParen(ch);
  }

  protected static boolean isLeftParen(char ch) {
    return ch == '(';
  }

  protected static boolean isRightParen(char ch) {
    return ch == ')';
  }

  protected static boolean isDot(char ch) {
    return ch == '.';
  }


  // exception class -------------------------------------------------------

  public static class ParserException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ParserException(Exception e)    { super(e); }
    public ParserException(String message) { super(message); }
  }

}  
