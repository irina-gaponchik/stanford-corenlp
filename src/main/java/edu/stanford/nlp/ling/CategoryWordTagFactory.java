package edu.stanford.nlp.ling;


/**
 * A {@code CategoryWordTagFactory} is a factory that makes
 * a {@code Label} which is a {@code CategoryWordTag} triplet.
 *
 * @author Christopher Manning
 */
public class CategoryWordTagFactory implements LabelFactory {

  /**
   * Make a new label with this {@code String} as the "name".
   *
   * @param labelStr The string to use as a label
   * @return The newly created Label
   */
  public Label newLabel(String labelStr) {
    return new CategoryWordTag(labelStr);
  }

  /**
   * Make a new label with this {@code String} as the value.
   * This implementation ignores the options
   *
   * @param labelStr The String that will be used for balue
   * @param options  This argument is ignored
   * @return The newly created Label
   */
  public Label newLabel(String labelStr, int options) {
    return new CategoryWordTag(labelStr);
  }

  /**
   * Make a new label with this {@code String} as the "name".
   *
   * @param labelStr The string to use as a label
   * @return The newly created Label
   */
  public Label newLabelFromString(String labelStr) {
    CategoryWordTag cwt = new CategoryWordTag();
    cwt.setFromString(labelStr);
    return cwt;
  }

  /**
   * Create a new CategoryWordTag label, where the label is formed from
   * the various {@code String} objects passed in.
   *
   * @param word     The word part of the label
   * @param tag      The tag part of the label
   * @param category The category part of the label
   * @return The newly created Label
   */
  public static Label newLabel(String word, String tag, String category) {
    // System.out.println("Making new CWT label: " + category + " | " +
    //		   word + " | " + tag);
    return new CategoryWordTag(category, word, tag);
  }

  /**
   * Create a new {@code CategoryWordTag Label}, where the label is
   * formed from
   * the {@code Label} object passed in.  Depending on what fields
   * each label has, other things will be {@code null}.
   *
   * @param oldLabel The Label that the new label is being created from
   * @return a new label of a particular type
   */
  public Label newLabel(Label oldLabel) {
    return new CategoryWordTag(oldLabel);
  }

}

