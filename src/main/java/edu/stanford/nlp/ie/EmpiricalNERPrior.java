package edu.stanford.nlp.ie;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;


/**
 * @author Jenny Finkel
 */

public class EmpiricalNERPrior<IN extends CoreMap> extends EntityCachingAbstractSequencePrior<IN> {

  protected final static  String ORG = "ORGANIZATION";
  protected final static  String PER = "PERSON";
  protected final static  String LOC = "LOCATION";
  protected final static  String MISC = "MISC";

  public EmpiricalNERPrior(String backgroundSymbol, Index<String> classIndex, List<IN> doc) {
    super(backgroundSymbol, classIndex, doc);
  }
  
  protected double p1 = -Math.log(0.01);

  protected double dem1 = 6631.0;
  protected double p2 = -Math.log(6436.0 / dem1)/2.0;
  protected double p3 = -Math.log(188 / dem1)/2.0;
  protected double p4 = -Math.log(4 / dem1)/2.0;
  protected double p5 = -Math.log(3 / dem1)/2.0;
  
  protected double dem2 = 3169.0;
  protected double p6 = -Math.log(188.0 / dem2)/2.0;
  protected double p7 = -Math.log(2975 / dem2)/2.0;
  protected double p8 = -Math.log(5 / dem2)/2.0;
  protected double p9 = -Math.log(1 / dem2)/2.0;

  protected double dem3 = 3151.0;
  protected double p10 = -Math.log(4.0 / dem3)/2.0;
  protected double p11 = -Math.log(5 / dem3)/2.0;
  protected double p12 = -Math.log(3141 / dem3)/2.0;
  protected double p13 = -Math.log(1 / dem3)/2.0;
  
  protected double dem4 = 2035.0;
  protected double p14 = -Math.log(3.0 / dem4)/2.0;
  protected double p15 = -Math.log(1 / dem4)/2.0;
  protected double p16 = -Math.log(1 / dem4)/2.0;
  protected double p17 = -Math.log(2030 / dem4)/2.0;
  
  protected double dem5 = 724.0;
  protected double p18 = -Math.log(167.0 / dem5);
  protected double p19 = -Math.log(328.0 / dem5);
  protected double p20 = -Math.log(5.0 / dem5);
  protected double p21 = -Math.log(224.0 / dem5);
  
  protected double dem6 = 834.0;
  protected double p22 = -Math.log(6.0 / dem6);
  protected double p23 = -Math.log(819.0 / dem6);
  protected double p24 = -Math.log(2.0 / dem6);
  protected double p25 = -Math.log(7.0 / dem6);
  
  protected double dem7 = 1978.0;
  protected double p26 = -Math.log(1.0 / dem7);
  protected double p27 = -Math.log(22.0 / dem7);
  protected double p28 = -Math.log(1941.0 / dem7);
  protected double p29 = -Math.log(14.0 / dem7);
  
  protected double dem8 = 622.0;
  protected double p30 = -Math.log(63.0 / dem8);
  protected double p31 = -Math.log(191.0 / dem8);
  protected double p32 = -Math.log(3.0 / dem8);
  protected double p33 = -Math.log(365.0 / dem8);

