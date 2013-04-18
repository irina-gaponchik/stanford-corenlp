package edu.stanford.nlp.ling;


/**
 * A {@code StringLabelFactory} object makes a simple
 * {@code StringLabel} out of a {@code String}.
 *
 * @author Christopher Manning
 */
public class StringLabelFactory implements LabelFactory {

  /**
   * Make a new label with this {@code String} as the "name".
   *
   * @param labelStr A string that determines the content of the label.
   *                 For a StringLabel, it is exactly the given string
   * @return The created label
   */
  public Label newLabel(String labelStr) {
    return new StringLabel(labelStr);
  }


  /**
   * Make a new label with this {@code String} as the "name".
   *
   * @param labelStr A string that determines the content of the label.
   *                 For a StringLabel, it is exactly the given string
   * @param options  The options are ignored by a StringLabelFactory
   * @return The created label
   */
  public Label newLabel(String labelStr, int options) {
    return new StringLabel(labelStr);
  }


  /**
   * Make a new label with this {@code String} as the "name".
   * This version does no decoding -- StringLabels just have a value.
   *
   * @param labelStr A string that determines the content of the label.
   *                 For a StringLabel, it is exactly the given string
   * @return The created label
   */
  public Label newLabelFromString(String labelStr) {
    return new StringLabel(labelStr);
  }


  /**
   * Create a new {@code StringLabel}, where the label is
   * formed from
   * the {@code Label} object passed in.  Depending on what fields
   * each label has, other things will be {@code null}.
   *
   * @param oldLabel The Label that the new label is being created from
   * @return a new label of a particular type
   */
  public Label newLabel(Label oldLabel) {
    return new StringLabel(oldLabel);
  }

}
