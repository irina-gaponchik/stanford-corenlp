package edu.stanford.nlp.dcoref;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.TextBuilder;
import javolution.util.FastMap;
import javolution.util.FastSet;

public class Dictionaries {

  public enum MentionType { PRONOMINAL, NOMINAL, PROPER }

  public enum Gender { MALE, FEMALE, NEUTRAL, UNKNOWN }

  public enum Number { SINGULAR, PLURAL, UNKNOWN }
  public enum Animacy { ANIMATE, INANIMATE, UNKNOWN }
  public enum Person { I, YOU, HE, SHE, WE, THEY, IT, UNKNOWN}

  public final Set<String> reportVerb = new FastSet<>((Set<? extends String>) Arrays.asList(
          "accuse", "acknowledge", "add", "admit", "advise", "agree", "alert",
          "allege", "announce", "answer", "apologize", "argue",
          "ask", "assert", "assure", "beg", "blame", "boast",
          "caution", "charge", "cite", "claim", "clarify", "command", "comment",
          "compare", "complain", "concede", "conclude", "confirm", "confront", "congratulate",
          "contend", "contradict", "convey", "counter", "criticize",
          "debate", "decide", "declare", "defend", "demand", "demonstrate", "deny",
          "describe", "determine", "disagree", "disclose", "discount", "discover", "discuss",
          "dismiss", "dispute", "disregard", "doubt", "emphasize", "encourage", "endorse",
          "equate", "estimate", "expect", "explain", "express", "extoll", "fear", "feel",
          "find", "forbid", "forecast", "foretell", "forget", "gather", "guarantee", "guess",
          "hear", "hint", "hope", "illustrate", "imagine", "imply", "indicate", "inform",
          "insert", "insist", "instruct", "interpret", "interview", "invite", "issue",
          "justify", "learn", "maintain", "mean", "mention", "negotiate", "note",
          "observe", "offer", "oppose", "order", "persuade", "pledge", "point", "point out",
          "praise", "pray", "predict", "prefer", "present", "promise", "prompt", "propose",
          "protest", "prove", "provoke", "question", "quote", "raise", "rally", "read",
          "reaffirm", "realise", "realize", "rebut", "recall", "reckon", "recommend", "refer",
          "reflect", "refuse", "refute", "reiterate", "reject", "relate", "remark",
          "remember", "remind", "repeat", "reply", "report", "request", "respond",
          "restate", "reveal", "rule", "say", "see", "show", "signal", "sing",
          "slam", "speculate", "spoke", "spread", "state", "stipulate", "stress",
          "suggest", "support", "suppose", "surmise", "suspect", "swear", "teach",
          "tell", "testify", "think", "threaten", "told", "uncover", "underline",
          "underscore", "urge", "voice", "vow", "warn", "welcome",
          "wish", "wonder", "worry", "write"));

