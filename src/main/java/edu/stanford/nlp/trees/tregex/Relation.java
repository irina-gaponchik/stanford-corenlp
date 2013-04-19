package edu.stanford.nlp.trees.tregex;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Trees;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.IdentityHashSet;
import edu.stanford.nlp.util.Interner;
import javolution.util.FastMap;


/**
 * An abstract base class for relations between tree nodes in tregex. There are
 * two types of subclasses: static anonymous singleton instantiations for
 * relations that do not require arguments, and private subclasses for those
 * with arguments. All invocations should be made through the static factory
 * methods, which insure that there is only a single instance of each relation.
 * Thus == can be used instead of .equals.
 * <p/>
 * If you want to add a new
 * relation, you just have to fill in the definition of satisfies and
 * searchNodeIterator. Also be careful to make the appropriate adjustments to
 * getRelation and SIMPLE_RELATIONS. Finally, if you are using the TregexParser,
 * you need to add the new relation symbol to the list of tokens.
 *
 * @author Galen Andrew
 * @author Roger Levy
 * @author Christopher Manning
 */
abstract class Relation implements Serializable {


  /**
   *
   */
  private static final long serialVersionUID = -1564793674551362909L;

  private final String symbol;

  /** Whether this relationship is satisfied between two trees.
   *
   * @param t1 The tree that is the left operand.
   * @param t2 The tree that is the right operand.
   * @param root The common root of t1 and t2
   * @return Whether this relationship is satisfied.
   */
  abstract boolean satisfies(Tree t1, Tree t2, Tree root);

  /**
   * For a given node, returns an {@link Iterator} over the nodes
   * of the tree containing the node that satisfy the relation.
   *
   * @param t A node in a Tree
   * @param matcher The matcher that nodes have to satisfy
   * @return An Iterator over the nodes
   *     of the root tree that satisfy the relation.
   */
  abstract Iterator<Tree> searchNodeIterator(Tree t,
                                             TregexMatcher matcher);

  private static final Pattern parentOfLastChild = Pattern.compile("(<-|<`)");

  private static final Pattern lastChildOfParent = Pattern.compile("(>-|>`)");

  /**
   * Static factory method for all relations with no arguments. Includes:
   * DOMINATES, DOMINATED_BY, PARENT_OF, CHILD_OF, PRECEDES,
   * IMMEDIATELY_PRECEDES, HAS_LEFTMOST_DESCENDANT, HAS_RIGHTMOST_DESCENDANT,
   * LEFTMOST_DESCENDANT_OF, RIGHTMOST_DESCENDANT_OF, SISTER_OF, LEFT_SISTER_OF,
   * RIGHT_SISTER_OF, IMMEDIATE_LEFT_SISTER_OF, IMMEDIATE_RIGHT_SISTER_OF,
   * HEADS, HEADED_BY, IMMEDIATELY_HEADS, IMMEDIATELY_HEADED_BY, ONLY_CHILD_OF,
   * HAS_ONLY_CHILD, EQUALS
   *
   * @param s The String representation of the relation
   * @return The singleton static relation of the specified type
   * @throws ParseException If bad relation s
   */
  static Relation getRelation(String s,
                              Function<String, String> basicCatFunction,
                              HeadFinder headFinder)
    throws ParseException
  {
    if (SIMPLE_RELATIONS_MAP.containsKey(s))
      return SIMPLE_RELATIONS_MAP.get(s);

    // these are shorthands for relations with arguments
    if (s.equals("<,")) {
      return getRelation("<", "1", basicCatFunction, headFinder);
    }
      if (parentOfLastChild.matcher(s).matches()) {
        return getRelation("<", "-1", basicCatFunction, headFinder);
      }
      if (s.equals(">,")) {
        return getRelation(">", "1", basicCatFunction, headFinder);
      }
      if (lastChildOfParent.matcher(s).matches()) {
        return getRelation(">", "-1", basicCatFunction, headFinder);
      }

      // finally try relations with headFinders
    Relation r;
      switch (s) {
          case ">>#":
              r = new Heads(headFinder);
              break;
          case "<<#":
              r = new HeadedBy(headFinder);
              break;
          case ">#":
              r = new ImmediatelyHeads(headFinder);
              break;
          case "<#":
              r = new ImmediatelyHeadedBy(headFinder);
              break;
          default:
              throw new ParseException("Unrecognized simple relation " + s);
      }

    return Interner.globalIntern(r);
  }

