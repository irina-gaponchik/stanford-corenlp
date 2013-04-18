package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.ling.tokensregex.parser.TokenSequenceParser;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Token Sequence Pattern for regular expressions for sequences over tokens (as the more general {@code CoreMap})
 * Sequences over tokens can be matched like strings.
 * <p>
 * To use
 * <pre>{@code
 *   TokenSequencePattern p = TokenSequencePattern.compile("....");
 *   TokenSequenceMatcher m = p.getMatcher(tokens);
 *   while (m.find()) ....
 * }</pre>
 * </p>
 *
 * <p>
 * Supports the following:
 * <ul>
 *  <li>Concatenation: {@code X Y}</li>
 *  <li>Or: {@code X | Y}</li>
 *  <li>And: {@code X & Y}</li>
 *  <li>Groups:
 *     <ul>
 *     <li>capturing: {@code (X)} (with numeric group id)</li>
 *     <li>capturing: {@code (?$var X)} (with group name "$var")</li>
 *     <li>noncapturing: {@code (?:X)}</li>
 *     </ul>
 *  Capturing groups can be retrieved with group id or group variable, as matched string
 *     ({@code m.group()}) or list of tokens ({@code m.groupNodes()}).
 *  <ul>
 *     <li>To retrieve group using id: {@code m.group(id)} or {@code m.groupNodes(id)}
 *     <br> NOTE: Capturing groups are indexed from left to right, starting at one.  Group zero is the entire matched sequence.
 *     </li>
 *     <li>To retrieve group using bind variable name: {@code m.group("$var")} or {@code m.groupNodes("$var")}
 *     </li>
 *  </ul>
 *  See {@link SequenceMatchResult} for more accessor functions to retrieve matches.
 * </li>
 * <li>Greedy Quantifiers:  {@code X+, X?, X*, X{n,m}, X{n}, X{n,}}</li>
 * <li>Reluctant Quantifiers: {@code X+?, X??, X*?, X{n,m}?, X{n}?, X{n,}?}</li>
 * <li>Back references: {@code \captureid} </li>
 * <li>Value binding for groups: {@code [pattern] => [value]}.
 *   Value for matched expression can be accessed using {@code m.groupValue()}
 *   <br></br>Example: <pre>( one => 1 | two => 2 | three => 3 | ...)</pre>
 * </li>
 * </ul>
 *
 * <p>
 * Individual tokens are marked by {@code "[" TOKEN_EXPR "]" }
 * <br>Possible {@code TOKEN_EXPR}:
 * <ul>
 * <li> All specified token attributes match:
 * <br> For Strings:
 *     {@code  { lemma:/.../; tag:"NNP" } } = attributes that need to all match
 * <br> NOTE: {@code /.../} used for regular expressions,
 *            {@code "..."} for exact string matches
 * <br> For Numbers:
 *      {@code { word>=2 }}
 * <br> NOTE: Relation can be {@code ">=", "<=", ">", "<",} or {@code "=="}
 * <br> Others:
 *      {@code { word::IS_NUM } , { word::IS_NIL } } or
 *      {@code { word::NOT_EXISTS }, { word::NOT_NIL } } or {@code  { word::EXISTS } }
 * </li>
 * <li>Short hand for just word/text match:
 *     {@code  /.../ }  or  {@code "..." }
 * </li>
 * <li>
 *  Negation:
 *     {@code  !{...} }
 * </li>
 * <li>
 *  Conjunction or Disjunction:
 *     {@code  {...} & {...} }   or  {@code  {...} | {...} }
 * </li>
 * </ui>
 * </p>
 *
 * <p>
 * Special tokens:
 *   Any token: {@code []}
 * </p>
 *
 * <p>
 * String pattern match across multiple tokens:
 *   {@code (?m){min,max} /pattern/}
 * </p>
 *
 * <p>
 * Binding of variables for use in compiling patterns:
 * <ol>
 * <li> Use  Env env = TokenSequencePattern.getNewEnv() to create new environment for binding </li>
 * <li> Bind string to attribute key (Class) lookup
 *    env.bind("numtype", CoreAnnotations.NumericTypeAnnotation.class);
 * </li>
 * <li> Bind patterns / strings for compiling patterns
 *    <pre>{@code
 *    // Bind string for later compilation using: compile("/it/ /was/ $RELDAY");
 *    env.bind("$RELDAY", "/today|yesterday|tomorrow|tonight|tonite/");
 *    // Bind pre-compiled patter for later compilation using: compile("/it/ /was/ $RELDAY");
 *    env.bind("$RELDAY", TokenSequencePattern.compile(env, "/today|yesterday|tomorrow|tonight|tonite/"));
 *    }</pre>
 * </li>
 * <li> Bind custom node pattern functions (currently no arguments are supported)
 *    <pre>{@code
 *    // Bind node pattern so we can do patterns like: compile("... temporal::IS_TIMEX_DATE ...");
 *    //   (TimexTypeMatchNodePattern is a NodePattern that implements some custom logic)
 *    env.bind("::IS_TIMEX_DATE", new TimexTypeMatchNodePattern(SUTime.TimexType.DATE));
 *   }</pre>
 * </li>
 * </ol>
 * </p>
 *
 * <p>
 * Actions (partially implemented)
 * <ul>
 * <li> {@code pattern ==> action} </li>
 * <li> Supported action:
 *    {@code &annotate( { ner="DATE" } )} </li>
 * <li> Not applied automatically, associated with a pattern.</li>
 * <li> To apply, call {@code pattern.getAction().apply(match, groupid)}</li>
 * </ul>
 * </p>
 *
 * @author Angel Chang
 * @see TokenSequenceMatcher
 */
