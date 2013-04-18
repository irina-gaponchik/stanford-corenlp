package edu.stanford.nlp.trees;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasIndex;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.objectbank.ObjectBank;


/**
 * A {@code MemoryTreebank} object stores a corpus of examples with
 * given tree structures in memory (as a {@code List}).
 *
 * @author Christopher Manning
 * @version 2004/09/01
 */
public final class MemoryTreebank extends Treebank implements FileProcessor, List<Tree> {

  private static final boolean PRINT_FILENAMES = false;

  /** THIS IS AT PRESENT DELETED, UNLESS PROBLEMS RECUR.
   * If this is true, the system will retry opening files a few times
   * before concluding that there is really a problem. This seems to
   * be necessary with NFS on Linux boxes -- at least the DB ones.
   */
  //  private static final boolean BROKEN_NFS = true;


  /**
   * The collection of parse trees.
   */
  private final List<Tree> parseTrees;

  /**
   * Create a new tree bank.
   * The trees are made with a {@code LabeledScoredTreeReaderFactory}.
   * <p/>
   * <i>Compatibility note: Until Sep 2004, this used to create a Treebank
   * with a SimpleTreeReaderFactory, but this was changed as the old
   * default wasn't very useful, especially to naive users. This one now
   * uses a LabledScoredTreeReaderFactory with a no-op TreeNormalizer.</i>
   */
  public MemoryTreebank() {
    this(new LabeledScoredTreeReaderFactory(new TreeNormalizer()));
  }

  /**
   * Create a new tree bank, using a specific TreeNormalizer.
   * The trees are made with a {@code LabeledScoredTreeReaderFactory}.
   * <p/>
   * <i>Compatibility note: Until Sep 2004, this used to create a Treebank
   * with a SimpleTreeReaderFactory, but this was changed as the old
   * default wasn't very useful, especially to naive users.</i>
   */
  public MemoryTreebank(TreeNormalizer tm) {
    this(new LabeledScoredTreeReaderFactory(tm));
  }

  /**
   * Create a new tree bank, set the encoding for file access
   *
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(String encoding) {
    this(new LabeledScoredTreeReaderFactory(), encoding);
  }

  /**
   * Create a new tree bank.
   *
   * @param trf the factory class to be called to create a new
   *            {@code TreeReader}
   */
  public MemoryTreebank(TreeReaderFactory trf) {
    super(trf);
    parseTrees = new ArrayList<>();
  }


  /**
   * Create a new tree bank.
   *
   * @param trf      the factory class to be called to create a new
   *                 {@code TreeReader}
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(TreeReaderFactory trf, String encoding) {
    super(trf, encoding);
    parseTrees = new ArrayList<>();
  }

  /**
   * Create a new tree bank.  The list of trees passed in is simply placed
   * in the Treebank.  It is not copied.
   *
   * @param trees    The trees to put in the Treebank.
   * @param trf      the factory class to be called to create a new
   *                 {@code TreeReader}
   * @param encoding the encoding to use for file access.
   */
  public MemoryTreebank(List<Tree> trees, TreeReaderFactory trf, String encoding) {
    super(trf, encoding);
    parseTrees = trees;
  }

  /**
   * Create a new Treebank.
   *
   * @param initialCapacity The initial size of the underlying Collection,
   *                        (if a Collection-based storage mechanism is being provided)
   */
  public MemoryTreebank(int initialCapacity) {
    this(initialCapacity, new LabeledScoredTreeReaderFactory(new TreeNormalizer()));
  }


  /**
   * Create a new tree bank.
   *
   * @param initialCapacity The initial size of the underlying Collection
   * @param trf             the factory class to be called to create a new
   *                        {@code TreeReader}
   */
  public MemoryTreebank(int initialCapacity, TreeReaderFactory trf) {
    super(initialCapacity, trf);
    parseTrees = new ArrayList<>(initialCapacity);
  }


  /**
   * Empty a {@code Treebank}.
   */
  @Override
  public void clear() {
    parseTrees.clear();
  }


  /**
   * Load trees from given directory.
   *
   * @param path file or directory to load from
   * @param filt a FilenameFilter of files to load
   */
  @Override
  public void loadPath(File path, FileFilter filt) {
    FilePathProcessor.processPath(path, filt, this);
  }

  public void loadPath(String path, FileFilter filt, String srlFile) {
    readSRLFile(srlFile);
    FilePathProcessor.processPath(new File(path), filt, this);
    srlMap = null;
  }

  private Map<String,CollectionValuedMap<Integer,String>> srlMap;

