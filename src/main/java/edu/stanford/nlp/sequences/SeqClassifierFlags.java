package edu.stanford.nlp.sequences;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.optimization.StochasticCalculateMethods;
import edu.stanford.nlp.process.WordShapeClassifier;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.ReflectionLoading;
import javolution.text.TxtBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Flags for sequence classifiers. Documentation for general flags and
 * flags for NER can be found in the Javadoc of
 * {@link edu.stanford.nlp.ie.NERFeatureFactory}. Documentation for the flags
 * for Chinese word segmentation can be found in the Javadoc of
 * {@link edu.stanford.nlp.wordseg.ChineseSegmenterFeatureFactory}.
 * <br>
 *
 * <i>IMPORTANT NOTE IF CHANGING THIS FILE:</i> <b>MAKE SURE</b> TO
 * ONLY ADD NEW VARIABLES AT THE END OF THE LIST OF VARIABLES (and not
 * to change existing variables)! Otherwise you usually break all
 * currently serialized classifiers!!! Search for "ADD VARIABLES ABOVE
 * HERE" below.
 * <br>
 * Some general flags are described here
 * <table border="1">
 * <tr>
 * <td><b>Property Name</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default Value</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td>useQN</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Use Quasi-Newton (L-BFGS) to find minimum. NOTE: Need to set this to
 * false if using other minimizers such as SGD.</td>
 * </tr>
 * <tr>
 * <td>QNsize</td>
 * <td>int</td>
 * <td>25</td>
 * <td>Number of previous iterations of Quasi-Newton to store (this increases
 * memory use, but speeds convergence by letting the Quasi-Newton optimization
 * more effectively approximate the second derivative).</td>
 * </tr>
 * <tr>
 * <td>QNsize2</td>
 * <td>int</td>
 * <td>25</td>
 * <td>Number of previous iterations of Quasi-Newton to store (used when pruning
 * features, after the first iteration - the first iteration is with QNSize).</td>
 * </tr>
 * <tr>
 * <td>useInPlaceSGD</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Use SGD (tweaking weights in place) to find minimum (more efficient than
 * the old SGD, faster to converge than Quasi-Newtown if there are very large of
 * samples). Implemented for CRFClassifier. NOTE: Remember to set useQN to false
 * </td>
 * </tr>
 * <tr>
 * <td>tuneSampleSize</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>If this number is greater than 0, specifies the number of samples to use
 * for tuning (default is 1000).</td>
 * </tr>
 * <tr>
 * <td>SGDPasses</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>If this number is greater than 0, specifies the number of SGD passes over
 * entire training set) to do before giving up (default is 50). Can be smaller
 * if sample size is very large.</td>
 * </tr>
 * <tr>
 * <td>useSGD</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Use SGD to find minimum (can be slow). NOTE: Remember to set useQN to
 * false</td>
 * </tr>
 * <tr>
 * <td>useSGDtoQN</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Use SGD (SGD version selected by useInPlaceSGD or useSGD) for a certain
 * number of passes (SGDPasses) and then switches to QN. Gives the quick initial
 * convergence of SGD, with the desired convergence criterion of QN (there is
 * some rampup time for QN). NOTE: Remember to set useQN to false</td>
 * </tr>
 * <tr>
 * <td>evaluateIters</td>
 * <td>int</td>
 * <td>0</td>
 * <td>If this number is greater than 0, evaluates on the test set every so
 * often while minimizing. Implemented for CRFClassifier.</td>
 * </tr>
 * <tr>
 * <td>evalCmd</td>
 * <td>String</td>
 * <td></td>
 * <td>If specified (and evaluateIters is set), runs the specified cmdline
 * command during evaluation (instead of default CONLL-like NER evaluation)</td>
 * </tr>
 * <tr>
 * <td>evaluateTrain</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If specified (and evaluateIters is set), also evaluate on training set
 * (can be expensive)</td>
 * </tr>
 * <tr>
 * <td>tokenizerOptions</td></td>String</td>
 * <td>(null)</td>
 * <td>Extra options to supply to the tokenizer when creating it.</td>
 * </tr>
 * <tr>
 * <td>tokenizerFactory</td></td>String</td>
 * <td>(null)</td>
 * <td>A different tokenizer factory to use if the ReaderAndWriter in question uses tokenizers.</td>
 * </tr>
 * </table>
 *
 * @author Jenny Finkel
 */
public class SeqClassifierFlags implements Serializable {

  private static final long serialVersionUID = -7076671761070232567L;

  public static final String DEFAULT_BACKGROUND_SYMBOL = "O";
    private static final Pattern COMPILE = Pattern.compile("[, ]+");

    private String stringRep = "";

  public boolean useNGrams;
  public boolean conjoinShapeNGrams;
  public boolean lowercaseNGrams;
  public boolean dehyphenateNGrams;
  public boolean usePrev;
  public boolean useNext;
  public boolean useTags;
  public boolean useWordPairs;
  public boolean useGazettes;
  public boolean useSequences = true;
  public boolean usePrevSequences;
  public boolean useNextSequences;
  public boolean useLongSequences;
  public boolean useBoundarySequences;
  public boolean useTaggySequences;
  public boolean useExtraTaggySequences;
  public boolean dontExtendTaggy;
  public boolean useTaggySequencesShapeInteraction;
  public boolean strictlyZeroethOrder;
  public boolean strictlyFirstOrder;
  public boolean strictlySecondOrder;
  public boolean strictlyThirdOrder;
  public String entitySubclassification = "IO";
  public boolean retainEntitySubclassification;
  public boolean useGazettePhrases;
  public boolean makeConsistent;
  public boolean useWordLabelCounts;
  // boolean usePrevInstanceLabel = false;
  // boolean useNextInstanceLabel = false;
  public boolean useViterbi = true;

  public int[] binnedLengths;

  public boolean verboseMode;

  public boolean useSum;
  public double tolerance = 1.0e-4;
  // Turned on if non-null. Becomes part of the filename features are printed to
  public String printFeatures;

  public boolean useSymTags;
  /**
   * useSymWordPairs Has a small negative effect.
   */
  public boolean useSymWordPairs;

  public String printClassifier = "WeightHistogram";
  public int printClassifierParam = 100;

  public boolean intern;
  public boolean intern2;
  public boolean selfTest;

  public boolean sloppyGazette;
  public boolean cleanGazette;

  public boolean noMidNGrams;
  public int maxNGramLeng = -1;
  public boolean useReverse;

  public boolean greekifyNGrams;

  public boolean useParenMatching;

  public boolean useLemmas;
  public boolean usePrevNextLemmas;
  public boolean normalizeTerms;
  public boolean normalizeTimex;

  public boolean useNB;
  public boolean useQN = true;
  public boolean useFloat;

  public int QNsize = 25;
  public int QNsize2 = 25;
  public int maxIterations = -1;

  public int wordShape = WordShapeClassifier.NOWORDSHAPE;
  public boolean useShapeStrings;
  public boolean useTypeSeqs;
  public boolean useTypeSeqs2;
  public boolean useTypeSeqs3;
  public boolean useDisjunctive;
  public int disjunctionWidth = 4;
  public boolean useDisjunctiveShapeInteraction;
  public boolean useDisjShape;

  public boolean useWord = true; // ON by default
  public boolean useClassFeature;
  public boolean useShapeConjunctions;
  public boolean useWordTag;
  public boolean useNPHead;
  public boolean useNPGovernor;
  public boolean useHeadGov;

  public boolean useLastRealWord;
  public boolean useNextRealWord;
  public boolean useOccurrencePatterns;
  public boolean useTypeySequences;

  public boolean justify;

  public boolean normalize;

  public String priorType = "QUADRATIC";
  public double sigma = 1.0;
  public double epsilon = 0.01;

  public int beamSize = 30;

  public int maxLeft = 2;
  public int maxRight;

  public boolean usePosition;
  public boolean useBeginSent;
  public boolean useGazFeatures;
  public boolean useMoreGazFeatures;
  public boolean useAbbr;
  public boolean useMinimalAbbr;
  public boolean useAbbr1;
  public boolean useMinimalAbbr1;
  public boolean useMoreAbbr;

  public boolean deleteBlankLines;

  public boolean useGENIA;
  public boolean useTOK;
  public boolean useABSTR;
  public boolean useABSTRFreqDict;
  public boolean useABSTRFreq;
  public boolean useFREQ;
  public boolean useABGENE;
  public boolean useWEB;
  public boolean useWEBFreqDict;
  public boolean useIsURL;
  public boolean useURLSequences;
  public boolean useIsDateRange;
  public boolean useEntityTypes;
  public boolean useEntityTypeSequences;
  public boolean useEntityRule;
  public boolean useOrdinal;
  public boolean useACR;
  public boolean useANTE;

  public boolean useMoreTags;

  public boolean useChunks;
  public boolean useChunkySequences;

  public boolean usePrevVB;
  public boolean useNextVB;
  public boolean useVB;
  public boolean subCWGaz;

  public String documentReader = "ColumnDocumentReader"; // TODO OBSOLETE:
  // delete when breaking
  // serialization
  // sometime.

  // public String trainMap = "word=0,tag=1,answer=2";
  // public String testMap = "word=0,tag=1,answer=2";
  public String map = "word=0,tag=1,answer=2";

  public boolean useWideDisjunctive;
  public int wideDisjunctionWidth = 10;