  /**
   * Static factory method for relations requiring an argument, including
   * HAS_ITH_CHILD, ITH_CHILD_OF, UNBROKEN_CATEGORY_DOMINATES,
   * UNBROKEN_CATEGORY_DOMINATED_BY.
   *
   * @param s The String representation of the relation
   * @param arg The argument to the relation, as a string; could be a node
   *          description or an integer
   * @return The singleton static relation of the specified type with the
   *         specified argument. Uses Interner to insure singleton-ity
   * @throws ParseException If bad relation s
   */
  static Relation getRelation(String s, String arg,
                              Function<String,String> basicCatFunction,
                              HeadFinder headFinder)
    throws ParseException
  {
    if (arg == null) {
      return getRelation(s, basicCatFunction, headFinder);
    }
    Relation r;
      switch (s) {
          case "<":
              r = new HasIthChild(Integer.parseInt(arg));
              break;
          case ">":
              r = new IthChildOf(Integer.parseInt(arg));
              break;
          case "<+":
              r = new UnbrokenCategoryDominates(arg, basicCatFunction);
              break;
          case ">+":
              r = new UnbrokenCategoryIsDominatedBy(arg, basicCatFunction);
              break;
          case ".+":
              r = new UnbrokenCategoryPrecedes(arg, basicCatFunction);
              break;
          case ",+":
              r = new UnbrokenCategoryFollows(arg, basicCatFunction);
              break;
          default:
              throw new ParseException("Unrecognized compound relation " + s + ' '
                      + arg);
      }
    return Interner.globalIntern(r);
  }

  private Relation(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }

  /**
   * This abstract Iterator implements a NULL iterator, but by subclassing and
   * overriding advance and/or initialize, it is an efficient implementation.
   */
  abstract static class SearchNodeIterator implements Iterator<Tree> {
    protected SearchNodeIterator() {
      initialize();
    }

    /**
     * This is the next tree to be returned by the iterator, or null if there
     * are no more items.
     */
    Tree next; // = null;

    /**
     * This method must insure that next points to first item, or null if there
     * are no items.
     */
    void initialize() {
      advance();
    }

    /**
     * This method must insure that next points to next item, or null if there
     * are no more items.
     */
    void advance() {
      next = null;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Tree next() {
      if (next == null) {
        throw new NoSuchElementException();
      }
      Tree ret = next;
      advance();
      return ret;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "SearchNodeIterator does not support remove().");
    }
  }

