//
// StanfordCoreNLP -- a suite of NLP tools
// Copyright (c) 2009-2010 The Board of Trustees of
// The Leland Stanford Junior University. All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Christopher Manning
//    Dept of Computer Science, Gates 1A
//    Stanford CA 94305-9010
//    USA
//

package edu.stanford.nlp.dcoref;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.dcoref.Dictionaries.Animacy;
import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.dcoref.Dictionaries.Number;
import edu.stanford.nlp.dcoref.Dictionaries.Person;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;
import javolution.text.TextBuilder;
import javolution.util.FastSet;

/**
 * One mention for the SieveCoreferenceSystem.
 *
 * @author Jenny Finkel, Karthik Raghunathan, Heeyoung Lee, Marta Recasens
 */
public class Mention implements CoreAnnotation<Mention>, Serializable {

  private static final long serialVersionUID = -7524485803945717057L;

  public Mention() {
  }

  public Mention(int mentionID, int startIndex, int endIndex, SemanticGraph dependency){
    this.mentionID = mentionID;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.dependency = dependency;
  }

  public Mention(int mentionID, int startIndex, int endIndex, SemanticGraph dependency, List<CoreLabel> mentionSpan){
    this.mentionID = mentionID;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.dependency = dependency;
    this.originalSpan = mentionSpan;
  }

  public Mention(int mentionID, int startIndex, int endIndex, SemanticGraph dependency, List<CoreLabel> mentionSpan, Tree mentionTree){
    this.mentionID = mentionID;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.dependency = dependency;
    this.originalSpan = mentionSpan;
    this.mentionSubTree = mentionTree;
  }

  public MentionType mentionType;
  public Number number;
  public edu.stanford.nlp.dcoref.Dictionaries.Gender gender;
  public Animacy animacy;
  public Person person;
  public String headString;
  public String nerString;

  public int startIndex;
  public int endIndex;
  public int headIndex;
  public int mentionID = -1;
  public int originalRef = -1;
  public IndexedWord headIndexedWord;

  public int goldCorefClusterID = -1;
  public int corefClusterID = -1;
  public int sentNum = -1;
  public int utter = -1;
  public int paragraph = -1;
  public boolean isSubject;
  public boolean isDirectObject;
  public boolean isIndirectObject;
  public boolean isPrepositionObject;
  public IndexedWord dependingVerb;
  public boolean twinless = true;
  public boolean generic;   // generic pronoun or generic noun (bare plurals)
  public boolean isSingleton;

  public List<CoreLabel> sentenceWords;
  public List<CoreLabel> originalSpan;

  public Tree mentionSubTree;
  public Tree contextParseTree;
  public CoreLabel headWord;
  public SemanticGraph dependency;
  public Set<String> dependents = new FastSet<>();
    public List<String> preprocessedTerms;
  public Object synsets;

  /** Set of other mentions in the same sentence that are syntactic appositions to this */
  public Set<Mention> appositions;
  public Set<Mention> predicateNominatives;
  public Set<Mention> relativePronouns;


  @Override
  public Class<Mention> getType() {
    return Mention.class;
  }

  public boolean isPronominal() {
    return mentionType == MentionType.PRONOMINAL;
  }

  @Override
  public String toString() {
    //    return headWord.toString();
    return spanToString();
  }

  public String spanToString() {
    TextBuilder os = new TextBuilder();
    for(int i = 0; i < originalSpan.size(); i ++){
      if(i > 0) os.append(' ');
      os.append(originalSpan.get(i).get(CoreAnnotations.TextAnnotation.class));
    }
    return os.toString();
  }

  /** Set attributes of a mention:
   * head string, mention type, NER label, Number, Gender, Animacy
   * @throws Exception
   */
  public void process(Dictionaries dict, Semantics semantics, MentionExtractor mentionExtractor) throws Exception {
    setHeadString();
    setType(dict);
    setNERString();
    List<String> mStr = getMentionString();
    setNumber(dict, getNumberCount(dict, mStr));
    setGender(dict, getGenderCount(dict, mStr));
    setAnimacy(dict);
    setPerson(dict);
    setDiscourse();
    headIndexedWord = dependency.getNodeByIndexSafe(headWord.index());
    if(semantics!=null) setSemantics(dict, semantics, mentionExtractor);
  }

  public void process(Dictionaries dict, Semantics semantics, MentionExtractor mentionExtractor,
      LogisticClassifier<String, String> singletonPredictor) throws Exception {
    process(dict, semantics, mentionExtractor);
    if(singletonPredictor != null) setSingleton(singletonPredictor, dict);
  }

  private void setSingleton(LogisticClassifier<String, String> predictor, Dictionaries dict){
    double coreference_score = predictor.probabilityOf(
        new BasicDatum<>(getSingletonFeatures(dict), "1"));
    if(coreference_score < 0.2) this.isSingleton = true;
  }

  /**
   * Returns the features used by the singleton predictor (logistic
   * classifier) to decide whether the mention belongs to a singleton entity
   */
  private ArrayList<String> getSingletonFeatures(Dictionaries dict){
    ArrayList<String> features = new ArrayList<>();
    features.add(mentionType.toString());
    features.add(nerString);
    features.add(animacy.toString());

    int personNum = 3;
    if(person.equals(Person.I) || person.equals(Person.WE)) personNum = 1;
    if(person.equals(Person.YOU)) personNum = 2;
    if(person.equals(Person.UNKNOWN)) personNum = 0;
    features.add(String.valueOf(personNum));
    features.add(number.toString());
    features.add(getPosition());
    features.add(getRelation());
    features.add(getQuantification(dict));
    features.add(String.valueOf(getModifiers(dict)));
    features.add(String.valueOf(getNegation(dict)));
    features.add(String.valueOf(getModal(dict)));
    features.add(String.valueOf(getReportEmbedding(dict)));
    features.add(String.valueOf(getCoordination()));
    return features;
  }

  private List<String> getMentionString() {
    List<String> mStr = new ArrayList<>();
    for(CoreLabel l : this.originalSpan) {
      mStr.add(l.get(CoreAnnotations.TextAnnotation.class).toLowerCase());
      if(l.equals(this.headWord)) break;   // remove words after headword
    }
    return mStr;
  }