  private void readSRLFile(String srlFile) {
    srlMap = Generics.newHashMap();
    for (String line : ObjectBank.getLineIterator(new File(srlFile))) {
      String[] bits = line.split("\\s+", 3);
      String filename = bits[0];
      int treeNum = Integer.parseInt(bits[1]);
      String info = bits[2];
      CollectionValuedMap<Integer,String> cvm = srlMap.get(filename);
      if (cvm == null) {
        cvm = new CollectionValuedMap<>();
        srlMap.put(filename, cvm);
      }
      cvm.add(treeNum, info);
    }
  }

  /**
   * Load a collection of parse trees from the file of given name.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   * This methods implements the {@code FileProcessor} interface.
   *
   * @param file file to load a tree from
   */
  public void processFile(File file) {
    TreeReader tr = null;

    // SRL stuff
    CollectionValuedMap<Integer,String> srlMap = null;
    if (this.srlMap != null) {
      // there must be a better way ...
      String filename = file.getAbsolutePath();
      for (Map.Entry<String, CollectionValuedMap<Integer, String>> stringCollectionValuedMapEntry : this.srlMap.entrySet()) {
        if (filename.endsWith(stringCollectionValuedMapEntry.getKey())) {
          srlMap = this.srlMap.get(stringCollectionValuedMapEntry.getKey());
          break;
        }
      }
      if (srlMap == null) {
        System.err.println("could not find SRL entries for file: "+file);
      }
    }

    try {
      // maybe print file name to stdout to get some feedback
      if (PRINT_FILENAMES) {
        System.err.println(file);
      }
      // could throw an IO exception if can't open for reading
      tr = treeReaderFactory().newTreeReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding())));
      int sentIndex=0;
      Tree pt;
      while ((pt = tr.readTree()) != null) {
        if (pt.label() instanceof HasIndex) { // so we can trace where this tree came from
          HasIndex hi = (HasIndex) pt.label();
          hi.setDocID(file.getName());
          hi.setSentIndex(sentIndex);
        }
          if (srlMap != null) {
            Collection<String> srls = srlMap.get(sentIndex);
  //           pt.pennPrint();
  //           System.err.println(srls);
            parseTrees.add(pt);
              if (!srls.isEmpty()) {
                  for (String srl : srls) {
        //              Tree t = pt.deepCopy();
                      String[] bits = srl.split("\\s+");
                      int verbIndex = Integer.parseInt(bits[0]);
                      String lemma = bits[2].split("\\.")[0];
        //              Tree verb = Trees.getTerminal(t, verbIndex);
                      Tree verb = Trees.getTerminal(pt, verbIndex);
        //              ((CoreLabel)verb.label()).set(SRLIDAnnotation.class, SRL_ID.REL);
                      ((TypesafeMap) verb.label()).set(CoreAnnotations.CoNLLPredicateAnnotation.class, true);
                      int length = bits.length;
                      for (int i = 4; i < length; i++) {
                          String arg = bits[i];
                          String[] bits1 = arg.split("-");
                          String locs = bits1[0];
                          String argType = bits1[1];
                          if (!argType.equals("rel")) for (String loc : locs.split("[*,]")) {
                              bits1 = loc.split(":");
                              int term = Integer.parseInt(bits1[0]);
                              int height = Integer.parseInt(bits1[1]);
                              //                  Tree t1 = Trees.getPreTerminal(t, term);
                              Tree t1 = Trees.getPreTerminal(pt, term);
                              for (int j = 0; j < height; j++) {
                                  //                    t1 = t1.parent(t);
                                  t1 = t1.parent(pt);
                              }
                              Map<Integer, String> roleMap = ((ArrayCoreMap) t1.label()).get(CoreAnnotations.CoNLLSRLAnnotation.class);
                              if (roleMap == null) {
                                  roleMap = Generics.newHashMap();
                                  ((TypesafeMap) t1.label()).set(CoreAnnotations.CoNLLSRLAnnotation.class, roleMap);
                              }
                              roleMap.put(verbIndex, argType);
                              //                  ((CoreLabel)t1.label()).set(SRLIDAnnotation.class, SRL_ID.ARG);
                          }
                      }
                  }
              }
          } else {
            parseTrees.add(pt);
          }

          sentIndex++;
      }
    } catch (IOException e) {
      throw new RuntimeIOException("MemoryTreebank.processFile IOException in file " + file, e);
    } finally {
      IOUtils.closeIgnoringExceptions(tr);
    }
  }


  /**
   * Load a collection of parse trees from a Reader.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   *
   * @param r The reader to read trees from.  (If you want it buffered,
   *    you should already have buffered it!)
   */
  public void load(Reader r) {
    load(r, null);
  }

  /**
   * Load a collection of parse trees from a Reader.
   * Each tree may optionally be encased in parens to allow for Penn
   * Treebank style trees.
   *
   * @param r The reader to read trees from.  (If you want it buffered,
   *    you should already have buffered it!)
   * @param id An ID for where these files come from (arbitrary, but
   *    something like a filename.  Can be {@code null} for none.
   */
  public void load(Reader r, String id) {
    try {
      // could throw an IO exception?
      TreeReader tr = treeReaderFactory().newTreeReader(r);
      int sentIndex = 0;
      for (Tree pt; (pt = tr.readTree()) != null; ) {
        if (pt.label() instanceof HasIndex) { // so we can trace where this tree came from
          HasIndex hi = (HasIndex) pt.label();
          if (id != null) {
            hi.setDocID(id);
          }
          hi.setSentIndex(sentIndex);
        }
        parseTrees.add(pt);
        sentIndex++;
      }
    } catch (IOException e) {
      System.err.println("load IO Exception: " + e);
    }
  }


  /**
   * Get a tree by index from the Treebank.
   * This operation isn't in the {@code Treebank} feature set, and
   * so is only available with a {@code MemoryTreebank}, but is
   * useful in allowing the latter to be used as a {@code List}.
   *
   * @param i The integer (counting from 0) index of the tree
   * @return A tree
   */
  public Tree get(int i) {
    return parseTrees.get(i);
  }


  /**
   * Apply the TreeVisitor tp to all trees in the Treebank.
   *
   * @param tp A class that implements the TreeVisitor interface
   */
  @Override
  public void apply(TreeVisitor tp) {
      for (Tree parseTree : parseTrees) {
          tp.visitTree(parseTree);
      }
    // or could do as Iterator but slower
    // Iterator iter = parseTrees.iterator();
    // while (iter.hasNext()) {
    //    tp.visitTree((Tree) iter.next());
    // }
  }


  /**
   * Return an Iterator over Trees in the Treebank.
   *
   * @return The iterator
   */
  @Override
  public Iterator<Tree> iterator() {
    return parseTrees.iterator();
  }


  /**
   * Returns the size of the Treebank.
   * Provides a more efficient implementation than the one for a
   * generic {@code Treebank}
   *
   * @return the number of trees in the Treebank
   */
  @Override
  public int size() {
    return parseTrees.size();
  }


  // Extra stuff to implement List interface

  public void add(int index, Tree element) {
    parseTrees.add(index, element);
  }

  @Override
  public boolean add(Tree element) {
    return parseTrees.add(element);
  }


  public boolean addAll(int index, Collection<? extends Tree> c) {
    return parseTrees.addAll(index, c);
  }

  public int indexOf(Object o) {
    return parseTrees.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return parseTrees.lastIndexOf(o);
  }

  public Tree remove(int index) {
    return parseTrees.remove(index);
  }

  public Tree set(int index, Tree element) {
    return parseTrees.set(index, element);
  }

  public ListIterator<Tree> listIterator() {
    return parseTrees.listIterator();
  }

  public ListIterator<Tree> listIterator(int index) {
    return parseTrees.listIterator(index);
  }

  public List<Tree> subList(int fromIndex, int toIndex) {
    return parseTrees.subList(fromIndex, toIndex);
  }

  /**
   * Return a MemoryTreebank where each
   * Tree in the current treebank has been transformed using the
   * TreeTransformer.  This Treebank is unchanged (assuming that the
   * TreeTransformer correctly doesn't change input Trees).
   *
   * @param treeTrans The TreeTransformer to use
   */
  @Override
  public Treebank transform(TreeTransformer treeTrans) {
    Treebank mtb = new MemoryTreebank(size(), treeReaderFactory());
    for (Tree t : this) {
      mtb.add(treeTrans.transformTree(t));
    }
    return mtb;
  }

  /**
   * Loads treebank grammar from first argument and prints it.
   * Just a demonstration of functionality. <br>
   * {@code usage: java MemoryTreebank treebankFilesPath}
   *
   * @param args array of command-line arguments
   */
  public static void main(String... args) {
    Timing.startTime();
    Treebank treebank = new MemoryTreebank(new TreeReaderFactory() {
      public TreeReader newTreeReader(Reader in) {
        return new PennTreeReader(in);
      }
    });
    treebank.loadPath(args[0]);
    Timing.endTime();
    System.out.println(treebank);
  }

}
