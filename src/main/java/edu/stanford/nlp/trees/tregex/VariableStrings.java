package edu.stanford.nlp.trees.tregex;

import edu.stanford.nlp.stats.IntCounter;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

import java.util.Map;

/** A class that takes care of the stuff necessary for variable strings.
 *
 *  @author Roger Levy (rog@nlp.stanford.edu)
 */
class VariableStrings {

  private final FastMap<String, String> varsToStrings;
  private final IntCounter<String> numVarsSet;

  public VariableStrings() {
      varsToStrings = new FastMap<String, String>();
    numVarsSet = new IntCounter<>();
  }

  public boolean isSet(String o) {
    return numVarsSet.get(o) >= 1;
  }

  public void setVar(String var, String string) {
    String oldString = varsToStrings.put(var,string);
    if(oldString != null && ! oldString.equals(string))
      throw new RuntimeException("Error -- can't setVar to a different string -- old: " + oldString + " new: " + string);
    numVarsSet.incrementCount(var);
  }

  public void unsetVar(String var) {
    if(numVarsSet.get(var) > 0)
      numVarsSet.decrementCount(var);
    if(numVarsSet.get(var)==0)
      varsToStrings.put(var,null);
  }

  public String getString(String var) {
    return varsToStrings.get(var);
  }

  @Override
  public String toString() {
    TextBuilder s = new TextBuilder();
    s.append('{');
    boolean appended = false;
    for (Map.Entry<String, String> stringStringEntry : varsToStrings.entrySet()) {
      if (appended) {
        s.append(',');
      } else {
        appended = true;
      }
      s.append(stringStringEntry.getKey());
      s.append("=(");
      s.append(stringStringEntry.getValue());
      s.append(':');
      s.append(numVarsSet.get(stringStringEntry.getKey()));
      s.append(')');
    }
    s.append('}');
    return s.toString();
  }

}