  public double scoreOf(int... sequence) {
    double p = 0.0;
    for (int i = 0; i < entities.length; i++) {
      Entity entity = entities[i];
      //System.err.println(entity);
      if ((i == 0 || !entities[i - 1].equals(entity)) && entity != null) {
        //System.err.println(1);
        int length = entity.words.size();
        String tag1 = classIndex.get(entity.type);

        int[] other = entities[i].otherOccurrences;
          for (int anOther : other) {

              Entity otherEntity = null;
              for (int k = anOther; k < anOther + length && k < entities.length; k++) {
                  otherEntity = entities[k];
                  if (otherEntity != null) {
                      break;
                  }
              }
              if (otherEntity != null) {

                  int oLength = otherEntity.words.size();
                  String tag2 = classIndex.get(otherEntity.type);

                  // exact match??
                  boolean exact = false;
                  int[] oOther = otherEntity.otherOccurrences;
                  for (int anOOther : oOther) {
                      if (anOOther >= i && anOOther <= i + length - 1) {
                          exact = true;
                          break;
                      }
                  }

                  if (exact) {
                      // entity not complete
                      if (length != oLength)
                          if (tag1.equals(tag2)) {
                              p -= Math.abs(oLength - length) * p1;
                          } else // shorter
                              if (((!tag1.equals(ORG) || !tag2.equals(LOC))) &&
                                      ((!tag2.equals(LOC) || !tag1.equals(ORG)))) {
                                  p -= (oLength + length) * p1;
                              }
                      switch (tag1) {
                          case LOC:
                              if (tag2.equals(LOC)) {
                                  break;
                              }
                      }
                      switch (tag2) {
                          case PER:
                              //p -= length * Math.log(188 / dem);
                              p -= length * p3;
                              break;
                          case MISC:
                              //p -= length * Math.log(3 / dem);
                              p -= length * p5;
                              break;
                          default:
                              switch (tag1) {
                                  case ORG: //p -= length * Math.log(4 / dem);
                                      p -= length * p4;

                                      break;
                                  case PER:
                                      switch (tag2) {
                                          case LOC:
                                              //p -= length * Math.log(4.0 / dem);
                                              p -= length * p10;
                                              break;
                                          default:
                                              switch (tag2) {
                                                  case ORG:
                                                      //p -= length * Math.log(5 / dem);
                                                      p -= length * p11;
                                                      break;
                                                  default:
                                                      switch (tag2) {
                                                          case PER:
                                                              continue;
                                                          case MISC:
                                                              p -= length * p13;
                                                              break;
                                                      }
                                                      break;
                                              }
                                              break;
                                      }
                                      break;
                                  case MISC:
                                      switch (tag2) {
                                          case LOC:
                                              //p -= length * Math.log(3.0 / dem);
                                              p -= length * p14;
                                              break;
                                          case ORG:
                                              //p -= length * Math.log(1 / dem);
                                              p -= length * p15;
                                              break;
                                          case PER:
                                              //p -= length * Math.log(1 / dem);
                                              p -= length * p16;
                                              break;
                                          case MISC:
                                              //p -= length * Math.log(2030 / dem);
                                              //p -= length * p17;
                                              break;
                                      }


                                      break;
                                  default:
                                      switch (tag2) {
                                          case LOC:
                                              //p -= length * Math.log(188.0 / dem);
                                              p -= length * p6;
                                              break;
                                          case ORG:
                                              //p -= length * Math.log(2975 / dem);
                                              //p -= length * p7;
                                              break;
                                          case PER:
                                              //p -= length * Math.log(5 / dem);
                                              p -= length * p8;
                                              break;
                                          case MISC:
                                              //p -= length * Math.log(1 / dem);
                                              p -= length * p9;
                                              break;
                                      }

                                      break;
                              }
                              break;
                      }
                      switch (tag2) {
                          case ORG:
                              continue;
                      }
                  } else {
                      switch (tag1) {
                          case LOC:
                              //double dem = 724.0;
                              switch (tag2) {
                                  case LOC:
                                  case ORG:
                                      //p -= length * Math.log(167.0 / dem);
                                      //p -= length * p18;
                                      break;
                                  case PER:
                                      //p -= length * Math.log(5.0 / dem);
                                      p -= length * p20;
                                      break;
                                  case MISC:
                                      //p -= length * Math.log(224.0 / dem);
                                      p -= length * p21;
                                      break;
                              }
                              break;
                          case ORG:
                              //double dem = 834.0;
                              switch (tag2) {
                                  case LOC:
                                      //p -= length * Math.log(6.0 / dem);
                                      p -= length * p22;
                                      break;
                                  case ORG:
                                      //p -= length * Math.log(819.0 / dem);
                                      //p -= length * p23;
                                      break;
                                  case PER:
                                      //p -= length * Math.log(2.0 / dem);
                                      p -= length * p24;
                                      break;
                                  case MISC:
                                      //p -= length * Math.log(7.0 / dem);
                                      p -= length * p25;
                                      break;
                              }
                              break;
                          case PER:
                              //double dem = 1978.0;
                              switch (tag2) {
                                  case LOC:
                                      //p -= length * Math.log(1.0 / dem);
                                      p -= length * p26;
                                      break;
                                  case ORG:
                                      //p -= length * Math.log(22.0 / dem);
                                      p -= length * p27;
                                      break;
                                  case PER:
                                      //p -= length * Math.log(1941.0 / dem);
                                      //p -= length * p28;
                                      break;
                                  case MISC:
                                      //p -= length * Math.log(14.0 / dem);
                                      p -= length * p29;
                                      break;
                              }
                              break;
                          case MISC:
                              //double dem = 622.0;
                              switch (tag2) {
                                  case LOC:
                                      //p -= length * Math.log(63.0 / dem);
                                      p -= length * p30;
                                      break;
                                  case ORG:
                                      //p -= length * Math.log(191.0 / dem);
                                      p -= length * p31;
                                      break;
                                  case PER:
                                      //p -= length * Math.log(3.0 / dem);
                                      p -= length * p32;
                                      break;
                                  case MISC:
                                      //p -= length * Math.log(365.0 / dem);
                                      p -= length * p33;
                                      break;
                              }
                              break;
                      }
                  }
              }

          }
      }
    }
    return p;
  }
}