    public final Set<String> reportNoun = new FastSet<>((Set<? extends String>) Arrays.asList(
            "acclamation", "account", "accusation", "acknowledgment", "address", "addressing",
            "admission", "advertisement", "advice", "advisory", "affidavit", "affirmation", "alert",
            "allegation", "analysis", "anecdote", "annotation", "announcement", "answer", "antiphon",
            "apology", "applause", "appreciation", "argument", "arraignment", "article", "articulation",
            "aside", "assertion", "asseveration", "assurance", "attestation", "attitude",
            "averment", "avouchment", "avowal", "axiom", "backcap", "band-aid", "basic", "belief", "bestowal",
            "bill", "blame", "blow-by-blow", "bomb", "book", "bow", "break", "breakdown", "brief", "briefing",
            "broadcast", "broadcasting", "bulletin", "buzz", "cable", "calendar", "call", "canard", "canon",
            "card", "cause", "censure", "certification", "characterization", "charge", "chat", "chatter",
            "chitchat", "chronicle", "chronology", "citation", "claim", "clarification", "close", "cognizance",
            "comeback", "comment", "commentary", "communication", "communique", "composition", "concept",
            "concession", "conference", "confession", "confirmation", "conjecture", "connotation", "construal",
            "construction", "consultation", "contention", "contract", "convention", "conversation", "converse",
            "conviction", "counterclaim", "credenda", "creed", "critique",
            "cry", "declaration", "defense", "definition", "delineation", "delivery", "demonstration",
            "denial", "denotation", "depiction", "deposition", "description", "detail", "details", "detention",
            "dialogue", "diction", "dictum", "digest", "directive", "disclosure", "discourse", "discovery",
            "discussion", "dispatch", "display", "disquisition", "dissemination", "dissertation", "divulgence",
            "dogma", "editorial", "ejaculation", "emphasis", "enlightenment",
            "enunciation", "essay", "evidence", "examination", "example", "excerpt", "exclamation",
            "excuse", "execution", "exegesis", "explanation", "explication", "exposing", "exposition", "expounding",
            "expression", "eye-opener", "feedback", "fiction", "findings", "fingerprint", "flash", "formulation",
            "fundamental", "gift", "gloss", "goods", "gospel", "gossip", "gratitude", "greeting",
            "guarantee", "hail", "hailing", "handout", "hash", "headlines", "hearing", "hearsay",
            "ideas", "idiom", "illustration", "impeachment", "implantation", "implication", "imputation",
            "incrimination", "indication", "indoctrination", "inference", "info", "information",
            "innuendo", "insinuation", "insistence", "instruction", "intelligence", "interpretation", "interview",
            "intimation", "intonation", "issue", "item", "itemization", "justification", "key", "knowledge",
            "leak", "letter", "locution", "manifesto",
            "meaning", "meeting", "mention", "message", "missive", "mitigation", "monograph", "motive", "murmur",
            "narration", "narrative", "news", "nod", "note", "notice", "notification", "oath", "observation",
            "okay", "opinion", "oral", "outline", "paper", "parley", "particularization", "phrase", "phraseology",
            "phrasing", "picture", "piece", "pipeline", "pitch", "plea", "plot", "portraiture", "portrayal",
            "position", "potboiler", "prating", "precept", "prediction", "presentation", "presentment", "principle",
            "proclamation", "profession", "program", "promulgation", "pronouncement", "pronunciation", "propaganda",
            "prophecy", "proposal", "proposition", "prosecution", "protestation", "publication", "publicity",
            "publishing", "quotation", "ratification", "reaction", "reason", "rebuttal", "receipt", "recital",
            "recitation", "recognition", "record", "recount", "recountal", "refutation", "regulation", "rehearsal",
            "rejoinder", "relation", "release", "remark", "rendition", "repartee", "reply", "report", "reporting",
            "representation", "resolution", "response", "result", "retort", "return", "revelation", "review",
            "rule", "rumble", "rumor", "rundown", "saying", "scandal", "scoop",
            "scuttlebutt", "sense", "showing", "sign", "signature", "significance", "sketch", "skinny", "solution",
            "speaking", "specification", "speech", "statement", "story", "study", "style", "suggestion",
            "summarization", "summary", "summons", "tale", "talk", "talking", "tattle", "telecast",
            "telegram", "telling", "tenet", "term", "testimonial", "testimony", "text", "theme", "thesis",
            "tract", "tractate", "tradition", "translation", "treatise", "utterance", "vent", "ventilation",
            "verbalization", "version", "vignette", "vindication", "warning",
            "warrant", "whispering", "wire", "word", "work", "writ", "write-up", "writeup", "writing",
            "acceptance", "complaint", "concern", "disappointment", "disclose", "estimate", "laugh", "pleasure", "regret",
            "resentment", "view"));