  // chinese word-segmenter features
  public boolean useRadical;
  public boolean useBigramInTwoClique;
  public String morphFeatureFile;
  public boolean useReverseAffix;
  public int charHalfWindow = 3;
  public boolean useWord1;
  public boolean useWord2;
  public boolean useWord3;
  public boolean useWord4;
  public boolean useRad1;
  public boolean useRad2;
  public boolean useWordn;
  public boolean useCTBPre1;
  public boolean useCTBSuf1;
  public boolean useASBCPre1;
  public boolean useASBCSuf1;
  public boolean usePKPre1;
  public boolean usePKSuf1;
  public boolean useHKPre1;
  public boolean useHKSuf1;
  public boolean useCTBChar2;
  public boolean useASBCChar2;
  public boolean useHKChar2;
  public boolean usePKChar2;
  public boolean useRule2;
  public boolean useDict2;
  public boolean useOutDict2;
  public String outDict2 = "/u/htseng/scr/chunking/segmentation/out.lexicon";
  public boolean useDictleng;
  public boolean useDictCTB2;
  public boolean useDictASBC2;
  public boolean useDictPK2;
  public boolean useDictHK2;
  public boolean useBig5;
  public boolean useNegDict2;
  public boolean useNegDict3;
  public boolean useNegDict4;
  public boolean useNegCTBDict2;
  public boolean useNegCTBDict3;
  public boolean useNegCTBDict4;
  public boolean useNegASBCDict2;
  public boolean useNegASBCDict3;
  public boolean useNegASBCDict4;
  public boolean useNegHKDict2;
  public boolean useNegHKDict3;
  public boolean useNegHKDict4;
  public boolean useNegPKDict2;
  public boolean useNegPKDict3;
  public boolean useNegPKDict4;
  public boolean usePre;
  public boolean useSuf;
  public boolean useRule;
  public boolean useHk;
  public boolean useMsr;
  public boolean useMSRChar2;
  public boolean usePk;
  public boolean useAs;
  public boolean useFilter; // TODO this flag is used for nothing;
  // delete when breaking serialization
  public boolean largeChSegFile; // TODO this flag is used for nothing;
  // delete when breaking serialization
  public boolean useRad2b;

  /**
   * Keep the whitespace between English words in testFile when printing out
   * answers. Doesn't really change the content of the CoreLabels. (For Chinese
   * segmentation.)
   */
  public boolean keepEnglishWhitespaces;

  /**
   * Keep all the whitespace words in testFile when printing out answers.
   * Doesn't really change the content of the CoreLabels. (For Chinese
   * segmentation.)
   */
  public boolean keepAllWhitespaces;

  public boolean sighanPostProcessing;

  /**
   * use POS information (an "open" feature for Chinese segmentation)
   */
  public boolean useChPos;

  // CTBSegDocumentReader normalization table
  // A value of null means that a default algorithmic normalization
  // is done in which ASCII characters get mapped to their fullwidth
  // equivalents in the Unihan range
  public String normalizationTable; // = null;
  public String dictionary; // = null;
  public String serializedDictionary; // = null;
  public String dictionary2; // = null;
  public String normTableEncoding = "GB18030";

  /**
   * for Sighan bakeoff 2005, the path to the dictionary of bigrams appeared in
   * corpus
   */
  public String sighanCorporaDict = "/u/nlp/data/chinese-segmenter/";

  // end Sighan 20005 chinese word-segmenter features/properties

  public boolean useWordShapeGaz;
  public String wordShapeGaz;

  // TODO: This should be removed in favor of suppressing splitting when
  // maxDocSize <= 0, when next breaking serialization
  // this now controls nothing
  public boolean splitDocuments = true;

  public boolean printXML;

  public boolean useSeenFeaturesOnly;

  public String lastNameList = "/u/nlp/data/dist.all.last";
  public String maleNameList = "/u/nlp/data/dist.male.first";
  public String femaleNameList = "/u/nlp/data/dist.female.first";

  // don't want these serialized
  public transient String trainFile;
  /** NER adaptation (Gaussian prior) parameters. */
  public transient String adaptFile;
  public transient String devFile;
  public transient String testFile;
  public transient String textFile;
  public transient String textFiles;
  public transient boolean readStdin;
  public transient String outputFile;
  public transient String loadClassifier;
  public transient String loadTextClassifier;
  public transient String loadJarClassifier;
  public transient String loadAuxClassifier;
  public transient String serializeTo;
  public transient String serializeToText;
  public transient int interimOutputFreq;
  public transient String initialWeights;
  public transient List<String> gazettes = new ArrayList<>();
  public transient String selfTrainFile;

  public String inputEncoding = "UTF-8"; // used for CTBSegDocumentReader as
  // well

  public boolean bioSubmitOutput;
  public int numRuns = 1;
  public String answerFile;
  public String altAnswerFile;
  public String dropGaz;
  public String printGazFeatures;
  public int numStartLayers = 1;
  public boolean dump;
  public boolean mergeTags; // whether to merge B- and I- tags
  public boolean splitOnHead;

  // threshold
  public int featureCountThreshold;
  public double featureWeightThreshold;

  // feature factory
  public String featureFactory = "edu.stanford.nlp.ie.NERFeatureFactory";
  public Object[] featureFactoryArgs = new Object[0];

  public String backgroundSymbol = DEFAULT_BACKGROUND_SYMBOL;
  // use
  public boolean useObservedSequencesOnly;

  public int maxDocSize;
  public boolean printProbs;
  public boolean printFirstOrderProbs;

  public boolean saveFeatureIndexToDisk;
  public boolean removeBackgroundSingletonFeatures;
  public boolean doGibbs;
  public int numSamples = 100;
  public boolean useNERPrior;
  public boolean useAcqPrior;
  /**
   * If true and doGibbs also true, will do generic Gibbs inference without any
   * priors
   */
  public boolean useUniformPrior;
  public boolean useMUCFeatures;
  public double annealingRate;
  public String annealingType;
  public String loadProcessedData;

  public boolean initViterbi = true;

  public boolean useUnknown;

  public boolean checkNameList;

  public boolean useSemPrior;
  public boolean useFirstWord;

  public boolean useNumberFeature;

  public int ocrFold;
  public transient boolean ocrTrain;

  public String classifierType = "MaxEnt";
  public String svmModelFile;

  public String inferenceType = "Viterbi";

  public boolean useLemmaAsWord;

  public String type = "cmm";

  public String readerAndWriter = "edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter";

  public List<String> comboProps = new ArrayList<>();

  public boolean usePrediction;

  public boolean useAltGazFeatures;

  public String gazFilesFile;

  public boolean usePrediction2;
  public String baseTrainDir = ".";
  public String baseTestDir = ".";
  public String trainFiles;
  public String trainFileList;
  public String testFiles;
  public String trainDirs; // cdm 2009: this is currently unsupported,
  // but one user wanted something like this....
  public String testDirs;

  public boolean useOnlySeenWeights;

  public String predProp;

  public CoreLabel pad = new CoreLabel();

  public boolean useObservedFeaturesOnly;

  public String distSimLexicon;
  public boolean useDistSim;

  public int removeTopN;
  public int numTimesRemoveTopN = 1;
  public double randomizedRatio = 1.0;

  public double removeTopNPercent;
  public int purgeFeatures = -1;

  public boolean booleanFeatures;

  public boolean iobWrapper;
  public boolean iobTags;
  public boolean useSegmentation; /*
                                           * binary segmentation feature for
                                           * character-based Chinese NER
                                           */

  public boolean memoryThrift;
  public boolean timitDatum;

  public String serializeDatasetsDir;
  public String loadDatasetsDir;
  public String pushDir;
  public boolean purgeDatasets;
  public boolean keepOBInMemory = true;
  public boolean fakeDataset;
  public boolean restrictTransitionsTimit;
  public int numDatasetsPerFile = 1;
  public boolean useTitle;

  // these are for the old stuff
  public boolean lowerNewgeneThreshold;
  public boolean useEitherSideWord;
  public boolean useEitherSideDisjunctive;
  public boolean twoStage;
  public String crfType = "MaxEnt";
  public int featureThreshold = 1;
  public String featThreshFile;
  public double featureDiffThresh;
  public int numTimesPruneFeatures;
  public double newgeneThreshold;
  public boolean doAdaptation;
  public boolean useInternal = true;
  public boolean useExternal = true;
  public double selfTrainConfidenceThreshold = 0.9;
  public int selfTrainIterations = 1;
  public int selfTrainWindowSize = 1; // Unigram
  public boolean useHuber;
  public boolean useQuartic;
  public double adaptSigma = 1.0;
  public int numFolds = 1;
  public int startFold = 1;
  public int endFold = 1;

  public boolean cacheNGrams;

  public String outputFormat;

  public boolean useSMD;
  public boolean useSGDtoQN;
  public boolean useStochasticQN;
  public boolean useScaledSGD;
  public int scaledSGDMethod;
  public int SGDPasses = -1;
  public int QNPasses = -1;
  public boolean tuneSGD;
  public StochasticCalculateMethods stochasticMethod = StochasticCalculateMethods.NoneSpecified;
  public double initialGain = 0.1;
  public int stochasticBatchSize = 15;
  public boolean useSGD;
  public double gainSGD = 0.1;
  public boolean useHybrid;
  public int hybridCutoffIteration;
  public boolean outputIterationsToFile;
  public boolean testObjFunction;
  public boolean testVariance;
  public int SGD2QNhessSamples = 50;
  public boolean testHessSamples;
  public int CRForder = 1;  // TODO remove this when breaking serialization; this is unused; really maxLeft/maxRight control order
  public int CRFwindow = 2;  // TODO remove this when breaking serialization; this is unused; really maxLeft/maxRight control clique size
  public boolean estimateInitial;