  private static int[] getNumberCount(Dictionaries dict, List<String> mStr) {
    int len = mStr.size();
    if(len > 1) {
      for(int i = 0 ; i < len-1 ; i++) {
        if(dict.genderNumber.containsKey(mStr.subList(i, len))) return dict.genderNumber.get(mStr.subList(i, len));
      }

      // find converted string with ! (e.g., "dr. martin luther king jr. boulevard" -> "! boulevard")
      List<String> convertedStr = new ArrayList<>();
      convertedStr.add("!");
      convertedStr.add(mStr.get(len-1));
      if(dict.genderNumber.containsKey(convertedStr)) return dict.genderNumber.get(convertedStr);
    }
    if(dict.genderNumber.containsKey(mStr.subList(len-1, len))) return dict.genderNumber.get(mStr.subList(len-1, len));

    return null;
  }

  private int[] getGenderCount(Dictionaries dict, List<String> mStr) {
    int len = mStr.size();
    char firstLetter = headWord.get(CoreAnnotations.TextAnnotation.class).charAt(0);
    if(len > 1 && Character.isUpperCase(firstLetter) && nerString.startsWith("PER")) {
      int firstNameIdx = len-2;
      String secondToLast = mStr.get(firstNameIdx);
      if(firstNameIdx > 1 && (secondToLast.length()==1 || secondToLast.length()==2 && !secondToLast.isEmpty() && secondToLast.charAt(secondToLast.length() - 1) == '.')) {
        firstNameIdx--;
      }

      for(int i = 0 ; i <= firstNameIdx ; i++){
        if(dict.genderNumber.containsKey(mStr.subList(i, len))) return dict.genderNumber.get(mStr.subList(i, len));
      }

      // find converted string with ! (e.g., "dr. martin luther king jr. boulevard" -> "dr. !")
      List<String> convertedStr = new ArrayList<>();
      convertedStr.add(mStr.get(firstNameIdx));
      convertedStr.add("!");
      if(dict.genderNumber.containsKey(convertedStr)) return dict.genderNumber.get(convertedStr);

      if(dict.genderNumber.containsKey(mStr.subList(firstNameIdx, firstNameIdx+1))) return dict.genderNumber.get(mStr.subList(firstNameIdx, firstNameIdx+1));
    }

    if(dict.genderNumber.containsKey(mStr.subList(len-1, len))) return dict.genderNumber.get(mStr.subList(len-1, len));
    return null;
  }
  private void setDiscourse() {
    utter = headWord.get(CoreAnnotations.UtteranceAnnotation.class);

    Pair<IndexedWord, String> verbDependency = findDependentVerb(this);
    String dep = verbDependency.second();
    dependingVerb = verbDependency.first();

    isSubject = false;
    isDirectObject = false;
    isIndirectObject = false;
    isPrepositionObject = false;

    if(dep==null) {
      return;
    } else if(dep.equals("nsubj") || dep.equals("csubj")) {
      isSubject = true;
    } else if(dep.equals("dobj")){
      isDirectObject = true;
    } else if(dep.equals("iobj")){
      isIndirectObject = true;
    } else if(dep.equals("pobj")){
      isPrepositionObject = true;
    }
  }

  private void setPerson(Dictionaries dict) {
    // only do for pronoun
    if(!this.isPronominal()) person = Person.UNKNOWN;
    String spanToString = this.spanToString().toLowerCase();

    if(dict.firstPersonPronouns.contains(spanToString)) {
      if (number == Number.SINGULAR) {
        person = Person.I;
      } else person = number == Number.PLURAL ? Person.WE : Person.UNKNOWN;
    } else if(dict.secondPersonPronouns.contains(spanToString)) {
      person = Person.YOU;
    } else if(dict.thirdPersonPronouns.contains(spanToString)) {
      if (gender == Gender.MALE && number == Number.SINGULAR) {
        person = Person.HE;
      } else if (gender == Gender.FEMALE && number == Number.SINGULAR) {
        person = Person.SHE;
      } else if ((gender == Gender.NEUTRAL || animacy == Animacy.INANIMATE) && number == Number.SINGULAR) {
        person = Person.IT;
      } else person = number == Number.PLURAL ? Person.THEY : Person.UNKNOWN;
    } else {
      person = Person.UNKNOWN;
    }
  }

  private void setSemantics(Dictionaries dict, Semantics semantics, MentionExtractor mentionExtractor) throws Exception {

    preprocessedTerms = this.preprocessSearchTerm();

    if(dict.statesAbbreviation.containsKey(this.spanToString())) {  // states abbreviations
      preprocessedTerms = new ArrayList<>();
      preprocessedTerms.add(dict.statesAbbreviation.get(this.spanToString()));
    }

    Method meth = semantics.wordnet.getClass().getDeclaredMethod("findSynset", List.class);
    synsets = meth.invoke(semantics.wordnet, preprocessedTerms);

    if(this.isPronominal()) return;
  }

  private void setType(Dictionaries dict) {
    if (headWord.has(CoreAnnotations.EntityTypeAnnotation.class)){    // ACE gold mention type
      if (headWord.get(CoreAnnotations.EntityTypeAnnotation.class).equals("PRO")) {
        mentionType = MentionType.PRONOMINAL;
      } else
          mentionType = headWord.get(CoreAnnotations.EntityTypeAnnotation.class).equals("NAM") ? MentionType.PROPER : MentionType.NOMINAL;
    } else {    // MUC
      if(!headWord.has(CoreAnnotations.NamedEntityTagAnnotation.class)) {   // temporary fix
        mentionType = MentionType.NOMINAL;
        SieveCoreferenceSystem.logger.finest("no NamedEntityTagAnnotation: "+headWord);
      } else if (headWord.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("PRP")
          || originalSpan.size() == 1 && headWord.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("O")
              && (dict.allPronouns.contains(headString) || dict.relativePronouns.contains(headString) )) {
        mentionType = MentionType.PRONOMINAL;
      } else
          mentionType = !headWord.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("O") || headWord.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("NNP") ? MentionType.PROPER : MentionType.NOMINAL;
    }
  }

