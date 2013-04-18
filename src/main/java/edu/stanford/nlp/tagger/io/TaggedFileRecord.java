package edu.stanford.nlp.tagger.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeTransformer;
import edu.stanford.nlp.trees.TreeNormalizer;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.ReflectionLoading;

public class TaggedFileRecord {
    private static final Pattern COMPILE = Pattern.compile(",");
    private static final Pattern PATTERN = Pattern.compile(";");
    private static final Pattern COMPILE1 = Pattern.compile(",");
    private static final Pattern COMPILE2 = Pattern.compile("=");

    public enum Format {
    TEXT,  // represents a tokenized file separated by text
    TSV,   // represents a tsv file such as a conll file
    TREES // represents a file in PTB format
  }

    final String file;
  final Format format;
  final String encoding;
  final String tagSeparator;
  final TreeTransformer treeTransformer;
  final TreeNormalizer treeNormalizer;
  final NumberRangesFileFilter treeRange;
  final Filter<Tree> treeFilter;
  final Integer wordColumn;
  final Integer tagColumn;

  private TaggedFileRecord(String file, Format format, 
                           String encoding, String tagSeparator,
                           TreeTransformer treeTransformer,
                           TreeNormalizer treeNormalizer,
                           NumberRangesFileFilter treeRange,
                           Filter<Tree> treeFilter,
                           Integer wordColumn, Integer tagColumn) {
    this.file = file;
    this.format = format;
    this.encoding = encoding;
    this.tagSeparator = tagSeparator;
    this.treeTransformer = treeTransformer;
    this.treeNormalizer = treeNormalizer;
    this.treeRange = treeRange;
    this.treeFilter = treeFilter;
    this.wordColumn = wordColumn;
    this.tagColumn = tagColumn;
  }

  static public final String FORMAT = "format";
  static public final String ENCODING = "encoding";
  static public final String TAG_SEPARATOR = "tagSeparator";
  static public final String TREE_TRANSFORMER = "treeTransformer";
  static public final String TREE_NORMALIZER = "treeNormalizer";
  static public final String TREE_RANGE = "treeRange";
  static public final String TREE_FILTER = "treeFilter";
  static public final String WORD_COLUMN = "wordColumn";
  static public final String TAG_COLUMN = "tagColumn";

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(FORMAT + '=').append(format);
    s.append(',' + ENCODING + '=').append(encoding);
    s.append(',' + TAG_SEPARATOR + '=').append(tagSeparator);
    if (treeTransformer != null) {
      s.append(',' + TREE_TRANSFORMER + '=').append(treeTransformer.getClass().getName());
    }
    if (treeNormalizer != null) {
      s.append(',' + TREE_NORMALIZER + '=').append(treeNormalizer.getClass().getName());
    }
    if (treeRange != null) {
      s.append(',' + TREE_RANGE + '=').append(COMPILE.matcher(treeRange.toString()).replaceAll(":"));
    }
    if (treeRange != null) {
      s.append(',' + TREE_FILTER + '=').append(treeFilter.getClass().toString());
    }
    if (wordColumn != null) {
      s.append(',' + WORD_COLUMN + '=').append(wordColumn);
    }
    if (tagColumn != null) {
      s.append(',' + TAG_COLUMN + '=').append(tagColumn);
    }
    return s.toString();
  }

  public String filename() { return file; }

  public TaggedFileReader reader() {
    switch(format) {
    case TEXT:
      return new TextTaggedFileReader(this);
    case TREES:
      return new TreeTaggedFileReader(this);
    case TSV:
      return new TSVTaggedFileReader(this);
    default:
      throw new IllegalArgumentException("Unknown format " + format);
    }
  }

  static public List<TaggedFileRecord> createRecords(Properties config,
                                                     String description) {
    String[] pieces = PATTERN.split(description);
    List<TaggedFileRecord> records = new ArrayList<>();
    for (String piece : pieces) {
      records.add(createRecord(config, piece));
    }
    return records;
  }

  static public TaggedFileRecord createRecord(Properties config,
                                              String description) {
    String[] pieces = COMPILE1.split(description);
    if (pieces.length == 1) {
      return new TaggedFileRecord(description, Format.TEXT,
                                  getEncoding(config),
                                  getTagSeparator(config),
                                  null, null, null, null, null, null);
    }

    String[] args = new String[pieces.length - 1];
    System.arraycopy(pieces, 0, args, 0, pieces.length - 1);
    String file = pieces[pieces.length - 1];
    Format format = Format.TEXT;
    String encoding = getEncoding(config);
    String tagSeparator = getTagSeparator(config);
    TreeTransformer treeTransformer = null;
    TreeNormalizer treeNormalizer = null;
    NumberRangesFileFilter treeRange = null;
    Filter<Tree> treeFilter = null;
    Integer wordColumn = null, tagColumn = null;

    for (String arg : args) {
      String[] argPieces = COMPILE2.split(arg, 2);
      if (argPieces.length != 2) {
        throw new IllegalArgumentException("TaggedFileRecord argument " + arg +
                                           " has an unexpected number of =s");
      }
      if (argPieces[0].equalsIgnoreCase(FORMAT)) {
        format = Format.valueOf(argPieces[1]);
      } else if (argPieces[0].equalsIgnoreCase(ENCODING)) {
        encoding = argPieces[1];
      } else if (argPieces[0].equalsIgnoreCase(TAG_SEPARATOR)) {
        tagSeparator = argPieces[1];
      } else if (argPieces[0].equalsIgnoreCase(TREE_TRANSFORMER)) {
        treeTransformer = ReflectionLoading.loadByReflection(argPieces[1]);
      } else if (argPieces[0].equalsIgnoreCase(TREE_NORMALIZER)) {
        treeNormalizer = ReflectionLoading.loadByReflection(argPieces[1]);
      } else if (argPieces[0].equalsIgnoreCase(TREE_RANGE)) {
        String range = argPieces[1].replace(':', ',');
        treeRange = new NumberRangesFileFilter(range, true);
      } else if (argPieces[0].equalsIgnoreCase(TREE_FILTER)) {
        treeFilter = ReflectionLoading.loadByReflection(argPieces[1]);
      } else if (argPieces[0].equalsIgnoreCase(WORD_COLUMN)) {
        wordColumn = Integer.valueOf(argPieces[1]);
      } else if (argPieces[0].equalsIgnoreCase(TAG_COLUMN)) {
        tagColumn = Integer.valueOf(argPieces[1]);
      } else {
        throw new IllegalArgumentException("TaggedFileRecord argument " + 
                                           argPieces[0] + " is unknown");
      }
    }
    return new TaggedFileRecord(file, format, encoding, tagSeparator,
                                treeTransformer, treeNormalizer, treeRange,
                                treeFilter, wordColumn, tagColumn);
  }

  static public String getEncoding(Properties config) {
    String encoding = config.getProperty(TaggerConfig.ENCODING_PROPERTY);
    if (encoding == null)
      return TaggerConfig.ENCODING;
    return encoding;
  }

  static public String getTagSeparator(Properties config) {
    String tagSeparator = 
      config.getProperty(TaggerConfig.TAG_SEPARATOR_PROPERTY);
    if (tagSeparator == null)
      return TaggerConfig.TAG_SEPARATOR;
    return tagSeparator;
  }
}