  public transient String biasedTrainFile;
  public transient String confusionMatrix;

  public String outputEncoding;

  public boolean useKBest;
  public String searchGraphPrefix;
  public double searchGraphPrune = Double.POSITIVE_INFINITY;
  public int kBest = 1;

  // more chinese segmenter features for GALE 2007
  public boolean useFeaturesC4gram;
  public boolean useFeaturesC5gram;
  public boolean useFeaturesC6gram;
  public boolean useFeaturesCpC4gram;
  public boolean useFeaturesCpC5gram;
  public boolean useFeaturesCpC6gram;
  public boolean useUnicodeType;
  public boolean useUnicodeType4gram;
  public boolean useUnicodeType5gram;
  public boolean use4Clique;
  public boolean useUnicodeBlock;
  public boolean useShapeStrings1;
  public boolean useShapeStrings3;
  public boolean useShapeStrings4;
  public boolean useShapeStrings5;
  public boolean useGoodForNamesCpC;
  public boolean useDictionaryConjunctions;
  public boolean expandMidDot;

  public int printFeaturesUpto; // = 0;

  public boolean useDictionaryConjunctions3;
  public boolean useWordUTypeConjunctions2;
  public boolean useWordUTypeConjunctions3;
  public boolean useWordShapeConjunctions2;
  public boolean useWordShapeConjunctions3;
  public boolean useMidDotShape;
  public boolean augmentedDateChars;
  public boolean suppressMidDotPostprocessing;

  public boolean printNR; // a flag for WordAndTagDocumentReaderAndWriter

  public String classBias;

  public boolean printLabelValue; // Old printErrorStuff

  public boolean useRobustQN;
  public boolean combo;

  public boolean useGenericFeatures;

  public boolean verboseForTrueCasing;

  public String trainHierarchical;
  public String domain;
  public boolean baseline;
  public String transferSigmas;
  public boolean doFE;
  public boolean restrictLabels = true;

  public boolean announceObjectBankEntries; // whether to print a line
  // giving each ObjectBank
  // entry (usually a
  // filename)

  // Arabic Subject Detector flags
  public boolean usePos;
  public boolean useAgreement;
  public boolean useAccCase;
  public boolean useInna;
  public boolean useConcord;
  public boolean useFirstNgram;
  public boolean useLastNgram;
  public boolean collapseNN;
  public boolean useConjBreak;
  public boolean useAuxPairs;
  public boolean usePPVBPairs;
  public boolean useAnnexing;
  public boolean useTemporalNN;
  public boolean usePath;
  public boolean innaPPAttach;
  public boolean markProperNN;
  public boolean markMasdar;
  public boolean useSVO;

  public int numTags = 3;
  public boolean useTagsCpC;
  public boolean useTagsCpCp2C;
  public boolean useTagsCpCp2Cp3C;
  public boolean useTagsCpCp2Cp3Cp4C;

  public double l1reg;

  // truecaser flags:
  public String mixedCaseMapFile = "";
  public String auxTrueCaseModels = "";

  // more flags inspired by Zhang and Johnson 2003
  public boolean use2W;
  public boolean useLC;
  public boolean useYetMoreCpCShapes;

  // added for the NFL domain
  public boolean useIfInteger;

  public String exportFeatures;
  public boolean useInPlaceSGD;
  public boolean useTopics;

  // Number of iterations before evaluating weights (0 = don't evaluate)
  public int evaluateIters;
  // Command to use for evaluation
  public String evalCmd = "";
  // Evaluate on training set or not
  public boolean evaluateTrain;
  public int tuneSampleSize = -1;

  public boolean usePhraseFeatures;
  public boolean usePhraseWords;
  public boolean usePhraseWordTags;
  public boolean usePhraseWordSpecialTags;
  public boolean useCommonWordsFeature;
  public boolean useProtoFeatures;
  public boolean useWordnetFeatures;
  public String tokenFactory = "edu.stanford.nlp.process.CoreLabelTokenFactory";
  public Object[] tokenFactoryArgs = new Object[0];
  public String tokensAnnotationClassName = "edu.stanford.nlp.ling.CoreAnnotations$TokensAnnotation";

  public transient String tokenizerOptions;
  public transient String tokenizerFactory;

  public boolean useCorefFeatures;
  public String wikiFeatureDbFile;
  // for combining 2 CRFs - one trained from noisy data and another trained from
  // non-noisy
  public boolean useNoisyNonNoisyFeature;
  // year annotation of the document
  public boolean useYear;

  public boolean useSentenceNumber;
  // to know source of the label. Currently, used to know which pattern is used
  // to label the token
  public boolean useLabelSource;

  /**
   * Whether to (not) lowercase tokens before looking them up in distsim
   * lexicon. By default lowercasing was done, but now it doesn't have to be
   * true :-).
   */
  public boolean casedDistSim;

  /**
   * The format of the distsim file. Known values are: alexClark = TSV file.
   * word TAB clusterNumber [optional other content] terryKoo = TSV file.
   * clusterBitString TAB word TAB frequency
   */
  public String distSimFileFormat = "alexClark";

  /**
   * If this number is greater than 0, the distSim class is assume to be a bit
   * string and is truncated at this many characters. Normal distSim features
   * will then use this amount of resolution. Extra, special distsim features
   * may work at a coarser level of resolution. Since the lexicon only stores
   * this length of bit string, there is then no way to have finer-grained
   * clusters.
   */
  public int distSimMaxBits = 8;

  /**
   * If this is set to true, all digit characters get mapped to '9' in a distsim
   * lexicon and for lookup. This is a simple word shaping that can shrink
   * distsim lexicons and improve their performance.
   */
  public boolean numberEquivalenceDistSim;

  /**
   * What class to assign to words not found in the dist sim lexicon. You might
   * want to make it a known class, if one is the "default class.
   */
  public String unknownWordDistSimClass = "null";

  /**
   * Use prefixes and suffixes from the previous and next word.
   */
  public boolean useNeighborNGrams;

  /**
   * This function maps words in the training or test data to new
   * words.  They are used at the feature extractor level, ie in the
   * FeatureFactory.  For now, only the NERFeatureFactory uses this.
   */
  public Function<String, String> wordFunction;

  public static final String DEFAULT_PLAIN_TEXT_READER = "edu.stanford.nlp.sequences.PlainTextDocumentReaderAndWriter";
  public String plainTextDocumentReaderAndWriter = DEFAULT_PLAIN_TEXT_READER;

  /**
   * Use a bag of all words as a feature.  Perhaps this will find some
   * words that indicate certain types of entities are present.
   */
  public boolean useBagOfWords;

  /**
   * When scoring, count the background symbol stats too.  Useful for
   * things where the background symbol is particularly meaningful,
   * such as truecase.
   */
  public boolean evaluateBackground;

  /**
   * Number of experts to be used in Logarithmic Opinion Pool (product of experts) training
   * default value is 1
   */
  public int numLopExpert = 1;
  public transient String initialLopScales;
  public transient String initialLopWeights;
  public boolean includeFullCRFInLOP;
  public boolean backpropLopTraining;
  public boolean randomLopWeights;
  public boolean randomLopFeatureSplit;
  public boolean nonLinearCRF;
  public boolean secondOrderNonLinear;
  public int numHiddenUnits = -1;
  public boolean useOutputLayer = true;
  public boolean useHiddenLayer = true;
  public boolean gradientDebug;
  public boolean checkGradient;
  public boolean useSigmoid;
  public boolean skipOutputRegularization;
  public boolean sparseOutputLayer;
  public boolean tieOutputLayer;
  public boolean blockInitialize;
  public boolean softmaxOutputLayer;


  /**
   * Bisequence CRF parameters
   */
  public String loadBisequenceClassifierEn;
  public String loadBisequenceClassifierCh;
  public String bisequenceClassifierPropEn;
  public String bisequenceClassifierPropCh;
  public String bisequenceTestFileEn;
  public String bisequenceTestFileCh;
  public String bisequenceTestOutputEn;
  public String bisequenceTestOutputCh;
  public String bisequenceTestAlignmentFile;
  public int bisequencePriorType = 1;
  public String bisequenceAlignmentPriorPenaltyCh;
  public String bisequenceAlignmentPriorPenaltyEn;
  public double alignmentPruneThreshold;
  public boolean factorInAlignmentProb;
  public boolean useChromaticSampling;
  public boolean useSequentialScanSampling;
  public int maxAllowedChromaticSize = 8;

  /** Whether to drop out some fraction of features in the input during
   *  training (and then to scale the weights at test time).
   */
  public double inputDropOut;

  /**
   * Whether or not to keep blank sentences when processing.  Useful
   * for systems such as the segmenter if you want to line up each
   * line exactly, including blank lines.
   */
  public boolean keepEmptySentences;
  public boolean useBilingualNERPrior;

  public int samplingSpeedUpThreshold = -1;
  public String entityMatrixCh;
  public String entityMatrixEn;

  public int multiThreadGibbs;
  public boolean matchNERIncentive;