  private void setGender(Dictionaries dict, int... genderNumberCount) {
    gender = Gender.UNKNOWN;
    if (mentionType == MentionType.PRONOMINAL) {
      if (dict.malePronouns.contains(headString)) {
        gender = Gender.MALE;
      } else if (dict.femalePronouns.contains(headString)) {
        gender = Gender.FEMALE;
      }
    } else {
      if(Constants.USE_GENDER_LIST){
        // Bergsma list
        if(gender == Gender.UNKNOWN)  {
          if(dict.maleWords.contains(headString)) {
            gender = Gender.MALE;
            SieveCoreferenceSystem.logger.finest("[Bergsma List] New gender assigned:\tMale:\t" +  headString);
          }
          else if(dict.femaleWords.contains(headString))  {
            gender = Gender.FEMALE;
            SieveCoreferenceSystem.logger.finest("[Bergsma List] New gender assigned:\tFemale:\t" +  headString);
          }
          else if(dict.neutralWords.contains(headString))   {
            gender = Gender.NEUTRAL;
            SieveCoreferenceSystem.logger.finest("[Bergsma List] New gender assigned:\tNeutral:\t" +  headString);
          }
        }
      }
      if(genderNumberCount!=null && this.number!=Number.PLURAL){
        double male = genderNumberCount[0];
        double female = genderNumberCount[1];
        double neutral = genderNumberCount[2];

        if (male * 0.5 > female + neutral && male > 2) {
          this.gender = Gender.MALE;
        } else if (female * 0.5 > male + neutral && female > 2) {
          this.gender = Gender.FEMALE;
        } else if (neutral * 0.5 > male + female && neutral > 2)
          this.gender = Gender.NEUTRAL;
      }
    }
  }

  protected void setNumber(Dictionaries dict, int... genderNumberCount) {
    if (mentionType == MentionType.PRONOMINAL) {
      if (dict.pluralPronouns.contains(headString)) {
        number = Number.PLURAL;
      } else number = dict.singularPronouns.contains(headString) ? Number.SINGULAR : Number.UNKNOWN;
    } else if(! nerString.equals("O") && mentionType!=MentionType.NOMINAL){
        number = !(nerString.equals("ORGANIZATION") || nerString.startsWith("ORG")) ? Number.SINGULAR : Number.UNKNOWN;
    } else {
      String tag = headWord.get(CoreAnnotations.PartOfSpeechAnnotation.class);
      if (!tag.isEmpty() && tag.charAt(0) == 'N' && !tag.isEmpty() && tag.charAt(tag.length() - 1) == 'S') {
        number = Number.PLURAL;
      } else number = !tag.isEmpty() && tag.charAt(0) == 'N' ? Number.SINGULAR : Number.UNKNOWN;
    }

    if(mentionType != MentionType.PRONOMINAL) {
      if(Constants.USE_NUMBER_LIST){
        if(number == Number.UNKNOWN){
          if(dict.singularWords.contains(headString)) {
            number = Number.SINGULAR;
            SieveCoreferenceSystem.logger.finest("[Bergsma] Number set to:\tSINGULAR:\t" + headString);
          }
          else if(dict.pluralWords.contains(headString))  {
            number = Number.PLURAL;
            SieveCoreferenceSystem.logger.finest("[Bergsma] Number set to:\tPLURAL:\t" + headString);
          }
        }
      }

      String enumerationPattern = "NP < (NP=tmp $.. (/,|CC/ $.. NP))";

      TregexPattern tgrepPattern = TregexPattern.compile(enumerationPattern);
      TregexMatcher m = tgrepPattern.matcher(this.mentionSubTree);
      while (m.find()) {
        //        Tree t = m.getMatch();
        if(this.mentionSubTree.equals(m.getNode("tmp"))
           && this.spanToString().toLowerCase().contains(" and ")) {
          number = Number.PLURAL;
        }
      }
    }
  }

  private void setAnimacy(Dictionaries dict) {
    if (mentionType == MentionType.PRONOMINAL) {
      if (dict.animatePronouns.contains(headString)) {
        animacy = Animacy.ANIMATE;
      } else animacy = dict.inanimatePronouns.contains(headString) ? Animacy.INANIMATE : Animacy.UNKNOWN;
    } else if (nerString.equals("PERSON") || nerString.startsWith("PER")) {
      animacy = Animacy.ANIMATE;
    } else if (nerString.equals("LOCATION") || nerString.startsWith("LOC") || nerString.equals("MONEY") || nerString.equals("NUMBER") || nerString.equals("PERCENT") || nerString.equals("DATE") || nerString.equals("TIME")) {
      animacy = Animacy.INANIMATE;
    } else if (nerString.equals("MISC") || nerString.startsWith("VEH")) {
      animacy = Animacy.UNKNOWN;
    } else
        animacy = nerString.startsWith("FAC") || nerString.startsWith("GPE") || nerString.startsWith("WEA") || nerString.startsWith("ORG") ? Animacy.INANIMATE : Animacy.UNKNOWN;
    if(mentionType != MentionType.PRONOMINAL) {
      if(Constants.USE_ANIMACY_LIST){
        // Better heuristics using DekangLin:
        if(animacy == Animacy.UNKNOWN)  {
          if(dict.animateWords.contains(headString))  {
            animacy = Animacy.ANIMATE;
            SieveCoreferenceSystem.logger.finest("Assigned Dekang Lin animacy:\tANIMATE:\t" + headString);
          }
          else if(dict.inanimateWords.contains(headString)) {
            animacy = Animacy.INANIMATE;
            SieveCoreferenceSystem.logger.finest("Assigned Dekang Lin animacy:\tINANIMATE:\t" + headString);
          }
        }
      }
    }
  }

  private static final String [] commonNESuffixes = {
    "Corp", "Co", "Inc", "Ltd"
  };
  private static boolean knownSuffix(String s) {
    if(!s.isEmpty() && s.charAt(s.length() - 1) == '.') s = s.substring(0, s.length() - 1);
    for(String suff: commonNESuffixes){
      if(suff.equalsIgnoreCase(s)){
        return true;
      }
    }
    return false;
  }