    public final Set<String> nonWords = new FastSet<>((Set<? extends String>) Arrays.asList("mm", "hmm", "ahem", "um"));
    public final Set<String> copulas = new FastSet<>((Set<? extends String>) Arrays.asList("is", "are", "were", "was", "be", "been", "become", "became", "becomes", "seem", "seemed", "seems", "remain", "remains", "remained"));
    public final Set<String> quantifiers = new FastSet<>((Set<? extends String>) Arrays.asList("not", "every", "any", "none", "everything", "anything", "nothing", "all", "enough"));
    public final Set<String> parts = new FastSet<>((Set<? extends String>) Arrays.asList("half", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "hundred", "thousand", "million", "billion", "tens", "dozens", "hundreds", "thousands", "millions", "billions", "group", "groups", "bunch", "number", "numbers", "pinch", "amount", "amount", "total", "all", "mile", "miles", "pounds"));
    public final Set<String> temporals = new FastSet<>((Set<? extends String>) Arrays.asList(
            "second", "minute", "hour", "day", "week", "month", "year", "decade", "century", "millennium",
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "now",
            "yesterday", "tomorrow", "age", "time", "era", "epoch", "morning", "evening", "day", "night", "noon", "afternoon",
            "semester", "trimester", "quarter", "term", "winter", "spring", "summer", "fall", "autumn", "season",
            "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"));