  public boolean useEmbedding;
  public boolean prependEmbedding;
  public String embeddingWords;
  public String embeddingVectors;
  public boolean transitionEdgeOnly;
  public double priorL1Lambda;
  public boolean addCapitalFeatures;
  public int arbitraryInputLayerSize = -1;
  public boolean noEdgeFeature;
  public boolean terminateOnEvalImprovement;
  public int terminateOnEvalImprovementNumOfEpoch = 1;
  public boolean useMemoryEvaluator = true;
  public boolean suppressTestDebug;
  public boolean useOWLQN;
  public boolean printWeights;
  public int totalDataSlice = 10;
  public int numOfSlices;
  public boolean regularizeSoftmaxTieParam;
  public double softmaxTieLambda;
  public int totalFeatureSlice = 10;
  public int numOfFeatureSlices;
  public boolean addBiasToEmbedding;
  public boolean hardcodeSoftmaxOutputWeights;

  public boolean useNERPriorBIO;
  public String entityMatrix;
  public int multiThreadClassifier;
  
  public String splitWordRegex;
  
  // "ADD VARIABLES ABOVE HERE"

  public transient List<String> phraseGazettes;
  public transient Properties props;

  public SeqClassifierFlags() {
  }

  /**
   * Create a new SeqClassifierFlags object and initialize it using values in
   * the Properties object. The properties are printed to stderr as it works.
   *
   * @param props The properties object used for initialization
   */
  public SeqClassifierFlags(Properties props) {
    setProperties(props, true);
  }

  /**
   * Initialize this object using values in Properties object. The properties
   * are printed to stderr as it works.
   *
   * @param props
   *          The properties object used for initialization
   */
  public final void setProperties(Properties props) {
    setProperties(props, true);
  }