public class TokenSequencePattern extends SequencePattern<CoreMap> {
  public static final TokenSequencePattern ANY_NODE_PATTERN = TokenSequencePattern.compile(ANY_NODE_PATTERN_EXPR);

  private static Env DEFAULT_ENV = getNewEnv();

  public TokenSequencePattern(String patternStr, SequencePattern.PatternExpr nodeSequencePattern) {
    super(patternStr, nodeSequencePattern);
  }

  public TokenSequencePattern(String patternStr, SequencePattern.PatternExpr nodeSequencePattern,
                                 SequenceMatchAction<CoreMap> action) {
    super(patternStr, nodeSequencePattern, action);
  }

  public static Env getNewEnv() {
    Env env =  new Env(new TokenSequenceParser());
    env.initDefaultBindings();
    return env;
  }

  /**
   * Compiles a regular expression over tokens into a TokenSequencePattern using the default environment
   * @param string Regular expression to be compiled
   * @return Compiled TokenSequencePattern
   */
  public static TokenSequencePattern compile(String string)
  {
    return compile(DEFAULT_ENV, string);
  }

  /**
   * Compiles a regular expression over tokens into a TokenSequencePattern using the specified environment
   * @param env Environment to use
   * @param string Regular expression to be compiled
   * @return Compiled TokenSequencePattern
   */
  public static TokenSequencePattern compile(Env env, String string)
  {
    try {
//      SequencePattern.PatternExpr nodeSequencePattern = TokenSequenceParser.parseSequence(env, string);
//      return new TokenSequencePattern(string, nodeSequencePattern);
      // TODO: Check token sequence parser?
      Pair<PatternExpr, SequenceMatchAction<CoreMap>> p = env.parser.parseSequenceWithAction(env, string);
      return new TokenSequencePattern(string, p.first(), p.second());

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Compiles a sequence of regular expression a TokenSequencePattern using the default environment
   * @param strings List of regular expression to be compiled
   * @return Compiled TokenSequencePattern
   */
  public static TokenSequencePattern compile(String... strings)
  {
    return compile(DEFAULT_ENV, strings);
  }

  /**
   * Compiles a sequence of regular expression a TokenSequencePattern using the specified environment
   * @param env Environment to use
   * @param strings List of regular expression to be compiled
   * @return Compiled TokenSequencePattern
   */
  public static TokenSequencePattern compile(Env env, String... strings)
  {
    try {
      List<SequencePattern.PatternExpr> patterns = new ArrayList<>();
      for (String string:strings) {
        // TODO: Check token sequence parser?
        SequencePattern.PatternExpr pattern = env.parser.parseSequence(env, string);
        patterns.add(pattern);
      }
      SequencePattern.PatternExpr nodeSequencePattern = new SequencePattern.SequencePatternExpr(patterns);
      return new TokenSequencePattern(StringUtils.join(strings), nodeSequencePattern);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static TokenSequencePattern compile(SequencePattern.PatternExpr nodeSequencePattern)
  {
    return new TokenSequencePattern(null, nodeSequencePattern);
  }

  /**
   * Returns a TokenSequenceMatcher that can be used to match this pattern against the specified list of tokens
   * @param tokens List of tokens to match against
   * @return TokenSequenceMatcher
   */
  public TokenSequenceMatcher getMatcher(List<? extends CoreMap> tokens) {
    return new TokenSequenceMatcher(this, tokens);
  }
}