    public final Set<String> femalePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"her", "hers", "herself", "she"}));
    public final Set<String> malePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"he", "him", "himself", "his"}));
    public final Set<String> neutralPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "its", "itself", "where", "here", "there", "which"}));
    public final Set<String> possessivePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"my", "your", "his", "her", "its", "our", "their", "whose"}));
    public final Set<String> otherPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"who", "whom", "whose", "where", "when", "which"}));
    public final Set<String> thirdPersonPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves"}));
    public final Set<String> secondPersonPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"you", "yourself", "yours", "your", "yourselves"}));
    public final Set<String> firstPersonPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our"}));
    public final Set<String> moneyPercentNumberPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "its"}));
    public final Set<String> dateTimePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"when"}));
    public final Set<String> organizationPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "its", "they", "their", "them", "which"}));
    public final Set<String> locationPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "its", "where", "here", "there"}));
    public final Set<String> inanimatePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "itself", "its", "where", "when"}));
    public final Set<String> animatePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our", "you", "yourself", "yours", "your", "yourselves", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves", "who", "whom", "whose"}));
    public final Set<String> indefinitePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"another", "anybody", "anyone", "anything", "each", "either", "enough", "everybody", "everyone", "everything", "less", "little", "much", "neither", "no one", "nobody", "nothing", "one", "other", "plenty", "somebody", "someone", "something", "both", "few", "fewer", "many", "others", "several", "all", "any", "more", "most", "none", "some", "such"}));
    public final Set<String> relativePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"that", "who", "which", "whom", "where", "whose"}));
    public final Set<String> GPEPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "itself", "its", "they", "where"}));
    public final Set<String> pluralPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"we", "us", "ourself", "ourselves", "ours", "our", "yourself", "yourselves", "they", "them", "themself", "themselves", "theirs", "their"}));
    public final Set<String> singularPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"i", "me", "myself", "mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's"}));
    public final Set<String> facilityVehicleWeaponPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "itself", "its", "they", "where"}));
    public final Set<String> miscPronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"it", "itself", "its", "they", "where"}));
    public final Set<String> reflexivePronouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"myself", "yourself", "yourselves", "himself", "herself", "itself", "ourselves", "themselves", "oneself"}));
    public final Set<String> transparentNouns = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"bunch", "group",
            "breed", "class", "ilk", "kind", "half", "segment", "top", "bottom", "glass", "bottle",
            "box", "cup", "gem", "idiot", "unit", "part", "stage", "name", "division", "label", "group", "figure",
            "series", "member", "members", "first", "version", "site", "side", "role", "largest", "title", "fourth",
            "third", "second", "number", "place", "trio", "two", "one", "longest", "highest", "shortest",
            "head", "resident", "collection", "result", "last"
    }));
    public final Set<String> stopWords = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"a", "an", "the", "of", "at",
            "on", "upon", "in", "to", "from", "out", "as", "so", "such", "or", "and", "those", "this", "these", "that",
            "for", ",", "is", "was", "am", "are", "'s", "been", "were"}));

    public final Set<String> notOrganizationPRP = new FastSet<>((Set<? extends String>) Arrays.asList(new String[]{"i", "me", "myself",
            "mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "here"}));

    public final Set<String> quantifiers2 = new FastSet<>((Set<? extends String>) Arrays.asList("all", "both", "neither", "either"));
    public final Set<String> determiners = new FastSet<>((Set<? extends String>) Arrays.asList("the", "this", "that", "these", "those", "his", "her", "my", "your", "their", "our"));
    public final Set<String> negations = new FastSet<>((Set<? extends String>) Arrays.asList("n't", "not", "nor", "neither", "never", "no", "non", "any", "none", "nobody", "nothing", "nowhere", "nearly", "almost",
            "if", "false", "fallacy", "unsuccessfully", "unlikely", "impossible", "improbable", "uncertain", "unsure", "impossibility", "improbability", "cancellation", "breakup", "lack",
            "long-stalled", "end", "rejection", "failure", "avoid", "bar", "block", "break", "cancel", "cease", "cut", "decline", "deny", "deprive", "destroy", "excuse",
            "fail", "forbid", "forestall", "forget", "halt", "lose", "nullify", "prevent", "refrain", "reject", "rebut", "remain", "refuse", "stop", "suspend", "ward"));
    public final Set<String> neg_relations = new FastSet<>((Set<? extends String>) Arrays.asList("prep_without", "prepc_without", "prep_except", "prepc_except", "prep_excluding", "prepx_excluding",
            "prep_if", "prepc_if", "prep_whether", "prepc_whether", "prep_away_from", "prepc_away_from", "prep_instead_of", "prepc_instead_of"));
    public final Set<String> modals = new FastSet<>((Set<? extends String>) Arrays.asList("can", "could", "may", "might", "must", "should", "would", "seem",
            "able", "apparently", "necessarily", "presumably", "probably", "possibly", "reportedly", "supposedly",
            "inconceivable", "chance", "impossibility", "improbability", "encouragement", "improbable", "impossible",
            "likely", "necessary", "probable", "possible", "uncertain", "unlikely", "unsure", "likelihood", "probability",
            "possibility", "eventual", "hypothetical", "presumed", "supposed", "reported", "apparent"));

    public final Set<String> personPronouns = new FastSet<>();
    public final Set<String> allPronouns = new FastSet<>();

    public final Map<String, String> statesAbbreviation = new FastMap<>();
    public final Map<String, Set<String>> demonyms = new FastMap<>();
    public final Set<String> demonymSet = new FastSet<>();public final Set<String> adjectiveNation = new FastSet<>();

    public final Set<String> countries = new FastSet<>();
    public final Set<String> statesAndProvinces = new FastSet<>();

    public final Set<String> neutralWords = new FastSet<>();
    public final Set<String> femaleWords = new FastSet<>();
    public final Set<String> maleWords = new FastSet<>();

    public final Set<String> pluralWords = new FastSet<>();
    public final Set<String> singularWords = new FastSet<>();

    public final Set<String> inanimateWords = new FastSet<>();
    public final Set<String> animateWords = new FastSet<>();

    public final Map<List<String>, int[]> genderNumber = new FastMap<>();

    public final ArrayList<Counter<Pair<String, String>>> corefDict = new ArrayList<>(4);
  public final Counter<Pair<String, String>> corefDictPMI = new ClassicCounter<>();
  public final Map<String,Counter<String>> NE_signatures = new FastMap<>();

    private void setPronouns() {
    for(String s: animatePronouns){
      personPronouns.add(s);
    }

    allPronouns.addAll(firstPersonPronouns);
    allPronouns.addAll(secondPersonPronouns);
    allPronouns.addAll(thirdPersonPronouns);
    allPronouns.addAll(otherPronouns);

    stopWords.addAll(allPronouns);
  }

  public void loadStateAbbreviation(String statesFile) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(statesFile);
      while(reader.ready()){
        String[] tokens = reader.readLine().split("\t");
        statesAbbreviation.put(tokens[1], tokens[0]);
        statesAbbreviation.put(tokens[2], tokens[0]);
      }
    } catch (IOException e){
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  private void loadDemonymLists(String demonymFile) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(demonymFile);
      while(reader.ready()){
        String[] line = reader.readLine().split("\t");
        if(!line[0].isEmpty() && line[0].charAt(0) == '#') continue;
          Set<String> set = new FastSet<>();
        for(String s : line){
          set.add(s.toLowerCase());
          demonymSet.add(s.toLowerCase());
        }
        demonyms.put(line[0].toLowerCase(), set);
      }
      adjectiveNation.addAll(demonymSet);
      adjectiveNation.removeAll(demonyms.keySet());
    } catch (IOException e){
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  private static void getWordsFromFile(String filename, Set<String> resultSet, boolean lowercase) throws IOException {
    BufferedReader reader = IOUtils.readerFromString(filename);
    while(reader.ready()) {
      if(lowercase) resultSet.add(reader.readLine().toLowerCase());
      else resultSet.add(reader.readLine());
    }
    IOUtils.closeIgnoringExceptions(reader);
  }

  private void loadAnimacyLists(String animateWordsFile, String inanimateWordsFile) {
    try {
      getWordsFromFile(animateWordsFile, animateWords, false);
      getWordsFromFile(inanimateWordsFile, inanimateWords, false);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  private void loadGenderLists(String maleWordsFile, String neutralWordsFile, String femaleWordsFile) {
    try {
      getWordsFromFile(maleWordsFile, maleWords, false);
      getWordsFromFile(neutralWordsFile, neutralWords, false);
      getWordsFromFile(femaleWordsFile, femaleWords, false);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  private void loadNumberLists(String pluralWordsFile, String singularWordsFile) {
    try {
      getWordsFromFile(pluralWordsFile, pluralWords, false);
      getWordsFromFile(singularWordsFile, singularWords, false);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }
  private void loadStatesLists(String file) {
    try {
      getWordsFromFile(file, statesAndProvinces, true);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  private void loadCountriesLists(String file) {
    try{
      BufferedReader reader = IOUtils.readerFromString(file);
      while(reader.ready()) {
        String line = reader.readLine();
        countries.add(line.split("\t")[1].toLowerCase());
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  private void loadGenderNumber(String file){
    try {
      BufferedReader reader = IOUtils.readerFromString(file);
      String line;
      while ((line = reader.readLine())!=null){
        String[] split = line.split("\t");
        List<String> tokens = new ArrayList<>(Arrays.asList(split[0].split(" ")));
        String[] countStr = split[1].split(" ");
        int[] counts = new int[4];
        counts[0] = Integer.parseInt(countStr[0]);
        counts[1] = Integer.parseInt(countStr[1]);
        counts[2] = Integer.parseInt(countStr[2]);
        counts[3] = Integer.parseInt(countStr[3]);

        genderNumber.put(tokens, counts);
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }
  private void loadExtraGender(String file){
    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(file);
      while(reader.ready()) {
        String[] split = reader.readLine().split("\t");
        if(split[1].equals("MALE")) maleWords.add(split[0]);
        else if(split[1].equals("FEMALE")) femaleWords.add(split[0]);
      }
    } catch (IOException e){
      throw new RuntimeIOException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  private static void loadCorefDict(String[] file,
      ArrayList<Counter<Pair<String, String>>> dict) {

    for(int i = 0; i < 4; i++){
      dict.add(new ClassicCounter<Pair<String, String>>());

      BufferedReader reader = null;
      try {
        reader = IOUtils.readerFromString(file[i]);
        // Skip the first line (header)
        reader.readLine();

        while(reader.ready()) {
          String[] split = reader.readLine().split("\t");
          dict.get(i).setCount(new Pair<>(split[0], split[1]), Double.parseDouble(split[2]));
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeIgnoringExceptions(reader);
      }
    }
  }

  private static void loadCorefDictPMI(String file, Counter<Pair<String, String>> dict) {

      BufferedReader reader = null;
      try {
        reader = IOUtils.readerFromString(file);
        // Skip the first line (header)
        reader.readLine();

        while(reader.ready()) {
          String[] split = reader.readLine().split("\t");
          dict.setCount(new Pair<>(split[0], split[1]), Double.parseDouble(split[3]));
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeIgnoringExceptions(reader);
      }
  }

  private static void loadSignatures(String file, Map<String,Counter<String>> sigs) {
    BufferedReader reader = null;
    try {
      reader = IOUtils.readerFromString(file);

      while(reader.ready()) {
        String[] split = reader.readLine().split("\t");
        Counter<String> cntr = new ClassicCounter<>();
        sigs.put(split[0], cntr);
        for (int i = 1; i < split.length; i=i+2) {
          cntr.setCount(split[i], Double.parseDouble(split[i+1]));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeIgnoringExceptions(reader);
    }
  }

  public Dictionaries(Properties props) {
    this(props.getProperty(Constants.DEMONYM_PROP, DefaultPaths.DEFAULT_DCOREF_DEMONYM),
        props.getProperty(Constants.ANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_ANIMATE),
        props.getProperty(Constants.INANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_INANIMATE),
        props.getProperty(Constants.MALE_PROP, DefaultPaths.DEFAULT_DCOREF_MALE),
        props.getProperty(Constants.NEUTRAL_PROP, DefaultPaths.DEFAULT_DCOREF_NEUTRAL),
        props.getProperty(Constants.FEMALE_PROP, DefaultPaths.DEFAULT_DCOREF_FEMALE),
        props.getProperty(Constants.PLURAL_PROP, DefaultPaths.DEFAULT_DCOREF_PLURAL),
        props.getProperty(Constants.SINGULAR_PROP, DefaultPaths.DEFAULT_DCOREF_SINGULAR),
        props.getProperty(Constants.STATES_PROP, DefaultPaths.DEFAULT_DCOREF_STATES),
        props.getProperty(Constants.GENDER_NUMBER_PROP, DefaultPaths.DEFAULT_DCOREF_GENDER_NUMBER),
        props.getProperty(Constants.COUNTRIES_PROP, DefaultPaths.DEFAULT_DCOREF_COUNTRIES),
        props.getProperty(Constants.STATES_PROVINCES_PROP, DefaultPaths.DEFAULT_DCOREF_STATES_AND_PROVINCES),
        props.getProperty(Constants.EXTRA_GENDER_PROP, DefaultPaths.DEFAULT_DCOREF_EXTRA_GENDER),
        Boolean.parseBoolean(props.getProperty(Constants.BIG_GENDER_NUMBER_PROP, "false")) ||
        Boolean.parseBoolean(props.getProperty(Constants.REPLICATECONLL_PROP, "false")),
        props.getProperty(Constants.SIEVES_PROP, Constants.SIEVEPASSES).contains("CorefDictionaryMatch"),
        new String[]{DefaultPaths.DEFAULT_DCOREF_DICT1, DefaultPaths.DEFAULT_DCOREF_DICT2,
          DefaultPaths.DEFAULT_DCOREF_DICT3, DefaultPaths.DEFAULT_DCOREF_DICT4},
        DefaultPaths.DEFAULT_DCOREF_DICT1,
        DefaultPaths.DEFAULT_DCOREF_NE_SIGNATURES);
  }

  public static String signature(Properties props) {
    TextBuilder os = new TextBuilder();
    os.append(Constants.DEMONYM_PROP + ':').append(props.getProperty(Constants.DEMONYM_PROP,
            DefaultPaths.DEFAULT_DCOREF_DEMONYM));
    os.append(Constants.ANIMATE_PROP + ':').append(props.getProperty(Constants.ANIMATE_PROP,
            DefaultPaths.DEFAULT_DCOREF_ANIMATE));
    os.append(Constants.INANIMATE_PROP + ':').append(props.getProperty(Constants.INANIMATE_PROP,
            DefaultPaths.DEFAULT_DCOREF_INANIMATE));
    os.append(Constants.MALE_PROP + ':').append(props.getProperty(Constants.MALE_PROP,
            DefaultPaths.DEFAULT_DCOREF_MALE));
    os.append(Constants.NEUTRAL_PROP + ':').append(props.getProperty(Constants.NEUTRAL_PROP,
            DefaultPaths.DEFAULT_DCOREF_NEUTRAL));
    os.append(Constants.FEMALE_PROP + ':').append(props.getProperty(Constants.FEMALE_PROP,
            DefaultPaths.DEFAULT_DCOREF_FEMALE));
    os.append(Constants.PLURAL_PROP + ':').append(props.getProperty(Constants.PLURAL_PROP,
            DefaultPaths.DEFAULT_DCOREF_PLURAL));
    os.append(Constants.SINGULAR_PROP + ':').append(props.getProperty(Constants.SINGULAR_PROP,
            DefaultPaths.DEFAULT_DCOREF_SINGULAR));
    os.append(Constants.STATES_PROP + ':').append(props.getProperty(Constants.STATES_PROP,
            DefaultPaths.DEFAULT_DCOREF_STATES));
    os.append(Constants.GENDER_NUMBER_PROP + ':').append(props.getProperty(Constants.GENDER_NUMBER_PROP,
            DefaultPaths.DEFAULT_DCOREF_GENDER_NUMBER));
    os.append(Constants.COUNTRIES_PROP + ':').append(props.getProperty(Constants.COUNTRIES_PROP,
            DefaultPaths.DEFAULT_DCOREF_COUNTRIES));
    os.append(Constants.STATES_PROVINCES_PROP + ':').append(props.getProperty(Constants.STATES_PROVINCES_PROP,
            DefaultPaths.DEFAULT_DCOREF_STATES_AND_PROVINCES));
    os.append(Constants.EXTRA_GENDER_PROP + ':').append(props.getProperty(Constants.EXTRA_GENDER_PROP,
            DefaultPaths.DEFAULT_DCOREF_EXTRA_GENDER));
    os.append(Constants.BIG_GENDER_NUMBER_PROP + ':').append(props.getProperty(Constants.BIG_GENDER_NUMBER_PROP,
            "false"));
    os.append(Constants.REPLICATECONLL_PROP + ':').append(props.getProperty(Constants.REPLICATECONLL_PROP,
            "false"));
    return os.toString();
  }

  public Dictionaries(
      String demonymWords,
      String animateWords,
      String inanimateWords,
      String maleWords,
      String neutralWords,
      String femaleWords,
      String pluralWords,
      String singularWords,
      String statesWords,
      String genderNumber,
      String countries,
      String states,
      String extraGender,
      boolean loadBigGenderNumber,
      boolean loadCorefDict,
      String[] corefDictFiles,
      String corefDictPMIFile,
      String signaturesFile) {
    loadDemonymLists(demonymWords);
    loadStateAbbreviation(statesWords);
    if(Constants.USE_ANIMACY_LIST) loadAnimacyLists(animateWords, inanimateWords);
    if(Constants.USE_GENDER_LIST) loadGenderLists(maleWords, neutralWords, femaleWords);
    if(Constants.USE_NUMBER_LIST) loadNumberLists(pluralWords, singularWords);
    if(loadBigGenderNumber) loadGenderNumber(genderNumber);
    loadCountriesLists(countries);
    loadStatesLists(states);
    loadExtraGender(extraGender);
    setPronouns();
    if(loadCorefDict){
      loadCorefDict(corefDictFiles, corefDict);
      loadCorefDictPMI(corefDictPMIFile, corefDictPMI);
      loadSignatures(signaturesFile, NE_signatures);
    }
  }

  public Dictionaries() {
    this(new Properties());
  }

}