  /**
   * Initialize using values in Properties file.
   *
   * @param props
   *          The properties object used for initialization
   * @param printProps
   *          Whether to print the properties to stderr as it works.
   */
  public void setProperties(Properties props, boolean printProps) {
    this.props = props;
    TxtBuilder sb = new TxtBuilder(stringRep);
    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String val = props.getProperty(key);
      if (!(key.isEmpty() && val.isEmpty())) {
        if (printProps) {
          System.err.println(key + '=' + val);
        }
        sb.append(key).append('=').append(val).append('\n');
      }
      if (key.equalsIgnoreCase("macro")) {
        if (Boolean.parseBoolean(val)) {
          useObservedSequencesOnly = true;
          readerAndWriter = "edu.stanford.nlp.sequences.CoNLLDocumentReaderAndWriter";
          // useClassFeature = true;
          // submit
          useLongSequences = true;
          useTaggySequences = true;
          useNGrams = true;
          usePrev = true;
          useNext = true;
          useTags = true;
          useWordPairs = true;
          useSequences = true;
          usePrevSequences = true;
          // noMidNGrams
          noMidNGrams = true;
          // reverse
          useReverse = true;
          // typeseqs3
          useTypeSeqs = true;
          useTypeSeqs2 = true;
          useTypeySequences = true;
          // wordtypes2 && known
          wordShape = WordShapeClassifier.WORDSHAPEDAN2USELC;
          // occurrence
          useOccurrencePatterns = true;
          // realword
          useLastRealWord = true;
          useNextRealWord = true;
          // smooth
          sigma = 3.0;
          // normalize
          normalize = true;
          normalizeTimex = true;
        }
      } else if (key.equalsIgnoreCase("goodCoNLL")) {
        if (Boolean.parseBoolean(val)) {
          // featureFactory = "edu.stanford.nlp.ie.NERFeatureFactory";
          readerAndWriter = "edu.stanford.nlp.sequences.CoNLLDocumentReaderAndWriter";
          useObservedSequencesOnly = true;
          // useClassFeature = true;
          useLongSequences = true;
          useTaggySequences = true;
          useNGrams = true;
          usePrev = true;
          useNext = true;
          useTags = true;
          useWordPairs = true;
          useSequences = true;
          usePrevSequences = true;
          // noMidNGrams
          noMidNGrams = true;
          // should this be set?? maxNGramLeng = 6; No (to get best score).
          // reverse
          useReverse = false;
          // typeseqs3
          useTypeSeqs = true;
          useTypeSeqs2 = true;
          useTypeySequences = true;
          // wordtypes2 && known
          wordShape = WordShapeClassifier.WORDSHAPEDAN2USELC;
          // occurrence
          useOccurrencePatterns = true;
          // realword
          useLastRealWord = true;
          useNextRealWord = true;
          // smooth
          sigma = 50.0; // increased Aug 2006 from 20; helpful with less feats
          // normalize
          normalize = true;
          normalizeTimex = true;
          maxLeft = 2;
          useDisjunctive = true;
          disjunctionWidth = 4; // clearly optimal for CoNLL
          useBoundarySequences = true;
          useLemmas = true; // no-op except for German
          usePrevNextLemmas = true; // no-op except for German
          inputEncoding = "iso-8859-1"; // needed for CoNLL German files
          // opt
          useQN = true;
          QNsize = 15;
        }
      } else if (key.equalsIgnoreCase("conllNoTags")) {
        if (Boolean.parseBoolean(val)) {
          readerAndWriter = "edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter";
          // trainMap=testMap="word=0,answer=1";
          map = "word=0,answer=1";
          useObservedSequencesOnly = true;
          // useClassFeature = true;
          useLongSequences = true;
          // useTaggySequences = true;
          useNGrams = true;
          usePrev = true;
          useNext = true;
          // useTags = true;
          useWordPairs = true;
          useSequences = true;
          usePrevSequences = true;
          // noMidNGrams
          noMidNGrams = true;
          // reverse
          useReverse = false;
          // typeseqs3
          useTypeSeqs = true;
          useTypeSeqs2 = true;
          useTypeySequences = true;
          // wordtypes2 && known
          wordShape = WordShapeClassifier.WORDSHAPEDAN2USELC;
          // occurrence
          // useOccurrencePatterns = true;
          // realword
          useLastRealWord = true;
          useNextRealWord = true;
          // smooth
          sigma = 20.0;
          adaptSigma = 20.0;
          // normalize
          normalize = true;
          normalizeTimex = true;
          maxLeft = 2;
          useDisjunctive = true;
          disjunctionWidth = 4;
          useBoundarySequences = true;
          // useLemmas = true; // no-op except for German
          // usePrevNextLemmas = true; // no-op except for German
          inputEncoding = "iso-8859-1";
          // opt
          useQN = true;
          QNsize = 15;
        }
      } else if (key.equalsIgnoreCase("notags")) {
        if (Boolean.parseBoolean(val)) {
          // turn off all features that use POS tags
          // this is slightly crude: it also turns off a few things that
          // don't use tags in e.g., useTaggySequences
          useTags = false;
          useSymTags = false;
          useTaggySequences = false;
          useOccurrencePatterns = false;
        }
      } else if (key.equalsIgnoreCase("submit")) {
        if (Boolean.parseBoolean(val)) {
          useLongSequences = true;
          useTaggySequences = true;
          useNGrams = true;
          usePrev = true;
          useNext = true;
          useTags = true;
          useWordPairs = true;
          wordShape = WordShapeClassifier.WORDSHAPEDAN1;
          useSequences = true;
          usePrevSequences = true;
        }
      } else if (key.equalsIgnoreCase("binnedLengths")) {
        if (val != null) {
          String[] binnedLengthStrs = COMPILE.split(val);
          binnedLengths = new int[binnedLengthStrs.length];
          for (int i = 0; i < binnedLengths.length; i++) {
            binnedLengths[i] = Integer.parseInt(binnedLengthStrs[i]);
          }
        }
      } else if (key.equalsIgnoreCase("makeConsistent")) {
        makeConsistent = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("dump")) {
        dump = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNGrams")) {
        useNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNeighborNGrams")) {
        useNeighborNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("wordFunction")) {
        wordFunction = ReflectionLoading.loadByReflection(val);
      } else if (key.equalsIgnoreCase("conjoinShapeNGrams")) {
        conjoinShapeNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("lowercaseNGrams")) {
        lowercaseNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useIsURL")) {
        useIsURL = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useURLSequences")) {
        useURLSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEntityTypes")) {
        useEntityTypes = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEntityRule")) {
        useEntityRule = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useOrdinal")) {
        useOrdinal = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEntityTypeSequences")) {
        useEntityTypeSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useIsDateRange")) {
        useIsDateRange = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("dehyphenateNGrams")) {
        dehyphenateNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("lowerNewgeneThreshold")) {
        lowerNewgeneThreshold = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePrev")) {
        usePrev = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNext")) {
        useNext = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTags")) {
        useTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordPairs")) {
        useWordPairs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useGazettes")) {
        useGazettes = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("wordShape")) {
        wordShape = WordShapeClassifier.lookupShaper(val);
      } else if (key.equalsIgnoreCase("useShapeStrings")) {
        useShapeStrings = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useGoodForNamesCpC")) {
        useGoodForNamesCpC = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDictionaryConjunctions")) {
        useDictionaryConjunctions = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDictionaryConjunctions3")) {
        useDictionaryConjunctions3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("expandMidDot")) {
        expandMidDot = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSequences")) {
        useSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePrevSequences")) {
        usePrevSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNextSequences")) {
        useNextSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLongSequences")) {
        useLongSequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useBoundarySequences")) {
        useBoundarySequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTaggySequences")) {
        useTaggySequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useExtraTaggySequences")) {
        useExtraTaggySequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTaggySequencesShapeInteraction")) {
        useTaggySequencesShapeInteraction = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("strictlyZeroethOrder")) {
        strictlyZeroethOrder = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("strictlyFirstOrder")) {
        strictlyFirstOrder = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("strictlySecondOrder")) {
        strictlySecondOrder = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("strictlyThirdOrder")) {
        strictlyThirdOrder = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("dontExtendTaggy")) {
        dontExtendTaggy = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("entitySubclassification")) {
        entitySubclassification = val;
      } else if (key.equalsIgnoreCase("useGazettePhrases")) {
        useGazettePhrases = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("phraseGazettes")) {
        StringTokenizer st = new StringTokenizer(val, " ,;\t");
        if (phraseGazettes == null) {
          phraseGazettes = new ArrayList<>();
        }
        while (st.hasMoreTokens()) {
          phraseGazettes.add(st.nextToken());
        }
      } else if (key.equalsIgnoreCase("useSum")) {
        useSum = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("verboseMode")) {
        verboseMode = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("tolerance")) {
        tolerance = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("maxIterations")) {
        maxIterations = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("exportFeatures")) {
        exportFeatures = val;
      } else if (key.equalsIgnoreCase("printFeatures")) {
        printFeatures = val;
      } else if (key.equalsIgnoreCase("printFeaturesUpto")) {
        printFeaturesUpto = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("lastNameList")) {
        lastNameList = val;
      } else if (key.equalsIgnoreCase("maleNameList")) {
        maleNameList = val;
      } else if (key.equalsIgnoreCase("femaleNameList")) {
        femaleNameList = val;
      } else if (key.equalsIgnoreCase("useSymTags")) {
        useSymTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSymWordPairs")) {
        useSymWordPairs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("printClassifier")) {
        printClassifier = val;
      } else if (key.equalsIgnoreCase("printClassifierParam")) {
        printClassifierParam = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("intern")) {
        intern = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("mergetags")) {
        mergeTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("iobtags")) {
        iobTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useViterbi")) {
        useViterbi = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("intern2")) {
        intern2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("selfTest")) {
        selfTest = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("sloppyGazette")) {
        sloppyGazette = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("cleanGazette")) {
        cleanGazette = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("noMidNGrams")) {
        noMidNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useReverse")) {
        useReverse = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("retainEntitySubclassification")) {
        retainEntitySubclassification = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLemmas")) {
        useLemmas = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePrevNextLemmas")) {
        usePrevNextLemmas = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("normalizeTerms")) {
        normalizeTerms = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("normalizeTimex")) {
        normalizeTimex = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNB")) {
        useNB = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useParenMatching")) {
        useParenMatching = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTypeSeqs")) {
        useTypeSeqs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTypeSeqs2")) {
        useTypeSeqs2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTypeSeqs3")) {
        useTypeSeqs3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDisjunctive")) {
        useDisjunctive = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("disjunctionWidth")) {
        disjunctionWidth = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useDisjunctiveShapeInteraction")) {
        useDisjunctiveShapeInteraction = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWideDisjunctive")) {
        useWideDisjunctive = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("wideDisjunctionWidth")) {
        wideDisjunctionWidth = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useDisjShape")) {
        useDisjShape = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTitle")) {
        useTitle = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("booleanFeatures")) {
        booleanFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useClassFeature")) {
        useClassFeature = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useShapeConjunctions")) {
        useShapeConjunctions = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordTag")) {
        useWordTag = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNPHead")) {
        useNPHead = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNPGovernor")) {
        useNPGovernor = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHeadGov")) {
        useHeadGov = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLastRealWord")) {
        useLastRealWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNextRealWord")) {
        useNextRealWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useOccurrencePatterns")) {
        useOccurrencePatterns = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTypeySequences")) {
        useTypeySequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("justify")) {
        justify = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("normalize")) {
        normalize = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("priorType")) {
        priorType = val;
      } else if (key.equalsIgnoreCase("sigma")) {
        sigma = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("epsilon")) {
        epsilon = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("beamSize")) {
        beamSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("removeTopN")) {
        removeTopN = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("removeTopNPercent")) {
        removeTopNPercent = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("randomizedRatio")) {
        randomizedRatio = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("numTimesRemoveTopN")) {
        numTimesRemoveTopN = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("maxLeft")) {
        maxLeft = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("maxRight")) {
        maxRight = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("maxNGramLeng")) {
        maxNGramLeng = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useGazFeatures")) {
        useGazFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAltGazFeatures")) {
        useAltGazFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMoreGazFeatures")) {
        useMoreGazFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbbr")) {
        useAbbr = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMinimalAbbr")) {
        useMinimalAbbr = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbbr1")) {
        useAbbr1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMinimalAbbr1")) {
        useMinimalAbbr1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("documentReader")) {
        System.err.println("You are using an outdated flag: -documentReader " + val);
        System.err.println("Please use -readerAndWriter instead.");
      } else if (key.equalsIgnoreCase("deleteBlankLines")) {
        deleteBlankLines = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("answerFile")) {
        answerFile = val;
      } else if (key.equalsIgnoreCase("altAnswerFile")) {
        altAnswerFile = val;
      } else if (key.equalsIgnoreCase("loadClassifier") ||
                 key.equalsIgnoreCase("model")) {
        loadClassifier = val;
      } else if (key.equalsIgnoreCase("loadTextClassifier")) {
        loadTextClassifier = val;
      } else if (key.equalsIgnoreCase("loadJarClassifier")) {
        loadJarClassifier = val;
      } else if (key.equalsIgnoreCase("loadAuxClassifier")) {
        loadAuxClassifier = val;
      } else if (key.equalsIgnoreCase("serializeTo")) {
        serializeTo = val;
      } else if (key.equalsIgnoreCase("serializeToText")) {
        serializeToText = val;
      } else if (key.equalsIgnoreCase("serializeDatasetsDir")) {
        serializeDatasetsDir = val;
      } else if (key.equalsIgnoreCase("loadDatasetsDir")) {
        loadDatasetsDir = val;
      } else if (key.equalsIgnoreCase("pushDir")) {
        pushDir = val;
      } else if (key.equalsIgnoreCase("purgeDatasets")) {
        purgeDatasets = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("keepOBInMemory")) {
        keepOBInMemory = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("fakeDataset")) {
        fakeDataset = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("numDatasetsPerFile")) {
        numDatasetsPerFile = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("trainFile")) {
        trainFile = val;
      } else if (key.equalsIgnoreCase("biasedTrainFile")) {
        biasedTrainFile = val;
      } else if (key.equalsIgnoreCase("classBias")) {
        classBias = val;
      } else if (key.equalsIgnoreCase("confusionMatrix")) {
        confusionMatrix = val;
      } else if (key.equalsIgnoreCase("adaptFile")) {
        adaptFile = val;
      } else if (key.equalsIgnoreCase("devFile")) {
        devFile = val;
      } else if (key.equalsIgnoreCase("testFile")) {
        testFile = val;
      } else if (key.equalsIgnoreCase("outputFile")) {
        outputFile = val;
      } else if (key.equalsIgnoreCase("textFile")) {
        textFile = val;
      } else if (key.equalsIgnoreCase("readStdin")) {
        readStdin = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("initialWeights")) {
        initialWeights = val;
      } else if (key.equalsIgnoreCase("interimOutputFreq")) {
        interimOutputFreq = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("inputEncoding")) {
        inputEncoding = val;
      } else if (key.equalsIgnoreCase("outputEncoding")) {
        outputEncoding = val;
      } else if (key.equalsIgnoreCase("gazette")) {
        useGazettes = true;
        StringTokenizer st = new StringTokenizer(val, " ,;\t");
        if (gazettes == null) {
          gazettes = new ArrayList<>();
        } // for after deserialization, as gazettes is transient
        while (st.hasMoreTokens()) {
          gazettes.add(st.nextToken());
        }
      } else if (key.equalsIgnoreCase("useQN")) {
        useQN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("QNsize")) {
        QNsize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("QNsize2")) {
        QNsize2 = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("l1reg")) {
        useQN = false;
        l1reg = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("useFloat")) {
        useFloat = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("trainMap") || key.equalsIgnoreCase("testMap")) {
        System.err.println("trainMap and testMap are no longer valid options - please use map instead.");
        throw new RuntimeException();
      } else if (key.equalsIgnoreCase("map")) {
        map = val;
      } else if (key.equalsIgnoreCase("useMoreAbbr")) {
        useMoreAbbr = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePrevVB")) {
        usePrevVB = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNextVB")) {
        useNextVB = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useVB")) {
        if (Boolean.parseBoolean(val)) {
          useVB = true;
          usePrevVB = true;
          useNextVB = true;
        }
      } else if (key.equalsIgnoreCase("useChunks")) {
        useChunks = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useChunkySequences")) {
        useChunkySequences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("greekifyNGrams")) {
        greekifyNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("restrictTransitionsTimit")) {
        restrictTransitionsTimit = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMoreTags")) {
        useMoreTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useBeginSent")) {
        useBeginSent = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePosition")) {
        usePosition = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useGenia")) {
        useGENIA = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbstr")) {
        useABSTR = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWeb")) {
        useWEB = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAnte")) {
        useANTE = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAcr")) {
        useACR = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTok")) {
        useTOK = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbgene")) {
        useABGENE = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbstrFreqDict")) {
        useABSTRFreqDict = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAbstrFreq")) {
        useABSTRFreq = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFreq")) {
        useFREQ = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usewebfreqdict")) {
        useWEBFreqDict = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("bioSubmitOutput")) {
        bioSubmitOutput = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("subCWGaz")) {
        subCWGaz = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("splitOnHead")) {
        splitOnHead = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("featureCountThreshold")) {
        featureCountThreshold = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useWord")) {
        useWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("memoryThrift")) {
        memoryThrift = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("timitDatum")) {
        timitDatum = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("splitDocuments")) {
        System.err.println("You are using an outdated flag: -splitDocuments");
        System.err.println("Please use -maxDocSize -1 instead.");
        splitDocuments = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("featureWeightThreshold")) {
        featureWeightThreshold = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("backgroundSymbol")) {
        backgroundSymbol = val;
      } else if (key.equalsIgnoreCase("featureFactory")) {
        featureFactory = val;
        if (featureFactory.equalsIgnoreCase("SuperSimpleFeatureFactory")) {
          featureFactory = "edu.stanford.nlp.sequences.SuperSimpleFeatureFactory";
        } else if (featureFactory.equalsIgnoreCase("NERFeatureFactory")) {
          featureFactory = "edu.stanford.nlp.ie.NERFeatureFactory";
        } else if (featureFactory.equalsIgnoreCase("GazNERFeatureFactory")) {
          featureFactory = "edu.stanford.nlp.sequences.GazNERFeatureFactory";
        } else if (featureFactory.equalsIgnoreCase("IncludeAllFeatureFactory")) {
          featureFactory = "edu.stanford.nlp.sequences.IncludeAllFeatureFactory";
        } else if (featureFactory.equalsIgnoreCase("PhraseFeatureFactory")) {
          featureFactory = "edu.stanford.nlp.article.extraction.PhraseFeatureFactory";
        }

      } else if (key.equalsIgnoreCase("printXML")) {
        printXML = Boolean.parseBoolean(val); // todo: This appears unused now.
        // Was it replaced by
        // outputFormat?

      } else if (key.equalsIgnoreCase("useSeenFeaturesOnly")) {
        useSeenFeaturesOnly = Boolean.parseBoolean(val);

      } else if (key.equalsIgnoreCase("useBagOfWords")) {
        useBagOfWords = Boolean.parseBoolean(val);

        // chinese word-segmenter features
      } else if (key.equalsIgnoreCase("useRadical")) {
        useRadical = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useBigramInTwoClique")) {
        useBigramInTwoClique = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useReverseAffix")) {
        useReverseAffix = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("charHalfWindow")) {
        charHalfWindow = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("purgeFeatures")) {
        purgeFeatures = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("ocrFold")) {
        ocrFold = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("morphFeatureFile")) {
        morphFeatureFile = val;
      } else if (key.equalsIgnoreCase("svmModelFile")) {
        svmModelFile = val;
        /* Dictionary */
      } else if (key.equalsIgnoreCase("useDictleng")) {
        useDictleng = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDict2")) {
        useDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useOutDict2")) {
        useOutDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("outDict2")) {
        outDict2 = val;
      } else if (key.equalsIgnoreCase("useDictCTB2")) {
        useDictCTB2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDictASBC2")) {
        useDictASBC2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDictPK2")) {
        useDictPK2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDictHK2")) {
        useDictHK2 = Boolean.parseBoolean(val);
        /* N-gram flags */
      } else if (key.equalsIgnoreCase("useWord1")) {
        useWord1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWord2")) {
        useWord2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWord3")) {
        useWord3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWord4")) {
        useWord4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useRad1")) {
        useRad1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useRad2")) {
        useRad2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useRad2b")) {
        useRad2b = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordn")) {
        useWordn = Boolean.parseBoolean(val);
        /* affix flags */
      } else if (key.equalsIgnoreCase("useCTBPre1")) {
        useCTBPre1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useCTBSuf1")) {
        useCTBSuf1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useASBCPre1")) {
        useASBCPre1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useASBCSuf1")) {
        useASBCSuf1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHKPre1")) {
        useHKPre1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHKSuf1")) {
        useHKSuf1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePKPre1")) {
        usePKPre1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePKSuf1")) {
        usePKSuf1 = Boolean.parseBoolean(val);
        /* POS flags */
      } else if (key.equalsIgnoreCase("useCTBChar2")) {
        useCTBChar2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePrediction")) {
        usePrediction = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useASBCChar2")) {
        useASBCChar2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHKChar2")) {
        useHKChar2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePKChar2")) {
        usePKChar2 = Boolean.parseBoolean(val);
        /* Rule flag */
      } else if (key.equalsIgnoreCase("useRule2")) {
        useRule2 = Boolean.parseBoolean(val);
        /* ASBC and HK */
      } else if (key.equalsIgnoreCase("useBig5")) {
        useBig5 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegDict2")) {
        useNegDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegDict3")) {
        useNegDict3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegDict4")) {
        useNegDict4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegCTBDict2")) {
        useNegCTBDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegCTBDict3")) {
        useNegCTBDict3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegCTBDict4")) {
        useNegCTBDict4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegASBCDict2")) {
        useNegASBCDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegASBCDict3")) {
        useNegASBCDict3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegASBCDict4")) {
        useNegASBCDict4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegPKDict2")) {
        useNegPKDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegPKDict3")) {
        useNegPKDict3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegPKDict4")) {
        useNegPKDict4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegHKDict2")) {
        useNegHKDict2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegHKDict3")) {
        useNegHKDict3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNegHKDict4")) {
        useNegHKDict4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePre")) {
        usePre = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSuf")) {
        useSuf = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useRule")) {
        useRule = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAs")) {
        useAs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePk")) {
        usePk = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHk")) {
        useHk = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMsr")) {
        useMsr = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMSRChar2")) {
        useMSRChar2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesC4gram")) {
        useFeaturesC4gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesC5gram")) {
        useFeaturesC5gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesC6gram")) {
        useFeaturesC6gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesCpC4gram")) {
        useFeaturesCpC4gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesCpC5gram")) {
        useFeaturesCpC5gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFeaturesCpC6gram")) {
        useFeaturesCpC6gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useUnicodeType")) {
        useUnicodeType = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useUnicodeBlock")) {
        useUnicodeBlock = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useUnicodeType4gram")) {
        useUnicodeType4gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useUnicodeType5gram")) {
        useUnicodeType5gram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useShapeStrings1")) {
        useShapeStrings1 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useShapeStrings3")) {
        useShapeStrings3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useShapeStrings4")) {
        useShapeStrings4 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useShapeStrings5")) {
        useShapeStrings5 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordUTypeConjunctions2")) {
        useWordUTypeConjunctions2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordUTypeConjunctions3")) {
        useWordUTypeConjunctions3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordShapeConjunctions2")) {
        useWordShapeConjunctions2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordShapeConjunctions3")) {
        useWordShapeConjunctions3 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMidDotShape")) {
        useMidDotShape = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("augmentedDateChars")) {
        augmentedDateChars = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("suppressMidDotPostprocessing")) {
        suppressMidDotPostprocessing = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("printNR")) {
        printNR = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("use4Clique")) {
        use4Clique = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFilter")) {
        useFilter = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("largeChSegFile")) {
        largeChSegFile = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("keepEnglishWhitespaces")) {
        keepEnglishWhitespaces = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("keepAllWhitespaces")) {
        keepAllWhitespaces = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("sighanPostProcessing")) {
        sighanPostProcessing = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useChPos")) {
        useChPos = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("sighanCorporaDict")) {
        sighanCorporaDict = val;
        // end chinese word-segmenter features
      } else if (key.equalsIgnoreCase("useObservedSequencesOnly")) {
        useObservedSequencesOnly = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("maxDocSize")) {
        maxDocSize = Integer.parseInt(val);
        splitDocuments = true;
      } else if (key.equalsIgnoreCase("printProbs")) {
        printProbs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("printFirstOrderProbs")) {
        printFirstOrderProbs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("saveFeatureIndexToDisk")) {
        saveFeatureIndexToDisk = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("removeBackgroundSingletonFeatures")) {
        removeBackgroundSingletonFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("doGibbs")) {
        doGibbs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNERPrior")) {
        useNERPrior = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAcqPrior")) {
        useAcqPrior = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSemPrior")) {
        useSemPrior = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useMUCFeatures")) {
        useMUCFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("initViterbi")) {
        initViterbi = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("checkNameList")) {
        checkNameList = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFirstWord")) {
        useFirstWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useUnknown")) {
        useUnknown = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("cacheNGrams")) {
        cacheNGrams = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNumberFeature")) {
        useNumberFeature = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("annealingRate")) {
        annealingRate = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("annealingType")) {
        if (val.equalsIgnoreCase("linear") || val.equalsIgnoreCase("exp") || val.equalsIgnoreCase("exponential")) {
          annealingType = val;
        } else {
          System.err.println("unknown annealingType: " + val + ".  Please use linear|exp|exponential");
        }
      } else if (key.equalsIgnoreCase("numSamples")) {
        numSamples = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("inferenceType")) {
        inferenceType = val;
      } else if (key.equalsIgnoreCase("loadProcessedData")) {
        loadProcessedData = val;
      } else if (key.equalsIgnoreCase("normalizationTable")) {
        normalizationTable = val;
      } else if (key.equalsIgnoreCase("dictionary")) {
        // don't set if empty string or spaces or true: revert it to null
        // special case so can empty out dictionary list on command line!
        val = val.trim();
          dictionary = !val.isEmpty() && !"true".equals(val) && !"null".equals(val) && !"false".equals("val") ? val : null;
      } else if (key.equalsIgnoreCase("serDictionary")) {
        // don't set if empty string or spaces or true: revert it to null
        // special case so can empty out dictionary list on command line!
        val = val.trim();
          serializedDictionary = !val.isEmpty() && !"true".equals(val) && !"null".equals(val) && !"false".equals("val") ? val : null;
      } else if (key.equalsIgnoreCase("dictionary2")) {
        // don't set if empty string or spaces or true: revert it to null
        // special case so can empty out dictionary list on command line!
        val = val.trim();
          dictionary2 = !val.isEmpty() && !"true".equals(val) && !"null".equals(val) && !"false".equals("val") ? val : null;
      } else if (key.equalsIgnoreCase("normTableEncoding")) {
        normTableEncoding = val;
      } else if (key.equalsIgnoreCase("useLemmaAsWord")) {
        useLemmaAsWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("type")) {
        type = val;
      } else if (key.equalsIgnoreCase("readerAndWriter")) {
        readerAndWriter = val;
      } else if (key.equalsIgnoreCase("plainTextDocumentReaderAndWriter")) {
        plainTextDocumentReaderAndWriter = val;
      } else if (key.equalsIgnoreCase("gazFilesFile")) {
        gazFilesFile = val;
      } else if (key.equalsIgnoreCase("baseTrainDir")) {
        baseTrainDir = val;
      } else if (key.equalsIgnoreCase("baseTestDir")) {
        baseTestDir = val;
      } else if (key.equalsIgnoreCase("trainFiles")) {
        trainFiles = val;
      } else if (key.equalsIgnoreCase("trainFileList")) {
        trainFileList = val;
      } else if (key.equalsIgnoreCase("trainDirs")) {
        trainDirs = val;
      } else if (key.equalsIgnoreCase("testDirs")) {
        testDirs = val;
      } else if (key.equalsIgnoreCase("testFiles")) {
        testFiles = val;
      } else if (key.equalsIgnoreCase("textFiles")) {
        textFiles = val;
      } else if (key.equalsIgnoreCase("usePrediction2")) {
        usePrediction2 = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useObservedFeaturesOnly")) {
        useObservedFeaturesOnly = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("iobWrapper")) {
        iobWrapper = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useDistSim")) {
        useDistSim = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("casedDistSim")) {
        casedDistSim = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("distSimFileFormat")) {
        distSimFileFormat = val;
      } else if (key.equalsIgnoreCase("distSimMaxBits")) {
        distSimMaxBits = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("numberEquivalenceDistSim")) {
        numberEquivalenceDistSim = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("unknownWordDistSimClass")) {
        unknownWordDistSimClass = val;
      } else if (key.equalsIgnoreCase("useOnlySeenWeights")) {
        useOnlySeenWeights = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("predProp")) {
        predProp = val;
      } else if (key.equalsIgnoreCase("distSimLexicon")) {
        distSimLexicon = val;
      } else if (key.equalsIgnoreCase("useSegmentation")) {
        useSegmentation = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useInternal")) {
        useInternal = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useExternal")) {
        useExternal = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEitherSideWord")) {
        useEitherSideWord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEitherSideDisjunctive")) {
        useEitherSideDisjunctive = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("featureDiffThresh")) {
        featureDiffThresh = Double.parseDouble(val);
        if (props.getProperty("numTimesPruneFeatures") == null) {
          numTimesPruneFeatures = 1;
        }
      } else if (key.equalsIgnoreCase("numTimesPruneFeatures")) {
        numTimesPruneFeatures = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("newgeneThreshold")) {
        newgeneThreshold = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("adaptFile")) {
        adaptFile = val;
      } else if (key.equalsIgnoreCase("doAdaptation")) {
        doAdaptation = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("selfTrainFile")) {
        selfTrainFile = val;
      } else if (key.equalsIgnoreCase("selfTrainIterations")) {
        selfTrainIterations = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("selfTrainWindowSize")) {
        selfTrainWindowSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("selfTrainConfidenceThreshold")) {
        selfTrainConfidenceThreshold = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("numFolds")) {
        numFolds = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("startFold")) {
        startFold = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("endFold")) {
        endFold = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("adaptSigma")) {
        adaptSigma = Double.parseDouble(val);
      } else if (key.startsWith("prop") && !key.equals("prop")) {
        comboProps.add(val);
      } else if (key.equalsIgnoreCase("outputFormat")) {
        outputFormat = val;
      } else if (key.equalsIgnoreCase("useSMD")) {
        useSMD = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useScaledSGD")) {
        useScaledSGD = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("scaledSGDMethod")) {
        scaledSGDMethod = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("tuneSGD")) {
        tuneSGD = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("StochasticCalculateMethod")) {
        if (val.equalsIgnoreCase("AlgorithmicDifferentiation")) {
          stochasticMethod = StochasticCalculateMethods.AlgorithmicDifferentiation;
        } else if (val.equalsIgnoreCase("IncorporatedFiniteDifference")) {
          stochasticMethod = StochasticCalculateMethods.IncorporatedFiniteDifference;
        } else if (val.equalsIgnoreCase("ExternalFinitedifference")) {
          stochasticMethod = StochasticCalculateMethods.ExternalFiniteDifference;
        }
      } else if (key.equalsIgnoreCase("initialGain")) {
        initialGain = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("stochasticBatchSize")) {
        stochasticBatchSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("SGD2QNhessSamples")) {
        SGD2QNhessSamples = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useSGD")) {
        useSGD = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useInPlaceSGD")) {
        useInPlaceSGD = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSGDtoQN")) {
        useSGDtoQN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("SGDPasses")) {
        SGDPasses = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("QNPasses")) {
        QNPasses = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("gainSGD")) {
        gainSGD = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("useHybrid")) {
        useHybrid = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("hybridCutoffIteration")) {
        hybridCutoffIteration = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useStochasticQN")) {
        useStochasticQN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("outputIterationsToFile")) {
        outputIterationsToFile = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("testObjFunction")) {
        testObjFunction = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("testVariance")) {
        testVariance = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("CRForder")) {
        CRForder = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("CRFwindow")) {
        CRFwindow = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("testHessSamples")) {
        testHessSamples = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("estimateInitial")) {
        estimateInitial = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("printLabelValue")) {
        printLabelValue = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("searchGraphPrefix")) {
        searchGraphPrefix = val;
      } else if (key.equalsIgnoreCase("searchGraphPrune")) {
        searchGraphPrune = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("kBest")) {
        useKBest = true;
        kBest = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useRobustQN")) {
        useRobustQN = true;
      } else if (key.equalsIgnoreCase("combo")) {
        combo = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("verboseForTrueCasing")) {
        verboseForTrueCasing = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("trainHierarchical")) {
        trainHierarchical = val;
      } else if (key.equalsIgnoreCase("domain")) {
        domain = val;
      } else if (key.equalsIgnoreCase("baseline")) {
        baseline = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("doFE")) {
        doFE = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("restrictLabels")) {
        restrictLabels = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("transferSigmas")) {
        transferSigmas = val;
      } else if (key.equalsIgnoreCase("announceObjectBankEntries")) {
        announceObjectBankEntries = true;
      } else if (key.equalsIgnoreCase("usePos")) {
        usePos = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAgreement")) {
        useAgreement = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAccCase")) {
        useAccCase = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useInna")) {
        useInna = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useConcord")) {
        useConcord = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useFirstNgram")) {
        useFirstNgram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLastNgram")) {
        useLastNgram = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("collapseNN")) {
        collapseNN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTagsCpC")) {
        useTagsCpC = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTagsCpCp2C")) {
        useTagsCpCp2C = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTagsCpCp2Cp3C")) {
        useTagsCpCp2Cp3C = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTagsCpCp2Cp3Cp4C")) {
        useTagsCpCp2Cp3Cp4C = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("numTags")) {
        numTags = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useConjBreak")) {
        useConjBreak = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAuxPairs")) {
        useAuxPairs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePPVBPairs")) {
        usePPVBPairs = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useAnnexing")) {
        useAnnexing = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useTemporalNN")) {
        useTemporalNN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("markProperNN")) {
        markProperNN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePath")) {
        usePath = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("markMasdar")) {
        markMasdar = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("innaPPAttach")) {
        innaPPAttach = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSVO")) {
        useSVO = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("mixedCaseMapFile")) {
        mixedCaseMapFile = val;
      } else if (key.equalsIgnoreCase("auxTrueCaseModels")) {
        auxTrueCaseModels = val;
      } else if (key.equalsIgnoreCase("use2W")) {
        use2W = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLC")) {
        useLC = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useYetMoreCpCShapes")) {
        useYetMoreCpCShapes = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useIfInteger")) {
        useIfInteger = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("twoStage")) {
        twoStage = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("evaluateIters")) {
        evaluateIters = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("evalCmd")) {
        evalCmd = val;
      } else if (key.equalsIgnoreCase("evaluateTrain")) {
        evaluateTrain = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("evaluateBackground")) {
        evaluateBackground = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("tuneSampleSize")) {
        tuneSampleSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useTopics")) {
        useTopics = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePhraseFeatures")) {
        usePhraseFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePhraseWords")) {
        usePhraseWords = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePhraseWordTags")) {
        usePhraseWordTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("usePhraseWordSpecialTags")) {
        usePhraseWordSpecialTags = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useProtoFeatures")) {
        useProtoFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useWordnetFeatures")) {
        useWordnetFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("wikiFeatureDbFile")) {
        wikiFeatureDbFile = val;
      } else if (key.equalsIgnoreCase("tokenizerOptions")) {
        tokenizerOptions = val;
      } else if (key.equalsIgnoreCase("tokenizerFactory")) {
        tokenizerFactory = val;
      } else if (key.equalsIgnoreCase("useCommonWordsFeature")) {
        useCommonWordsFeature = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useYear")) {
        useYear = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSentenceNumber")) {
        useSentenceNumber = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useLabelSource")) {
        useLabelSource = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("tokenFactory")) {

        tokenFactory = val;
      } else if (key.equalsIgnoreCase("tokensAnnotationClassName")) {
        tokensAnnotationClassName = val;
      } else if (key.equalsIgnoreCase("numLopExpert")) {
        numLopExpert = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("initialLopScales")) {
        initialLopScales = val;
      } else if (key.equalsIgnoreCase("initialLopWeights")) {
        initialLopWeights = val;
      } else if (key.equalsIgnoreCase("includeFullCRFInLOP")) {
        includeFullCRFInLOP = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("backpropLopTraining")) {
        backpropLopTraining = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("randomLopWeights")) {
        randomLopWeights = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("randomLopFeatureSplit")) {
        randomLopFeatureSplit = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("nonLinearCRF")) {
        nonLinearCRF = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("secondOrderNonLinear")) {
        secondOrderNonLinear = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("numHiddenUnits")) {
        numHiddenUnits = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useOutputLayer")) {
        useOutputLayer = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useHiddenLayer")) {
        useHiddenLayer = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("gradientDebug")) {
        gradientDebug = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("checkGradient")) {
        checkGradient = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSigmoid")) {
        useSigmoid = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("skipOutputRegularization")) {
        skipOutputRegularization = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("sparseOutputLayer")) {
        sparseOutputLayer = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("tieOutputLayer")) {
        tieOutputLayer = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("blockInitialize")) {
        blockInitialize = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("softmaxOutputLayer")) {
        softmaxOutputLayer = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("loadBisequenceClassifierEn")) {
        loadBisequenceClassifierEn = val;
      } else if (key.equalsIgnoreCase("bisequenceClassifierPropEn")) {
        bisequenceClassifierPropEn = val;
      } else if (key.equalsIgnoreCase("loadBisequenceClassifierCh")) {
        loadBisequenceClassifierCh = val;
      } else if (key.equalsIgnoreCase("bisequenceClassifierPropCh")) {
        bisequenceClassifierPropCh = val;
      } else if (key.equalsIgnoreCase("bisequenceTestFileEn")) {
        bisequenceTestFileEn = val;
      } else if (key.equalsIgnoreCase("bisequenceTestFileCh")) {
        bisequenceTestFileCh = val;
      } else if (key.equalsIgnoreCase("bisequenceTestOutputEn")) {
        bisequenceTestOutputEn = val;
      } else if (key.equalsIgnoreCase("bisequenceTestOutputCh")) {
        bisequenceTestOutputCh = val;
      } else if (key.equalsIgnoreCase("bisequenceTestAlignmentFile")) {
        bisequenceTestAlignmentFile = val;
      } else if (key.equalsIgnoreCase("bisequencePriorType")) {
        bisequencePriorType = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("bisequenceAlignmentPriorPenaltyCh")) {
        bisequenceAlignmentPriorPenaltyCh = val;
      } else if (key.equalsIgnoreCase("bisequenceAlignmentPriorPenaltyEn")) {
        bisequenceAlignmentPriorPenaltyEn = val;
      } else if (key.equalsIgnoreCase("alignmentPruneThreshold")) {
        alignmentPruneThreshold = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("factorInAlignmentProb")) {
        factorInAlignmentProb = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useChromaticSampling")) {
        useChromaticSampling = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useSequentialScanSampling")) {
        useSequentialScanSampling = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("maxAllowedChromaticSize")) {
        maxAllowedChromaticSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("inputDropOut")) {
        inputDropOut = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("keepEmptySentences")) {
        keepEmptySentences = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useBilingualNERPrior")) {
        useBilingualNERPrior = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("samplingSpeedUpThreshold")) {
        samplingSpeedUpThreshold = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("entityMatrixCh")) {
        entityMatrixCh = val;
      } else if (key.equalsIgnoreCase("entityMatrixEn")) {
        entityMatrixEn = val;
      } else if (key.equalsIgnoreCase("multiThreadGibbs")) {
        multiThreadGibbs = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("matchNERIncentive")) {
        matchNERIncentive = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useEmbedding")) {
        useEmbedding = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("prependEmbedding")) {
        prependEmbedding = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("embeddingWords")) {
        embeddingWords = val;
      } else if (key.equalsIgnoreCase("embeddingVectors")) {
        embeddingVectors = val;
      } else if (key.equalsIgnoreCase("transitionEdgeOnly")) {
        transitionEdgeOnly = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("priorL1Lambda")) {
        priorL1Lambda = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("addCapitalFeatures")) {
        addCapitalFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("arbitraryInputLayerSize")) {
        arbitraryInputLayerSize = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("noEdgeFeature")) {
        noEdgeFeature = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("terminateOnEvalImprovement")) {
        terminateOnEvalImprovement = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("terminateOnEvalImprovementNumOfEpoch")) {
        terminateOnEvalImprovementNumOfEpoch = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useMemoryEvaluator")) {
        useMemoryEvaluator = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("suppressTestDebug")) {
        suppressTestDebug = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useOWLQN")) {
        useOWLQN = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("printWeights")) {
        printWeights = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("totalDataSlice")) {
        totalDataSlice = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("numOfSlices")) {
        numOfSlices = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("regularizeSoftmaxTieParam")) {
        regularizeSoftmaxTieParam = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("softmaxTieLambda")) {
        softmaxTieLambda = Double.parseDouble(val);
      } else if (key.equalsIgnoreCase("totalFeatureSlice")) {
        totalFeatureSlice = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("numOfFeatureSlices")) {
        numOfFeatureSlices = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("addBiasToEmbedding")) {
        addBiasToEmbedding = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("hardcodeSoftmaxOutputWeights")) {
        hardcodeSoftmaxOutputWeights = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("useNERPriorBIO")) {
        useNERPriorBIO = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("entityMatrix")) {
        entityMatrix = val;
      } else if (key.equalsIgnoreCase("multiThreadClassifier")) {
        multiThreadClassifier = Integer.parseInt(val);
      } else if (key.equalsIgnoreCase("useGenericFeatures")) {
        useGenericFeatures = Boolean.parseBoolean(val);
      } else if (key.equalsIgnoreCase("splitWordRegex")){
        splitWordRegex = val;
        // ADD VALUE ABOVE HERE
      } else if (!key.isEmpty() && !key.equals("prop")) {
        System.err.println("Unknown property: |" + key + '|');
      }
    }
    if (startFold > numFolds) {
      System.err.println("startFold > numFolds -> setting startFold to 1");
      startFold = 1;
    }
    if (endFold > numFolds) {
      System.err.println("endFold > numFolds -> setting to numFolds");
      endFold = numFolds;
    }

