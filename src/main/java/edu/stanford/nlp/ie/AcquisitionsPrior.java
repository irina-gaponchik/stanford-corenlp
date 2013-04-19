package edu.stanford.nlp.ie;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.StringUtils;
import javolution.util.FastSet;
import javolution.util.FastTable;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jenny Finkel
 */

public class AcquisitionsPrior<IN extends CoreMap> extends EntityCachingAbstractSequencePrior<IN> {

  double penalty = 4.0;
  double penalty1 = 3.0;
  double penalty2 = 4.0;

  public AcquisitionsPrior(String backgroundSymbol, Index<String> classIndex, List<IN> doc) {
    super(backgroundSymbol, classIndex, doc);
  }

  public double scoreOf(int... sequence) {

      Set<String> purchasers =new FastSet<>();
      Set<String> purchabrs = new FastSet<>();
      Set<String> sellers =   new FastSet<>();
      Set<String> sellerabrs =new FastSet<>();
      Set<String> acquireds = new FastSet<>();
      Set<String> acqabrs =   new FastSet<>();

    List<Entity> purchasersL =  new FastTable<>();
    List<Entity> purchabrsL =   new FastTable<>();
    List<Entity> sellersL =     new FastTable<>();
    List<Entity> sellerabrsL =  new FastTable<>();
    List<Entity> acquiredsL =   new FastTable<>();
    List<Entity> acqabrsL =     new FastTable<>();

    double p = 0.0;
    for (int i = 0; i < entities.length; i++) {
      Entity entity = entities[i];
      if ((i == 0 || !entities[i - 1].equals(entity)) && entity != null) {

        String type = classIndex.get(entity.type);
        String phrase = StringUtils.join(entity.words, " ").toLowerCase();
          switch (type) {
              case "purchaser":
                  purchasers.add(phrase);
                  purchasersL.add(entity);
                  break;
              case "purchabr":
                  purchabrs.add(phrase);
                  purchabrsL.add(entity);
                  break;
              case "seller":
                  sellers.add(phrase);
                  sellersL.add(entity);
                  break;
              case "sellerabr":
                  sellerabrs.add(phrase);
                  sellerabrsL.add(entity);
                  break;
              case "acquired":
                  acquireds.add(phrase);
                  acquiredsL.add(entity);
                  break;
              case "acqabr":
                  acqabrs.add(phrase);
                  acqabrsL.add(entity);
                  break;
              default:
                  System.err.println("unknown entity type: " + type);
                  System.exit(0);
          }
      }
    }
    
    for (Entity purchaser : purchasersL) {
      if (purchasers.size() > 1) {
        p -= purchaser.words.size() * penalty;
      }
      String s = StringUtils.join(purchaser.words, "").toLowerCase();
      boolean match = false;
      for (Entity purchabr : purchabrsL) {
        String s1 = StringUtils.join(purchabr.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s.contains(s1)) {
          match = true;
          break;
        }
      }
      if (!match && !purchabrs.isEmpty()) {
        p -= purchaser.words.size() * penalty;
      }
    }

    for (Entity seller : sellersL) {
      if (sellers.size() > 1) {
        p -= seller.words.size() * penalty;
      }
      String s = StringUtils.join(seller.words, "").toLowerCase();
      boolean match = false;
      for (Entity sellerabr : sellerabrsL) {
        String s1 = StringUtils.join(sellerabr.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s.contains(s1)) {
          match = true;
          break;
        }
      }
      if (!match && !sellerabrs.isEmpty()) {
        p -= seller.words.size() * penalty;
      }
    }
    
    for (Entity acquired : acquiredsL) {
      if (acquireds.size() > 1) {
        p -= acquired.words.size() * penalty;
      }
      String s = StringUtils.join(acquired.words, "").toLowerCase();
      boolean match = false;
      for (Entity acqabr : acqabrsL) {
        String s1 = StringUtils.join(acqabr.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s.contains(s1)) {
          match = true;
          break;
        }
      }
      if (!match && !acqabrs.isEmpty()) {
        p -= acquired.words.size() * penalty;
      }
    }

    
    for (Entity purchabr : purchabrsL) {
      //p -= purchabr.words.size() * penalty;
      String s = StringUtils.join(purchabr.words, "").toLowerCase();
      boolean match = false;
      for (Entity purchaser : purchasersL) {
        String s1 = StringUtils.join(purchaser.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (!match) {
        p -= purchabr.words.size() * penalty2;
      }
      
      match = false;
      for (Entity acquired : acquiredsL) {
        String s1 = StringUtils.join(acquired.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      for (Entity seller : sellersL) {
        String s1 = StringUtils.join(seller.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (match) {
        p -= purchabr.words.size() * penalty1;
      }
    }

    for (Entity sellerabr : sellerabrsL) {
      //p -= sellerabr.words.size() * penalty;
      String s = StringUtils.join(sellerabr.words, "").toLowerCase();
      boolean match = false;
      for (Entity seller : sellersL) {
        String s1 = StringUtils.join(seller.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (!match) {
        p -= sellerabr.words.size() * penalty2;
      }
      
      
      match = false;
      for (Entity acquired : acquiredsL) {
        String s1 = StringUtils.join(acquired.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      for (Entity purchaser : purchasersL) {
        String s1 = StringUtils.join(purchaser.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (match) {
        p -= sellerabr.words.size() * penalty1;
      }
    }


    for (Entity acqabr : acqabrsL) {
      //p -= acqabr.words.size() * penalty;
      String s = StringUtils.join(acqabr.words, "").toLowerCase();
      boolean match = false;
      for (Entity acquired : acquiredsL) {
        String s1 = StringUtils.join(acquired.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s1.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (!match) {
        p -= acqabr.words.size() * penalty2;
      }
      
      match = false;
      for (Entity seller : sellersL) {
        String s1 = StringUtils.join(seller.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          //System.err.println(acqabr.toString(classIndex)+"\n"+seller.toString(classIndex)+"\n");
          match = true;
            break;
        }
      }
      for (Entity purchaser : purchasersL) {
        String s1 = StringUtils.join(purchaser.words, "").toLowerCase();
        //int dist = StringUtils.longestCommonSubstring(s, s1);          
        //if (dist > s.length() - 2) {
        if (s1.contains(s)) {
          match = true;
          break;
        }
      }
      if (match) {
        p -= acqabr.words.size() * penalty1;
      }
    }

    return p;
  }
  
}