  static final Relation ROOT = new Relation("Root") {  // used in TregexParser

    private static final long serialVersionUID = -8311913236233762612L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return t1.equals(t2);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
        }
      };
    }
  };

  private static final Relation EQUALS = new Relation("==") {

    private static final long serialVersionUID = 164629344977943816L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return t1.equals(t2);
    }

    @Override
    Iterator<Tree> searchNodeIterator(Tree t,
                                      TregexMatcher matcher) {
      return Collections.singletonList(t).iterator();
    }

  };

  /* this is a "dummy" relation that allows you to segment patterns. */
  private static final Relation PATTERN_SPLITTER = new Relation(":") {

    private static final long serialVersionUID = 3409941930361386114L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return true;
    }

    @Override
    Iterator<Tree> searchNodeIterator(Tree t,
                                      TregexMatcher matcher) {
      return matcher.getRoot().iterator();
    }
  };

  private static final Relation DOMINATES = new Relation("<<") {

    private static final long serialVersionUID = -2580199434621268260L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return !t1.equals(t2) && t1.dominates(t2);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          for (int i = t.numChildren() - 1; i >= 0; i--) {
            searchStack.push(t.getChild(i));
          }
          if (!searchStack.isEmpty()) {
            advance();
          }
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            for (int i = next.numChildren() - 1; i >= 0; i--) {
              searchStack.push(next.getChild(i));
            }
          }
        }
      };
    }
  };

  private static final Relation DOMINATED_BY = new Relation(">>") {

    private static final long serialVersionUID = 6140614010121387690L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return DOMINATES.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = matcher.getParent(t);
        }

        @Override
        public void advance() {
          next = matcher.getParent(next);
        }
      };
    }
  };

  private static final Relation PARENT_OF = new Relation("<") {

    private static final long serialVersionUID = 9140193735607580808L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      Tree[] kids = t1.children();
        for (Tree kid : kids) {
            if (kid.equals(t2)) {
                return true;
            }
        }
      return false;
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        int nextNum; // subtle bug warning here: if we use int nextNum=0;

        // instead,

        // we get the first daughter twice because the assignment occurs after
        // advance() has already been
        // called once by the constructor of SearchNodeIterator.

        @Override
        public void advance() {
          if (nextNum < t.numChildren()) {
            next = t.getChild(nextNum);
            nextNum++;
          } else {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation CHILD_OF = new Relation(">") {

    private static final long serialVersionUID = 8919710375433372537L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return PARENT_OF.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = matcher.getParent(t);
        }
      };
    }
  };

  private static final Relation PRECEDES = new Relation("..") {

    private static final long serialVersionUID = -9065012389549976867L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return Trees.rightEdge(t1, root) <= Trees.leftEdge(t2, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          Tree current = t;
          Tree parent = matcher.getParent(t);
          while (parent != null) {
            for (int i = parent.numChildren() - 1; !parent.getChild(i).equals(current); i--) {
              searchStack.push(parent.getChild(i));
            }
            current = parent;
            parent = matcher.getParent(parent);
          }
          advance();
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            for (int i = next.numChildren() - 1; i >= 0; i--) {
              searchStack.push(next.getChild(i));
            }
          }
        }
      };
    }
  };

  private static final Relation IMMEDIATELY_PRECEDES = new Relation(".") {

    private static final long serialVersionUID = 3390147676937292768L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return Trees.leftEdge(t2, root) == Trees.rightEdge(t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          Tree current;
          Tree parent = t;
          do {
            current = parent;
            parent = matcher.getParent(parent);
            if (parent == null) {
              next = null;
              return;
            }
          } while (parent.lastChild().equals(current));

          for (int i = 1, n = parent.numChildren(); i < n; i++) {
            if (parent.getChild(i - 1).equals(current)) {
              next = parent.getChild(i);
              return;
            }
          }
        }

        @Override
        public void advance() {
            next = next.isLeaf() ? null : next.firstChild();
        }
      };
    }
  };

  private static final Relation FOLLOWS = new Relation(",,") {

    private static final long serialVersionUID = -5948063114149496983L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return Trees.rightEdge(t2, root) <= Trees.leftEdge(t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          Tree current = t;
          Tree parent = matcher.getParent(t);
          while (parent != null) {
            for (int i = 0; !parent.getChild(i).equals(current); i++) {
              searchStack.push(parent.getChild(i));
            }
            current = parent;
            parent = matcher.getParent(parent);
          }
          advance();
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            for (int i = next.numChildren() - 1; i >= 0; i--) {
              searchStack.push(next.getChild(i));
            }
          }
        }
      };
    }
  };

  private static final Relation IMMEDIATELY_FOLLOWS = new Relation(",") {

    private static final long serialVersionUID = -2895075562891296830L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return Trees.leftEdge(t1, root) == Trees.rightEdge(t2, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          Tree current;
          Tree parent = t;
          do {
            current = parent;
            parent = matcher.getParent(parent);
            if (parent == null) {
              next = null;
              return;
            }
          } while (parent.firstChild().equals(current));

          for (int i = 0, n = parent.numChildren() - 1; i < n; i++) {
            if (parent.getChild(i + 1).equals(current)) {
              next = parent.getChild(i);
              return;
            }
          }
        }

        @Override
        public void advance() {
            next = next.isLeaf() ? null : next.lastChild();
        }
      };
    }
  };

  private static final Relation HAS_LEFTMOST_DESCENDANT = new Relation("<<,") {

    private static final long serialVersionUID = -7352081789429366726L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
        return !t1.isLeaf() && (t1.children()[0].equals(t2)
                || satisfies(t1.children()[0], t2, root));
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
            next = next.isLeaf() ? null : next.firstChild();
        }
      };
    }
  };

  private static final Relation HAS_RIGHTMOST_DESCENDANT = new Relation("<<-") {

    private static final long serialVersionUID = -1405509785337859888L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t1.isLeaf()) {
        return false;
      } else {
        Tree lastKid = t1.children()[t1.children().length - 1];
        return lastKid.equals(t2) || satisfies(lastKid, t2, root);
      }
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
            next = next.isLeaf() ? null : next.lastChild();
        }
      };
    }
  };

  private static final Relation LEFTMOST_DESCENDANT_OF = new Relation(">>,") {

    private static final long serialVersionUID = 3103412865783190437L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return HAS_LEFTMOST_DESCENDANT.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
          Tree last = next;
          next = matcher.getParent(next);
          if (next != null && !next.firstChild().equals(last)) {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation RIGHTMOST_DESCENDANT_OF = new Relation(">>-") {

    private static final long serialVersionUID = -2000255467314675477L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return HAS_RIGHTMOST_DESCENDANT.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
          Tree last = next;
          next = matcher.getParent(next);
          if (next != null && !next.lastChild().equals(last)) {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation SISTER_OF = new Relation("$") {

    private static final long serialVersionUID = -3776688096782419004L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t1.equals(t2) || t1.equals(root)) {
        return false;
      }
      Tree parent = t1.parent(root);
      return PARENT_OF.satisfies(parent, t2, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Tree parent;

        int nextNum;

        @Override
        void initialize() {
          parent = matcher.getParent(t);
          if (parent != null) {
            nextNum = 0;
            advance();
          }
        }

        @Override
        public void advance() {
          if (nextNum < parent.numChildren()) {
            next = parent.getChild(nextNum++);
            if (next.equals(t)) {
              advance();
            }
          } else {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation LEFT_SISTER_OF = new Relation("$++") {

    private static final long serialVersionUID = -4516161080140406862L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t1.equals(t2) || t1.equals(root)) {
        return false;
      }
      Tree parent = t1.parent(root);
      Tree[] kids = parent.children();
      for (int i = kids.length - 1; i > 0; i--) {
        if (kids[i].equals(t1)) {
          return false;
        }
        if (kids[i].equals(t2)) {
          return true;
        }
      }
      return false;
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Tree parent;

        int nextNum;

        @Override
        void initialize() {
          parent = matcher.getParent(t);
          if (parent != null) {
            nextNum = parent.numChildren() - 1;
            advance();
          }
        }

        @Override
        public void advance() {
          next = parent.getChild(nextNum--);
          if (next.equals(t)) {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation RIGHT_SISTER_OF = new Relation("$--") {

    private static final long serialVersionUID = -5880626025192328694L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return LEFT_SISTER_OF.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Tree parent;

        int nextNum;

        @Override
        void initialize() {
          parent = matcher.getParent(t);
          if (parent != null) {
            nextNum = 0;
            advance();
          }
        }

        @Override
        public void advance() {
          next = parent.getChild(nextNum++);
          if (next.equals(t)) {
            next = null;
          }
        }
      };
    }
  };

  private static final Relation IMMEDIATE_LEFT_SISTER_OF = new Relation("$+") {

    private static final long serialVersionUID = 7745237994722126917L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t1.equals(t2) || t1.equals(root)) {
        return false;
      }
      Tree[] sisters = t1.parent(root).children();
      for (int i = sisters.length - 1; i > 0; i--) {
        if (sisters[i].equals(t1)) {
          return false;
        }
        if (sisters[i].equals(t2)) {
          return sisters[i - 1].equals(t1);
        }
      }
      return false;
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.equals(matcher.getRoot())) {
            Tree parent = matcher.getParent(t);
            int i = 0;
            while (!parent.getChild(i).equals(t)) {
              i++;
            }
            if (i + 1 < parent.numChildren()) {
              next = parent.getChild(i + 1);
            }
          }
        }
      };
    }
  };

  private static final Relation IMMEDIATE_RIGHT_SISTER_OF = new Relation("$-") {

    private static final long serialVersionUID = -6555264189937531019L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return IMMEDIATE_LEFT_SISTER_OF.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.equals(matcher.getRoot())) {
            Tree parent = matcher.getParent(t);
            int i = 0;
            while (!parent.getChild(i).equals(t)) {
              i++;
            }
            if (i > 0) {
              next = parent.getChild(i - 1);
            }
          }
        }
      };
    }
  };

  private static final Relation ONLY_CHILD_OF = new Relation(">:") {

    private static final long serialVersionUID = 1719812660770087879L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return t2.children().length == 1 && t2.firstChild().equals(t1);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.equals(matcher.getRoot())) {
            next = matcher.getParent(t);
            if (next.numChildren() != 1) {
              next = null;
            }
          }
        }
      };
    }
  };

  private static final Relation HAS_ONLY_CHILD = new Relation("<:") {

    private static final long serialVersionUID = -8776487500849294279L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return t1.children().length == 1 && t1.firstChild().equals(t2);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.isLeaf() && t.numChildren() == 1) {
            next = t.firstChild();
          }
        }
      };
    }
  };

  private static final Relation UNARY_PATH_ANCESTOR_OF = new Relation("<<:") {

    private static final long serialVersionUID = -742912038636163403L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t1.isLeaf() || t1.children().length > 1)
        return false;
      Tree onlyDtr = t1.children()[0];
        return onlyDtr.equals(t2) || satisfies(onlyDtr, t2, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          if (!t.isLeaf() && t.children().length == 1)
            searchStack.push(t.getChild(0));
          if (!searchStack.isEmpty()) {
            advance();
          }
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            if (!next.isLeaf() && next.children().length == 1)
              searchStack.push(next.getChild(0));
          }
        }
      };
    }
  };

  private static final Relation UNARY_PATH_DESCENDANT_OF = new Relation(">>:") {

    private static final long serialVersionUID = 4364021807752979404L;

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t2.isLeaf() || t2.children().length > 1)
        return false;
      Tree onlyDtr = t2.children()[0];
        return onlyDtr.equals(t1) || satisfies(t1, onlyDtr, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          Tree parent = matcher.getParent(t);
          if (parent != null && !parent.isLeaf() &&
              parent.children().length == 1)
            searchStack.push(parent);
          if (!searchStack.isEmpty()) {
            advance();
          }
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            Tree parent = matcher.getParent(next);
            if (parent != null && !parent.isLeaf() &&
                parent.children().length == 1)
              searchStack.push(parent);
          }
        }
      };
    }
  };

  private static final Relation[] SIMPLE_RELATIONS = {
      DOMINATES, DOMINATED_BY, PARENT_OF, CHILD_OF, PRECEDES,
      IMMEDIATELY_PRECEDES, FOLLOWS, IMMEDIATELY_FOLLOWS,
          HAS_LEFTMOST_DESCENDANT, HAS_RIGHTMOST_DESCENDANT,
          LEFTMOST_DESCENDANT_OF, RIGHTMOST_DESCENDANT_OF, SISTER_OF,
      LEFT_SISTER_OF, RIGHT_SISTER_OF, IMMEDIATE_LEFT_SISTER_OF,
      IMMEDIATE_RIGHT_SISTER_OF, ONLY_CHILD_OF, HAS_ONLY_CHILD, EQUALS,
      PATTERN_SPLITTER,UNARY_PATH_ANCESTOR_OF, UNARY_PATH_DESCENDANT_OF};

  private static final Map<String, Relation> SIMPLE_RELATIONS_MAP = new FastMap<>();

    static {
    for (Relation r : SIMPLE_RELATIONS) {
      SIMPLE_RELATIONS_MAP.put(r.symbol, r);
    }
    SIMPLE_RELATIONS_MAP.put("<<`", HAS_RIGHTMOST_DESCENDANT);
    SIMPLE_RELATIONS_MAP.put("<<,", HAS_LEFTMOST_DESCENDANT);
    SIMPLE_RELATIONS_MAP.put(">>`", RIGHTMOST_DESCENDANT_OF);
    SIMPLE_RELATIONS_MAP.put(">>,", LEFTMOST_DESCENDANT_OF);
    SIMPLE_RELATIONS_MAP.put("$..", LEFT_SISTER_OF);
    SIMPLE_RELATIONS_MAP.put("$,,", RIGHT_SISTER_OF);
    SIMPLE_RELATIONS_MAP.put("$.", IMMEDIATE_LEFT_SISTER_OF);
    SIMPLE_RELATIONS_MAP.put("$,", IMMEDIATE_RIGHT_SISTER_OF);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Relation)) {
      return false;
    }

    Relation relation = (Relation) o;

    return symbol.equals(relation.symbol);
  }

  @Override
  public int hashCode() {
    return symbol.hashCode();
  }


  private static class Heads extends Relation {

    private static final long serialVersionUID = 4681433462932265831L;

    final HeadFinder hf;

    Heads(HeadFinder hf) {
      super(">>#");
      this.hf = hf;
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      if (t2.isLeaf()) {
        return false;
      } else if (t2.isPreTerminal()) {
        return t2.firstChild().equals(t1);
      } else {
        Tree head = hf.determineHead(t2);
          return head.equals(t1) || satisfies(t1, head, root);
      }
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
          Tree last = next;
          next = matcher.getParent(next);
          if (next != null && !hf.determineHead(next).equals(last)) {
            next = null;
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Heads)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      Heads heads = (Heads) o;

        return !(hf != null ? !hf.equals(heads.hf) : heads.hf != null);

    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + (hf != null ? hf.hashCode() : 0);
      return result;
    }
  }


  private static class HeadedBy extends Relation {

    private static final long serialVersionUID = 2825997185749055693L;

    private final Heads heads;

    HeadedBy(HeadFinder hf) {
      super("<<#");
      this.heads = Interner.globalIntern(new Heads(hf));
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return heads.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = t;
          advance();
        }

        @Override
        public void advance() {
            next = next.isLeaf() ? null : heads.hf.determineHead(next);
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof HeadedBy)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      HeadedBy headedBy = (HeadedBy) o;

        return !(heads != null ? !heads.equals(headedBy.heads)
                : headedBy.heads != null);

    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + (heads != null ? heads.hashCode() : 0);
      return result;
    }
  }


  private static class ImmediatelyHeads extends Relation {


    private static final long serialVersionUID = 2085410152913894987L;

    private final HeadFinder hf;

    ImmediatelyHeads(HeadFinder hf) {
      super(">#");
      this.hf = hf;
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return hf.determineHead(t2).equals(t1);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.equals(matcher.getRoot())) {
            next = matcher.getParent(t);
            if (!hf.determineHead(next).equals(t)) {
              next = null;
            }
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ImmediatelyHeads)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      ImmediatelyHeads immediatelyHeads = (ImmediatelyHeads) o;

        return hf.equals(immediatelyHeads.hf);

    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + hf.hashCode();
      return result;
    }
  }


  private static class ImmediatelyHeadedBy extends Relation {

    private static final long serialVersionUID = 5910075663419780905L;

    private final ImmediatelyHeads immediatelyHeads;

    ImmediatelyHeadedBy(HeadFinder hf) {
      super("<#");
      this.immediatelyHeads = Interner
          .globalIntern(new ImmediatelyHeads(hf));
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return immediatelyHeads.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.isLeaf()) {
            next = immediatelyHeads.hf.determineHead(t);
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ImmediatelyHeadedBy)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      ImmediatelyHeadedBy immediatelyHeadedBy = (ImmediatelyHeadedBy) o;

        return !(immediatelyHeads != null ? !immediatelyHeads
                .equals(immediatelyHeadedBy.immediatelyHeads)
                : immediatelyHeadedBy.immediatelyHeads != null);

    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result
          + (immediatelyHeads != null ? immediatelyHeads.hashCode() : 0);
      return result;
    }
  }


  private static class IthChildOf extends Relation {

    private static final long serialVersionUID = -1463126827537879633L;

    private final int childNum;

    IthChildOf(int i) {
      super('>' + String.valueOf(i));
      if (i == 0) {
        throw new IllegalArgumentException(
            "Error -- no such thing as zeroth child!");
      } else {
        childNum = i;
      }
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      Tree[] kids = t2.children();
        return kids.length >= Math.abs(childNum) && (childNum > 0 && kids[childNum - 1].equals(t1) || childNum < 0 && kids[kids.length + childNum].equals(t1));
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          if (!t.equals(matcher.getRoot())) {
            next = matcher.getParent(t);
            if (childNum > 0
                && (next.numChildren() < childNum || !next
                    .getChild(childNum - 1).equals(t))
                || childNum < 0
                && (next.numChildren() < -childNum || !next.getChild(next
                    .numChildren()
                    + childNum).equals(t))) {
              next = null;
            }
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof IthChildOf)) {
        return false;
      }

      IthChildOf ithChildOf = (IthChildOf) o;

        return childNum == ithChildOf.childNum;

    }

    @Override
    public int hashCode() {
      return childNum;
    }

  }


  private static class HasIthChild extends Relation {

    private static final long serialVersionUID = 3546853729291582806L;

    private final IthChildOf ithChildOf;

    HasIthChild(int i) {
      super('<' + String.valueOf(i));
      ithChildOf = Interner.globalIntern(new IthChildOf(i));
    }

    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return ithChildOf.satisfies(t2, t1, root);
    }

    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          int childNum = ithChildOf.childNum;
          if (t.numChildren() >= Math.abs(childNum)) {
              next = childNum > 0 ? t.getChild(childNum - 1) : t.getChild(t.numChildren() + childNum);
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof HasIthChild)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      HasIthChild hasIthChild = (HasIthChild) o;

        return !(ithChildOf != null ? !ithChildOf.equals(hasIthChild.ithChildOf)
                : hasIthChild.ithChildOf != null);

    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + (ithChildOf != null ? ithChildOf.hashCode() : 0);
      return result;
    }
  }




  private static class UnbrokenCategoryDominates extends Relation {

    private static final long serialVersionUID = -4174923168221859262L;
      private static final Pattern COMPILE = Pattern.compile("__");
      private static final Pattern PATTERN = Pattern.compile("/.*/");

      private final Pattern pattern;
    private final boolean negatedPattern;
    private final boolean basicCat;
    private Function<String, String> basicCatFunction;


    /**
     *
     * @param arg This may have a ! and then maybe a @ and then either an
     *            identifier or regex
     */
    UnbrokenCategoryDominates(String arg,
                              Function<String, String> basicCatFunction) {
      super("<+(" + arg + ')');
      if (!arg.isEmpty() && arg.charAt(0) == '!') {
        negatedPattern = true;
        arg = arg.substring(1);
      } else {
        negatedPattern = false;
      }
      if (!arg.isEmpty() && arg.charAt(0) == '@') {
        basicCat = true;
        this.basicCatFunction = basicCatFunction;
        arg = arg.substring(1);
      } else {
        basicCat = false;
      }
      if (PATTERN.matcher(arg).matches()) {
        pattern = Pattern.compile(arg.substring(1, arg.length() - 1));
      } else pattern = COMPILE.matcher(arg).matches() ? Pattern.compile("^.*$") : Pattern.compile("^(?:" + arg + ")$");
    }

    /** {@inheritDoc} */
    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      for (Tree kid : t1.children()) {
        if (kid.equals(t2)) {
          return true;
        } else {
          if (pathMatchesNode(kid) && satisfies(kid, t2, root)) {
            return true;
          }
        }
      }
      return false;
    }

    private boolean pathMatchesNode(Tree node) {
      String lab = node.value();
      // added this code to not crash if null node, even though there probably should be null nodes in the tree
      if (lab == null) {
        // Say that a null label matches no positive pattern, but any negated patern
        return negatedPattern;
      } else {
        if (basicCat) {
          lab = basicCatFunction.apply(lab);
        }
        Matcher m = pattern.matcher(lab);
        return m.find() != negatedPattern;
      }
    }

    /** {@inheritDoc} */
    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      TregexMatcher matcher) {
      return new SearchNodeIterator() {
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          searchStack = new Stack<>();
          for (int i = t.numChildren() - 1; i >= 0; i--) {
            searchStack.push(t.getChild(i));
          }
          if (!searchStack.isEmpty()) {
            advance();
          }
        }

        @Override
        void advance() {
          if (searchStack.isEmpty()) {
            next = null;
          } else {
            next = searchStack.pop();
            if (pathMatchesNode(next)) {
              for (int i = next.numChildren() - 1; i >= 0; i--) {
                searchStack.push(next.getChild(i));
              }
            }
          }
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UnbrokenCategoryDominates)) {
        return false;
      }

      UnbrokenCategoryDominates unbrokenCategoryDominates = (UnbrokenCategoryDominates) o;

        return negatedPattern == unbrokenCategoryDominates.negatedPattern && pattern.equals(unbrokenCategoryDominates.pattern);

    }

    @Override
    public int hashCode() {
      int result;
      result = pattern.hashCode();
      result = 29 * result + (negatedPattern ? 1 : 0);
      return result;
    }

  } // end class UnbrokenCategoryDominates


  private static class UnbrokenCategoryIsDominatedBy extends Relation {

    private static final long serialVersionUID = 2867922828235355129L;

    private final UnbrokenCategoryDominates unbrokenCategoryDominates;

    UnbrokenCategoryIsDominatedBy(String arg,
                                  Function<String, String> basicCatFunction) {
      super(">+(" + arg + ')');
      unbrokenCategoryDominates = Interner
        .globalIntern(new UnbrokenCategoryDominates(arg, basicCatFunction));
    }

    /** {@inheritDoc} */
    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return unbrokenCategoryDominates.satisfies(t2, t1, root);
    }

    /** {@inheritDoc} */
    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        @Override
        void initialize() {
          next = matcher.getParent(t);
        }

        @Override
        public void advance() {
            next = unbrokenCategoryDominates.pathMatchesNode(next) ? matcher.getParent(next) : null;
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UnbrokenCategoryIsDominatedBy)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      UnbrokenCategoryIsDominatedBy unbrokenCategoryIsDominatedBy = (UnbrokenCategoryIsDominatedBy) o;

      return unbrokenCategoryDominates.equals(unbrokenCategoryIsDominatedBy.unbrokenCategoryDominates);
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 29 * result + unbrokenCategoryDominates.hashCode();
      return result;
    }
  }


  /**
   * Note that this only works properly for context-free trees.
   * Also, the use of initialize and advance is not very efficient just yet.  Finally, each node in the tree
   * is added only once, even if there is more than one unbroken-category precedence path to it.
   *
   */
  private static class UnbrokenCategoryPrecedes extends Relation {

    private static final long serialVersionUID = 6866888667804306111L;
      private static final Pattern COMPILE = Pattern.compile("__");
      private static final Pattern PATTERN = Pattern.compile("/.*/");

      private final Pattern pattern;
    private final boolean negatedPattern;
    private final boolean basicCat;
    private Function<String, String> basicCatFunction;

    /**
     * @param arg The pattern to match, perhaps preceded by ! and/or @
     */
    UnbrokenCategoryPrecedes(String arg,
                             Function<String, String> basicCatFunction) {
      super(".+(" + arg + ')');
      if (!arg.isEmpty() && arg.charAt(0) == '!') {
        negatedPattern = true;
        arg = arg.substring(1);
      } else {
        negatedPattern = false;
      }
      if (!arg.isEmpty() && arg.charAt(0) == '@') {
        basicCat = true;
        this.basicCatFunction = basicCatFunction; // todo -- this was missing a this. which must be testable in a unit test!!! Make one
        arg = arg.substring(1);
      } else {
        basicCat = false;
      }
      if (PATTERN.matcher(arg).matches()) {
        pattern = Pattern.compile(arg.substring(1, arg.length() - 1));
      } else pattern = COMPILE.matcher(arg).matches() ? Pattern.compile("^.*$") : Pattern.compile("^(?:" + arg + ")$");
    }

    /** {@inheritDoc} */
    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return true; // shouldn't have to do anything here.
    }

    private boolean pathMatchesNode(Tree node) {
      String lab = node.value();
      // added this code to not crash if null node, even though there probably should be null nodes in the tree
      if (lab == null) {
        // Say that a null label matches no positive pattern, but any negated pattern
        return negatedPattern;
      } else {
        if (basicCat) {
          lab = basicCatFunction.apply(lab);
        }
        Matcher m = pattern.matcher(lab);
        return m.find() != negatedPattern;
      }
    }

    /** {@inheritDoc} */
    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        private IdentityHashSet<Tree> nodesToSearch;
        private Stack<Tree> searchStack;

        @Override
        public void initialize() {
          nodesToSearch = new IdentityHashSet<>();
          searchStack = new Stack<>();
          initializeHelper(searchStack, t, matcher.getRoot());
          advance();
        }

        private void initializeHelper(Stack<Tree> stack, Tree node, Tree root) {
          if (node.equals(root)) {
            return;
          }
          Tree parent = matcher.getParent(node);
          int i = parent.objectIndexOf(node);
          while (i == parent.children().length-1 && !parent.equals(root)) {
            node = parent;
            parent = matcher.getParent(parent);
            i = parent.objectIndexOf(node);
          }
          Tree followingNode;
            followingNode = i + 1 < parent.children().length ? parent.children()[i + 1] : null;
          while (followingNode != null) {
            //System.err.println("adding to stack node " + followingNode.toString());
            if (! nodesToSearch.contains(followingNode)) {
              stack.add(followingNode);
              nodesToSearch.add(followingNode);
            }
            if (pathMatchesNode(followingNode)) {
              initializeHelper(stack, followingNode, root);
            }
              followingNode = !followingNode.isLeaf() ? followingNode.children()[0] : null;
          }
        }

        @Override
        void advance() {
            next = searchStack.isEmpty() ? null : searchStack.pop();
        }
      };
    }
  }


  /**
   * Note that this only works properly for context-free trees.
   * Also, the use of initialize and advance is not very efficient just yet.  Finally, each node in the tree
   * is added only once, even if there is more than one unbroken-category precedence path to it.
   */
  private static class UnbrokenCategoryFollows extends Relation {

    private static final long serialVersionUID = -7890430001297866437L;
      private static final Pattern COMPILE = Pattern.compile("__");
      private static final Pattern PATTERN = Pattern.compile("/.*/");

      private final Pattern pattern;
    private final boolean negatedPattern;
    private final boolean basicCat;
    private Function<String, String> basicCatFunction;

    /**
     * @param arg The pattern to match, perhaps preceded by ! and/or @
     */
    UnbrokenCategoryFollows(String arg,
                            Function<String, String> basicCatFunction) {
      super(",+(" + arg + ')');
      if (!arg.isEmpty() && arg.charAt(0) == '!') {
        negatedPattern = true;
        arg = arg.substring(1);
      } else {
        negatedPattern = false;
      }
      if (!arg.isEmpty() && arg.charAt(0) == '@') {
        basicCat = true;
        this.basicCatFunction = basicCatFunction;
        arg = arg.substring(1);
      } else {
        basicCat = false;
      }
      if (PATTERN.matcher(arg).matches()) {
        pattern = Pattern.compile(arg.substring(1, arg.length() - 1));
      } else pattern = COMPILE.matcher(arg).matches() ? Pattern.compile("^.*$") : Pattern.compile("^(?:" + arg + ")$");
    }

    /** {@inheritDoc} */
    @Override
    boolean satisfies(Tree t1, Tree t2, Tree root) {
      return true; // shouldn't have to do anything here.
    }

    private boolean pathMatchesNode(Tree node) {
      String lab = node.value();
      // added this code to not crash if null node, even though there probably should be null nodes in the tree
      if (lab == null) {
        // Say that a null label matches no positive pattern, but any negated pattern
        return negatedPattern;
      } else {
        if (basicCat) {
          lab = basicCatFunction.apply(lab);
        }
        Matcher m = pattern.matcher(lab);
        return m.find() != negatedPattern;
      }
    }

    /** {@inheritDoc} */
    @Override
    Iterator<Tree> searchNodeIterator(final Tree t,
                                      final TregexMatcher matcher) {
      return new SearchNodeIterator() {
        IdentityHashSet<Tree> nodesToSearch;
        Stack<Tree> searchStack;

        @Override
        public void initialize() {
          nodesToSearch = new IdentityHashSet<>();
          searchStack = new Stack<>();
          initializeHelper(searchStack, t, matcher.getRoot());
          advance();
        }

        private void initializeHelper(Stack<Tree> stack, Tree node, Tree root) {
          if (node.equals(root)) {
            return;
          }
          Tree parent = matcher.getParent(node);
          int i = parent.objectIndexOf(node);
          while (i == 0 && !parent.equals(root)) {
            node = parent;
            parent = matcher.getParent(parent);
            i = parent.objectIndexOf(node);
          }
          Tree precedingNode;
            precedingNode = i > 0 ? parent.children()[i - 1] : null;
          while (precedingNode != null) {
            //System.err.println("adding to stack node " + precedingNode.toString());
            if ( ! nodesToSearch.contains(precedingNode)) {
              stack.add(precedingNode);
              nodesToSearch.add(precedingNode);
            }
            if (pathMatchesNode(precedingNode)) {
              initializeHelper(stack, precedingNode, root);
            }
              precedingNode = !precedingNode.isLeaf() ? precedingNode.children()[0] : null;
          }
        }

        @Override
        void advance() {
            next = searchStack.isEmpty() ? null : searchStack.pop();
        }
      };
    }
  }

}