    if (combo) {
      splitDocuments = false;
    }

    stringRep = sb.toString();
  } // end setProperties()

  /**
   * Print the properties specified by this object.
   *
   * @return A String describing the properties specified by this object.
   */
  @Override
  public String toString() {
    return stringRep;
  }

  /**
   * note that this does *not* return string representation of arrays, lists and
   * enums
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public String getNotNullTrueStringRep() {
    try {
      String rep = "";
      String joiner = "\n";
      Field[] f = this.getClass().getFields();
      for (Field ff : f) {

        String name = ff.getName();
        Class<?> type = ff.getType();

        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
          boolean val = ff.getBoolean(this);
          if (val) {
            rep += joiner + name + '=' + val;
          }
        } else if (type.equals(String.class)) {
          String val = (String) ff.get(this);
          if (val != null)
            rep += joiner + name + '=' + val;
        } else if (type.equals(Double.class)) {
          Double val = (Double) ff.get(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(double.class)) {
          double val = ff.getDouble(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(Integer.class)) {
          Integer val = (Integer) ff.get(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(int.class)) {
          int val = ff.getInt(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(Float.class)) {
          Float val = (Float) ff.get(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(float.class)) {
          float val = ff.getFloat(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(Byte.class)) {
          Byte val = (Byte) ff.get(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(byte.class)) {
          byte val = ff.getByte(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(char.class)) {
          char val = ff.getChar(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(Long.class)) {
          Long val = (Long) ff.get(this);
          rep += joiner + name + '=' + val;
        } else if (type.equals(long.class)) {
          long val = ff.getLong(this);
          rep += joiner + name + '=' + val;
        }
      }
      return rep;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

} // end class SeqClassifierFlags
