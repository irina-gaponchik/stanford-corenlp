package edu.stanford.nlp.ling;

/**
 * A {@code WordTagFactory} acts as a factory for creating
 * objects of class {@code WordTag}.  Note that
 * {@code WordTag} is a replacement for (now deprecated)
 * {@code TaggedWord}, which has a less stringent equality
 * condition.
 *
 * @author Christopher Manning, Roger Levy
 * @version 2003/04
 */
public class WordTagFactory implements LabelFactory {

  private final char divider;


  /**
   * Create a new {@code WordTagFactory}.
   * The divider will be taken as '/'.
   */
  public WordTagFactory() {
    this('/');
  }


  /**
   * Create a new {@code WordTagFactory}.
   *
   * @param divider This character will be used in calls to the one
   *                argument version of {@code newLabel()}, to divide
   *                the word from the tag.  Stuff after the last instance of this
   *                character will become the tag, and stuff before it will
   *                become the label.
   */
  public WordTagFactory(char divider) {
    this.divider = divider;
  }


  /**
   * Make a new label with this {@code String} as the value (word).
   * Any other fields of the label would normally be null.
   *
   * @param labelStr The String that will be used for value
   * @return The new WordTag (tag will be {@code null})
   */
  public Label newLabel(String labelStr) {
    return new WordTag(labelStr);
  }


  /**
   * Make a new label with this {@code String} as a value component.
   * Any other fields of the label would normally be null.
   *
   * @param labelStr The String that will be used for value
   * @param options  what to make (use labelStr as word or tag)
   * @return The new WordTag (tag or word will be {@code null})
   */
  public Label newLabel(String labelStr, int options) {
      return options == TaggedWordFactory.TAG_LABEL ? new WordTag(null, labelStr) : new WordTag(labelStr);
  }


  /**
   * Create a new word, where the label is formed from
   * the {@code String} passed in.  The String is divided according
   * to the divider character.  We assume that we can always just
   * divide on the rightmost divider character, rather than trying to
   * parse up escape sequences.  If the divider character isn't found
   * in the word, then the whole string becomes the word, and the tag
   * is {@code null}.
   *
   * @param word The word that will go into the {@code Word}
   * @return The new WordTag
   */
  public Label newLabelFromString(String word) {
    int where = word.lastIndexOf(divider);
      return where >= 0 ? new WordTag(word.substring(0, where), word.substring(where + 1)) : new WordTag(word);
  }


  /**
   * Create a new {@code WordTag Label}, where the label is
   * formed from
   * the {@code Label} object passed in.  Depending on what fields
   * each label has, other things will be {@code null}.
   *
   * @param oldLabel The Label that the new label is being created from
   * @return a new label of a particular type
   */
  public Label newLabel(Label oldLabel) {
      return oldLabel instanceof HasTag ? new WordTag(oldLabel.value(), ((HasTag) oldLabel).tag()) : new WordTag(oldLabel.value());
  }
}