  private void setHeadString() {
    this.headString = headWord.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
    if(headWord.has(CoreAnnotations.NamedEntityTagAnnotation.class)) {
      // make sure that the head of a NE is not a known suffix, e.g., Corp.
      int start = headIndex - startIndex;
      if (start >= originalSpan.size()) {
        throw new RuntimeException("Invalid start index " + start + '=' + headIndex + '-' + startIndex
                + ": originalSpan=[" + StringUtils.joinWords(originalSpan, " ") + "], head=" + headWord);
      }
      while(start >= 0){
        String head = originalSpan.get(start).get(CoreAnnotations.TextAnnotation.class).toLowerCase();
          if (knownSuffix(head)) {
              start--;
          } else {
              this.headString = head;
              break;
          }
      }
    }
  }

  private void setNERString() {
    if(headWord.has(CoreAnnotations.EntityTypeAnnotation.class)){ // ACE
        this.nerString = headWord.has(CoreAnnotations.NamedEntityTagAnnotation.class) && headWord.get(CoreAnnotations.EntityTypeAnnotation.class).equals("NAM") ? headWord.get(CoreAnnotations.NamedEntityTagAnnotation.class) : "O";
    }
    else{ // MUC
        this.nerString = headWord.has(CoreAnnotations.NamedEntityTagAnnotation.class) ? headWord.get(CoreAnnotations.NamedEntityTagAnnotation.class) : "O";
    }
  }

  public boolean sameSentence(Mention m) {
    return m.sentenceWords.equals(sentenceWords);
  }

  private static boolean included(CoreLabel small, List<CoreLabel> big) {
    if(small.tag().equals("NNP")){
      for(CoreLabel w: big){
        if(small.word().equals(w.word()) ||
            small.word().length() > 2 && w.word().startsWith(small.word())){
          return true;
        }
      }
    }
    return false;
  }

  protected boolean headsAgree(Mention m) {
    // we allow same-type NEs to not match perfectly, but rather one could be included in the other, e.g., "George" -> "George Bush"
      return !nerString.equals("O") && !m.nerString.equals("O") && nerString.equals(m.nerString) && (included(headWord, m.originalSpan) || included(m.headWord, originalSpan)) || headString.equals(m.headString);
  }

  public boolean numbersAgree(Mention m){
    return numbersAgree(m, false);
  }
  private boolean numbersAgree(Mention m, boolean strict) {
      return strict ? number == m.number : number == Number.UNKNOWN ||
              m.number == Number.UNKNOWN ||
              number == m.number;
  }

  public boolean gendersAgree(Mention m){
    return gendersAgree(m, false);
  }
  public boolean gendersAgree(Mention m, boolean strict) {
      return strict ? gender == m.gender : gender == Gender.UNKNOWN ||
              m.gender == Gender.UNKNOWN ||
              gender == m.gender;
  }

  public boolean animaciesAgree(Mention m){
    return animaciesAgree(m, false);
  }
  public boolean animaciesAgree(Mention m, boolean strict) {
      return strict ? animacy == m.animacy : animacy == Animacy.UNKNOWN ||
              m.animacy == Animacy.UNKNOWN ||
              animacy == m.animacy;
  }

  public boolean entityTypesAgree(Mention m, Dictionaries dict){
    return entityTypesAgree(m, dict, false);
  }

  public boolean entityTypesAgree(Mention m, Dictionaries dict, boolean strict) {
    if (strict) {
      return nerString.equals(m.nerString);
    } else {
      if (isPronominal()) {
        if (nerString.contains("-") || m.nerString.contains("-")) { //for ACE with gold NE
          if (m.nerString.equals("O")) {
            return true;
          } else if (m.nerString.startsWith("ORG")) {
            return dict.organizationPronouns.contains(headString);
          } else if (m.nerString.startsWith("PER")) {
            return dict.personPronouns.contains(headString);
          } else if (m.nerString.startsWith("LOC")) {
            return dict.locationPronouns.contains(headString);
          } else if (m.nerString.startsWith("GPE")) {
            return dict.GPEPronouns.contains(headString);
          } else
              return (m.nerString.startsWith("VEH") || m.nerString.startsWith("FAC") || m.nerString.startsWith("WEA")) && dict.facilityVehicleWeaponPronouns.contains(headString);
        } else {  // ACE w/o gold NE or MUC
            switch (m.nerString) {
                case "O":
                    return true;
                case "MISC":
                    return true;
                case "ORGANIZATION":
                    return dict.organizationPronouns.contains(headString);
                case "PERSON":
                    return dict.personPronouns.contains(headString);
                case "LOCATION":
                    return dict.locationPronouns.contains(headString);
                case "DATE":
                case "TIME":
                    return dict.dateTimePronouns.contains(headString);
                case "MONEY":
                case "PERCENT":
                case "NUMBER":
                    return dict.moneyPercentNumberPronouns.contains(headString);
                default:
                    return false;
            }
        }
      }
      return nerString.equals("O") ||
              m.nerString.equals("O") ||
              nerString.equals(m.nerString);
    }
  }



  /**
   * Verifies if this mention's tree is dominated by the tree of the given mention
   */
  public boolean includedIn(Mention m) {
    if (!m.sameSentence(this)) {
      return false;
    }
    if(this.startIndex < m.startIndex || this.endIndex > m.endIndex) return false;
    for (Tree t : m.mentionSubTree.subTrees()) {
      if (t.equals(mentionSubTree)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Detects if the mention and candidate antecedent agree on all attributes respectively.
   * @param potentialAntecedent
   * @return true if all attributes agree between both mention and candidate, else false.
   */
  public boolean attributesAgree(Mention potentialAntecedent, Dictionaries dict){
    return this.animaciesAgree(potentialAntecedent) &&
        this.entityTypesAgree(potentialAntecedent, dict) &&
        this.gendersAgree(potentialAntecedent) &&
        this.numbersAgree(potentialAntecedent);
  }

  /** Find apposition */
  public void addApposition(Mention m) {
    if(appositions == null) appositions = new FastSet<>();
    appositions.add(m);
  }

  /** Check apposition */
  public boolean isApposition(Mention m) {
      return appositions != null && appositions.contains(m);
  }
  /** Find predicate nominatives */
  public void addPredicateNominatives(Mention m) {
    if(predicateNominatives == null) predicateNominatives = new FastSet<>();
    predicateNominatives.add(m);
  }

  /** Check predicate nominatives */
  public boolean isPredicateNominatives(Mention m) {
      return predicateNominatives != null && predicateNominatives.contains(m);
  }

  /** Find relative pronouns */
  public void addRelativePronoun(Mention m) {
    if(relativePronouns == null) relativePronouns = new FastSet<>();
    relativePronouns.add(m);
  }

  /** Find which mention appears first in a document */
  public boolean appearEarlierThan(Mention m){
      return this.sentNum < m.sentNum || this.sentNum <= m.sentNum && (this.startIndex < m.startIndex || this.startIndex <= m.startIndex && (this.endIndex > m.endIndex ? true : false));
  }

  public String longestNNPEndsWithHead (){
    String ret = "";
    for (int i = headIndex; i >=startIndex ; i--){
      String pos = sentenceWords.get(i).get(CoreAnnotations.PartOfSpeechAnnotation.class);
      if(!pos.startsWith("NNP")) break;
      if(!ret.isEmpty()) ret = ' ' +ret;
      ret = sentenceWords.get(i).get(CoreAnnotations.TextAnnotation.class)+ret;
    }
    return ret;
  }

  public String lowestNPIncludesHead (){
    String ret = "";
    Tree head = this.contextParseTree.getLeaves().get(this.headIndex);
    Tree lowestNP = head;
    String s;
    while(true) {
      if(lowestNP==null) return ret;
      s = ((CoreLabel) lowestNP.label()).get(CoreAnnotations.ValueAnnotation.class);
      if(s.equals("NP") || s.equals("ROOT")) break;
      lowestNP = lowestNP.ancestor(1, this.contextParseTree);
    }
    if (s.equals("ROOT")) lowestNP = head;
    for (Tree t : lowestNP.getLeaves()){
      if (!ret.isEmpty()) ret = ret + ' ';
      ret = ret + ((CoreLabel) t.label()).get(CoreAnnotations.TextAnnotation.class);
    }
    if(!this.spanToString().contains(ret)) return this.sentenceWords.get(this.headIndex).get(CoreAnnotations.TextAnnotation.class);
    return ret;
  }

  public String stringWithoutArticle(String str) {
    String ret = str==null ? this.spanToString() : str;
    if (ret.startsWith("a ") || ret.startsWith("A ")) {
      return ret.substring(2);
    }
      if (ret.startsWith("an ") || ret.startsWith("An ")) {
        return ret.substring(3);
      }
      if (ret.startsWith("the ") || ret.startsWith("The "))
        return ret.substring(4);
      return ret;
  }

  public List<String> preprocessSearchTerm (){
    List<String> searchTerms = new ArrayList<>();
    String[] terms = new String[4];

    terms[0] = this.stringWithoutArticle(this.removePhraseAfterHead());
    terms[1] = this.stringWithoutArticle(this.lowestNPIncludesHead());
    terms[2] = this.stringWithoutArticle(this.longestNNPEndsWithHead());
    terms[3] = this.headString;

    for (String term : terms){

      if(term.contains("\"")) term = term.replace("\"", "\\\"");
      if(term.contains("(")) term = term.replace("(","\\(");
      if(term.contains(")")) term = term.replace(")", "\\)");
      if(term.contains("!")) term = term.replace("!", "\\!");
      if(term.contains(":")) term = term.replace(":", "\\:");
      if(term.contains("+")) term = term.replace("+", "\\+");
      if(term.contains("-")) term = term.replace("-", "\\-");
      if(term.contains("~")) term = term.replace("~", "\\~");
      if(term.contains("*")) term = term.replace("*", "\\*");
      if(term.contains("[")) term = term.replace("[", "\\[");
      if(term.contains("]")) term = term.replace("]", "\\]");
      if(term.contains("^")) term = term.replace("^", "\\^");
      if(term.isEmpty()) continue;

      if(term.isEmpty() || searchTerms.contains(term)) continue;
      if(term.equals(terms[3]) && !terms[2].isEmpty()) continue;
      searchTerms.add(term);
    }
    return searchTerms;
  }
  public static String buildQueryText(List<String> terms) {
    String query = "";
    for (String t : terms){
      query += t + ' ';
    }
    return query.trim();
  }

  /** Remove any clause after headword */
  public String removePhraseAfterHead(){
    String removed ="";
    int posComma = -1;
    int posWH = -1;
    for(int i = 0 ; i < this.originalSpan.size() ; i++){
      CoreLabel w = this.originalSpan.get(i);
      if(posComma == -1 && w.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals(",")) posComma = this.startIndex + i;
      if(posWH == -1 && !w.get(CoreAnnotations.PartOfSpeechAnnotation.class).isEmpty() && w.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'W') posWH = this.startIndex + i;
    }
    if(posComma!=-1 && this.headIndex < posComma){
      TextBuilder os = new TextBuilder();
      for(int i = 0; i < posComma-this.startIndex; i++){
        if(i > 0) os.append(' ');
        os.append(this.originalSpan.get(i).get(CoreAnnotations.TextAnnotation.class));
      }
      removed = os.toString();
    }
    if(posComma==-1 && posWH != -1 && this.headIndex < posWH){
      TextBuilder os = new TextBuilder();
      for(int i = 0; i < posWH-this.startIndex; i++){
        if(i > 0) os.append(' ');
        os.append(this.originalSpan.get(i).get(CoreAnnotations.TextAnnotation.class));
      }
      removed = os.toString();
    }
    if(posComma==-1 && posWH == -1){
      removed = this.spanToString();
    }
    return removed;
  }

  public static String removeParenthesis(String text) {
      return text.split("\\(").length > 0 ? text.split("\\(")[0].trim() : "";
  }

  // the mention is 'the + commonNoun' form
  protected boolean isTheCommonNoun() {
      return this.mentionType == MentionType.NOMINAL
              && this.spanToString().toLowerCase().startsWith("the ")
              && this.spanToString().split(" ").length == 2 ? true : false;
  }

  private static Pair<IndexedWord, String> findDependentVerb(Mention m) {
    Pair<IndexedWord, String> ret = new Pair<>();
    int headIndex = m.headIndex+1;
    try {
      IndexedWord w = m.dependency.getNodeByIndex(headIndex);
      if(w==null) return ret;
      while (true) {
        IndexedWord p = null;
        for(Pair<GrammaticalRelation,IndexedWord> parent : m.dependency.parentPairs(w)){
          if(ret.second()==null) {
            String relation = parent.first().getShortName();
            ret.setSecond(relation);
          }
          p = parent.second();
        }
        if(p==null || !p.get(CoreAnnotations.PartOfSpeechAnnotation.class).isEmpty() && p.get(CoreAnnotations.PartOfSpeechAnnotation.class).charAt(0) == 'V') {
          ret.setFirst(p);
          break;
        }
        if(w.equals(p)) return ret;
        w = p;
      }
    } catch (Exception e) {
      return ret;
    }
    return ret;
  }

  public boolean insideIn(Mention m){
    return this.sentNum == m.sentNum
            && m.startIndex <= this.startIndex
            && this.endIndex <= m.endIndex;
  }

  public boolean moreRepresentativeThan(Mention m){
    if(m==null) return true;
      if (mentionType == m.mentionType) {
          return headIndex - startIndex > m.headIndex - m.startIndex || (sentNum < m.sentNum || sentNum == m.sentNum && headIndex < m.headIndex ? true : false);
      } else {
          return mentionType == MentionType.PROPER && m.mentionType != MentionType.PROPER
                  || mentionType == MentionType.NOMINAL && m.mentionType == MentionType.PRONOMINAL ? true : false;
      }
  }

  //Returns filtered premodifiers (no determiners or numerals)
  public ArrayList<ArrayList<IndexedWord>> getPremodifiers(){

    ArrayList<ArrayList<IndexedWord>> premod = new ArrayList<>();

    if(headIndexedWord == null) return premod;
    for(Pair<GrammaticalRelation,IndexedWord> child : dependency.childPairs(headIndexedWord)){
      String function = child.first().getShortName();
      if(child.second().index() < headWord.index()
          && !child.second.tag().equals("DT") && !child.second.tag().equals("WRB")
          && !function.endsWith("det") && !function.equals("num")
          && !function.equals("rcmod") && !function.equals("infmod")
          && !function.equals("partmod") && !function.equals("punct")){
        ArrayList<IndexedWord> phrase = new ArrayList<>(dependency.descendants(child.second()));
        Collections.sort(phrase);
        premod.add(phrase);
      }
    }
    return premod;
  }

  // Returns filtered postmodifiers (no relative, -ed or -ing clauses)
  public ArrayList<ArrayList<IndexedWord>> getPostmodifiers(){

    ArrayList<ArrayList<IndexedWord>> postmod = new ArrayList<>();

    if(headIndexedWord == null) return postmod;
    for(Pair<GrammaticalRelation,IndexedWord> child : dependency.childPairs(headIndexedWord)){
      String function = child.first().getShortName();
      if(child.second().index() > headWord.index() &&
          !function.endsWith("det") && !function.equals("num")
          && !function.equals("rcmod") && !function.equals("infmod")
          && !function.equals("partmod") && !function.equals("punct")
          && !(function.equals("possessive") && dependency.descendants(child.second()).size() == 1)){
        ArrayList<IndexedWord> phrase = new ArrayList<>(dependency.descendants(child.second()));
        Collections.sort(phrase);
        postmod.add(phrase);
      }
    }
    return postmod;
  }


  public String[] getSplitPattern(){

    ArrayList<ArrayList<IndexedWord>> premodifiers = getPremodifiers();

    String[] components = new String[4];

    components[0] = headWord.lemma();

    if(premodifiers.isEmpty()){
      components[1] = headWord.lemma();
      components[2] = headWord.lemma();
    } else if(premodifiers.size() == 1){
      ArrayList<CoreLabel> premod = new ArrayList<>();
      premod.addAll(premodifiers.get(premodifiers.size()-1));
      premod.add(headWord);
      components[1] = getPattern(premod);
      components[2] = getPattern(premod);
    } else {
      ArrayList<CoreLabel> premod1 = new ArrayList<>();
      premod1.addAll(premodifiers.get(premodifiers.size()-1));
      premod1.add(headWord);
      components[1] = getPattern(premod1);

      ArrayList<CoreLabel> premod2 = new ArrayList<>();
      for(ArrayList<IndexedWord> premodifier : premodifiers){
        premod2.addAll(premodifier);
      }
      premod2.add(headWord);
      components[2] = getPattern(premod2);
    }

    components[3] = getPattern();
    return components;
  }

  public String getPattern(){

    ArrayList<CoreLabel> pattern = new ArrayList<>();
    for(ArrayList<IndexedWord> premodifier : getPremodifiers()){
      pattern.addAll(premodifier);
    }
    pattern.add(headWord);
    for(ArrayList<IndexedWord> postmodifier : getPostmodifiers()){
      pattern.addAll(postmodifier);
    }
    return getPattern(pattern);
  }

  public String getPattern(List<CoreLabel> pTokens){

    ArrayList<String> phrase_string = new ArrayList<>();
    String ne = "";
    for(CoreLabel token : pTokens)
        if (token.index() != headWord.index()) {
            if ((!token.lemma().equals("and") && !StringUtils.isPunct(token.lemma()))
                    || pTokens.size() <= pTokens.indexOf(token) + 1
                    || pTokens.indexOf(token) <= 0
                    || !pTokens.get(pTokens.indexOf(token) + 1).ner().equals(pTokens.get(pTokens.indexOf(token) - 1).ner())) {
                if (token.index() == headWord.index() - 1
                        && token.ner().equals(nerString)) {
                    phrase_string.add(token.lemma());
                    ne = "";

                } else if (!token.ner().equals("O")) {
                    if (!token.ner().equals(ne)) {
                        ne = token.ner();
                        phrase_string.add('<' + ne + '>');
                    }

                } else {
                    phrase_string.add(token.lemma());
                    ne = "";
                }
            }
        } else {
            phrase_string.add(token.lemma());
            ne = "";

        }
      return StringUtils.join(phrase_string);
  }

  public boolean isCoordinated(){
      if (headIndexedWord != null) {
          for (Pair<GrammaticalRelation, IndexedWord> child : dependency.childPairs(headIndexedWord)) {
              if (child.first().getShortName().equals("cc")) return true;
          }

      }
      return false;
  }

  private static List<String> getContextHelper(List<? extends CoreLabel> words) {
    List<List<CoreLabel>> namedEntities = new ArrayList<>();
    List<CoreLabel> ne = new ArrayList<>();
    String previousNEType = "";
    int previousNEIndex = -1;
    for (int i = 0; i < words.size(); i++) {
      CoreLabel word = words.get(i);
      if(!word.ner().equals("O")) {
        if (!word.ner().equals(previousNEType) || previousNEIndex != i-1) {
          ne = new ArrayList<>();
          namedEntities.add(ne);
        }
        ne.add(word);
        previousNEType = word.ner();
        previousNEIndex = i;
      }
    }

    List<String> neStrings = new ArrayList<>();
      Set<String> hs = new FastSet<>();
    for (List<CoreLabel> namedEntity : namedEntities) {
      String ne_str = StringUtils.joinWords(namedEntity, " ");
      hs.add(ne_str);
    }
    neStrings.addAll(hs);
    return neStrings;
  }

  public List<String> getContext() {
    return getContextHelper(sentenceWords);
  }

  public List<String> getPremodifierContext() {
    List<String> neStrings = new ArrayList<>();
    for (List<IndexedWord> words : getPremodifiers()) {
      neStrings.addAll(getContextHelper(words));
    }
    return neStrings;
  }

  /** Check relative pronouns */
  public boolean isRelativePronoun(Mention m) {
    return relativePronouns != null && relativePronouns.contains(m);
  }

  public boolean isRoleAppositive(Mention m, Dictionaries dict) {
    String thisString = this.spanToString();
    if(this.isPronominal() || dict.allPronouns.contains(thisString.toLowerCase())) return false;
    if(!m.nerString.startsWith("PER") && !m.nerString.equals("O")) return false;
      return !(!this.nerString.startsWith("PER") && !this.nerString.equals("O")) && !(!sameSentence(m) || !m.spanToString().startsWith(thisString)) && !(m.spanToString().contains("'") || m.spanToString().contains(" and ")) && !(!animaciesAgree(m) || this.animacy == Animacy.INANIMATE || this.gender == Gender.NEUTRAL || m.gender == Gender.NEUTRAL || !this.numbersAgree(m)) && !(dict.demonymSet.contains(thisString.toLowerCase()) || dict.demonymSet.contains(m.spanToString().toLowerCase()));
  }

  public boolean isDemonym(Mention m, Dictionaries dict){
    String thisString = this.spanToString().toLowerCase();
    String antString = m.spanToString().toLowerCase();
    if(thisString.startsWith("the ") || thisString.startsWith("The ")) {
      thisString = thisString.substring(4);
    }
    if(antString.startsWith("the ") || antString.startsWith("The ")) antString = antString.substring(4);

    if (dict.statesAbbreviation.containsKey(m.spanToString()) && dict.statesAbbreviation.get(m.spanToString()).equals(this.spanToString())
         || dict.statesAbbreviation.containsKey(this.spanToString()) && dict.statesAbbreviation.get(this.spanToString()).equals(m.spanToString())) {
      return true;
    }

    if(dict.demonyms.get(thisString)!=null){
      if(dict.demonyms.get(thisString).contains(antString)) return true;
    } else if(dict.demonyms.get(antString)!=null){
      if(dict.demonyms.get(antString).contains(thisString)) return true;
    }
    return false;
  }

  public String getPosition() {
    int size = sentenceWords.size();
    if(headIndex == 0) {
      return "first";
    }
      if (headIndex == size -1) {
        return "last";
      }
      if(headIndex > 0 && headIndex < size/3) {
        return "begin";
      }
      if (headIndex >= size/3 && headIndex < 2 * size/3) {
        return "middle";
      }
      if (headIndex >= 2 * size/3 && headIndex < size -1) {
        return "end";
      }
      return null;
  }

  public String getRelation(){

    if(headIndexedWord == null) return null;

    if(dependency.getRoots().isEmpty()) return null;
    // root relation
    if(dependency.getFirstRoot().equals(headIndexedWord)) return "root";
    if(!dependency.vertexSet().contains(dependency.getParent(headIndexedWord))) return null;
    GrammaticalRelation relation = dependency.reln(dependency.getParent(headIndexedWord), headIndexedWord);

    // adjunct relations
    if(relation.toString().startsWith("prep") || relation.equals(EnglishGrammaticalRelations.PREPOSITIONAL_OBJECT) || relation.equals(EnglishGrammaticalRelations.TEMPORAL_MODIFIER) || relation.equals(EnglishGrammaticalRelations.ADV_CLAUSE_MODIFIER) || relation.equals(EnglishGrammaticalRelations.ADVERBIAL_MODIFIER) || relation.equals(EnglishGrammaticalRelations.PREPOSITIONAL_COMPLEMENT)) return "adjunct";

    // subject relations
    if(relation.equals(EnglishGrammaticalRelations.NOMINAL_SUBJECT) || relation.equals(EnglishGrammaticalRelations.CLAUSAL_SUBJECT) || relation.equals(EnglishGrammaticalRelations.CONTROLLING_SUBJECT)) return "subject";
    if(relation.equals(EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT) || relation.equals(EnglishGrammaticalRelations.CLAUSAL_PASSIVE_SUBJECT)) return "subject";

    // verbal argument relations
    if(relation.equals(EnglishGrammaticalRelations.ADJECTIVAL_COMPLEMENT) || relation.equals(EnglishGrammaticalRelations.ATTRIBUTIVE) || relation.equals(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT) || relation.equals(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT) || relation.equals(EnglishGrammaticalRelations.AGENT) || relation.equals(EnglishGrammaticalRelations.DIRECT_OBJECT) || relation.equals(EnglishGrammaticalRelations.INDIRECT_OBJECT)) return "verbArg";

    // noun argument relations
    if(relation.equals(EnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER) || relation.equals(EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER) || relation.equals(EnglishGrammaticalRelations.ADJECTIVAL_MODIFIER) || relation.equals(EnglishGrammaticalRelations.APPOSITIONAL_MODIFIER) || relation.equals(EnglishGrammaticalRelations.POSSESSION_MODIFIER)) return "nounArg";

    return null;
  }

  public int getModifiers(Dictionaries dict){

    if(headIndexedWord == null) return 0;

    int count = 0;
    List<Pair<GrammaticalRelation, IndexedWord>> childPairs = dependency.childPairs(headIndexedWord);
    for(Pair<GrammaticalRelation, IndexedWord> childPair : childPairs) {
      GrammaticalRelation gr = childPair.first;
      IndexedWord word = childPair.second;
      if(gr.equals(EnglishGrammaticalRelations.ADJECTIVAL_MODIFIER) || gr.equals(EnglishGrammaticalRelations.PARTICIPIAL_MODIFIER)
          || gr.equals(EnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER) || gr.equals(EnglishGrammaticalRelations.INFINITIVAL_MODIFIER)
          || gr.toString().startsWith("prep_")) {
        count++;
      }
      // add noun modifier when the mention isn't a NER
      if(nerString.equals("O") && gr.equals(EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER)) {
        count++;
      }

      // add possessive if not a personal determiner
      if(gr.equals(EnglishGrammaticalRelations.POSSESSION_MODIFIER) && !dict.determiners.contains(word.lemma())) {
        count++;
      }
    }
    return count;
  }

  public String getQuantification(Dictionaries dict){

    if(headIndexedWord == null) return null;

    if(!nerString.equals("O")) return "definite";

    List<IndexedWord> quant = dependency.getChildrenWithReln(headIndexedWord, EnglishGrammaticalRelations.DETERMINER);
    List<IndexedWord> poss = dependency.getChildrenWithReln(headIndexedWord, EnglishGrammaticalRelations.POSSESSION_MODIFIER);
    String det = "";
    if(!quant.isEmpty()) {
      det = quant.get(0).lemma();
      if(dict.determiners.contains(det)) {
        return "definite";
      }
    }
    else if(!poss.isEmpty()) {
      return "definite";
    }
    else {
      quant = dependency.getChildrenWithReln(headIndexedWord, EnglishGrammaticalRelations.NUMERIC_MODIFIER);
      if(dict.quantifiers2.contains(det) || !quant.isEmpty()) {
        return "quantified";
      }
    }
    return "indefinite";
  }

  public int getNegation(Dictionaries dict) {

    if(headIndexedWord == null) return 0;

    // direct negation in a child
    Collection<IndexedWord> children = dependency.getChildren(headIndexedWord);
    for(IndexedWord child : children) {
      if(dict.negations.contains(child.lemma())) return 1;
    }

    // or has a sibling
    Collection<IndexedWord> siblings = dependency.getSiblings(headIndexedWord);
    for(IndexedWord sibling : siblings) {
      if(dict.negations.contains(sibling.lemma()) && !dependency.hasParentWithReln(headIndexedWord, EnglishGrammaticalRelations.NOMINAL_SUBJECT)) return 1;
    }
    // check the parent
    List<Pair<GrammaticalRelation,IndexedWord>> parentPairs = dependency.parentPairs(headIndexedWord);
    if (!parentPairs.isEmpty()) {
      Pair<GrammaticalRelation,IndexedWord> parentPair = parentPairs.get(0);
      GrammaticalRelation gr = parentPair.first;
      // check negative prepositions
      if(dict.neg_relations.contains(gr.toString())) return 1;
    }
    return 0;
  }

  public int getModal(Dictionaries dict) {

    if(headIndexedWord == null) return 0;

    // direct modal in a child
    Collection<IndexedWord> children = dependency.getChildren(headIndexedWord);
    for(IndexedWord child : children) {
      if(dict.modals.contains(child.lemma())) return 1;
    }

    // check the parent
    IndexedWord parent = dependency.getParent(headIndexedWord);
    if (parent != null) {
      if(dict.modals.contains(parent.lemma())) return 1;
      // check the children of the parent (that is needed for modal auxiliaries)
      IndexedWord child = dependency.getChildWithReln(parent,EnglishGrammaticalRelations.AUX_MODIFIER);
      if(!dependency.hasParentWithReln(headIndexedWord, EnglishGrammaticalRelations.NOMINAL_SUBJECT) && child != null && dict.modals.contains(child.lemma())) return 1;
    }

    // look at the path to root
    List<IndexedWord> path = dependency.getPathToRoot(headIndexedWord);
    if(path == null) return 0;
    for(IndexedWord word : path) {
      if(dict.modals.contains(word.lemma())) return 1;
    }
    return 0;
  }

  public int getReportEmbedding(Dictionaries dict) {

    if(headIndexedWord == null) return 0;

    // check adverbial clause with marker "as"
    Collection<IndexedWord> siblings = dependency.getSiblings(headIndexedWord);
    for(IndexedWord sibling : siblings) {
      if(dict.reportVerb.contains(sibling.lemma()) && dependency.hasParentWithReln(sibling,EnglishGrammaticalRelations.ADV_CLAUSE_MODIFIER)) {
        IndexedWord marker = dependency.getChildWithReln(sibling,EnglishGrammaticalRelations.MARKER);
        if (marker != null && marker.lemma().equals("as")) {
          return 1;
        }
      }
    }

    // look at the path to root
    List<IndexedWord> path = dependency.getPathToRoot(headIndexedWord);
    if(path == null) return 0;
    boolean isSubject = false;

    // if the node itself is a subject, we will not take into account its parent in the path
    if(dependency.hasParentWithReln(headIndexedWord, EnglishGrammaticalRelations.NOMINAL_SUBJECT)) isSubject = true;

    for (IndexedWord word : path) {
      if(!isSubject && (dict.reportVerb.contains(word.lemma()) || dict.reportNoun.contains(word.lemma()))) {
        return 1;
      }
      // check how to put isSubject
      isSubject = dependency.hasParentWithReln(word, EnglishGrammaticalRelations.NOMINAL_SUBJECT);
    }
    return 0;
  }

  public int getCoordination() {

    if(headIndexedWord == null) return 0;

    Set<GrammaticalRelation> relations = dependency.childRelns(headIndexedWord);
    for (GrammaticalRelation rel : relations) {
      if(rel.toString().startsWith("conj_")) {
        return 1;
      }
    }

    Set<GrammaticalRelation> parent_relations = dependency.relns(headIndexedWord);
    for (GrammaticalRelation rel : parent_relations) {
      if(rel.toString().startsWith("conj_")) {
        return 1;
      }
    }
    return 0;
  }

}
