package edu.stanford.nlp.ling;

/**
 * A {@code Tag} object acts as a Label by containing a
 * {@code String} that is a part-of-speech tag.
 *
 * @author Christopher Manning
 * @version 2003/02/15 (implements TagFactory correctly now)
 */
public class Tag extends StringLabel implements HasTag {

  /**
   * 
   */
  private static final long serialVersionUID = 1143434026005416755L;


  /**
   * Constructs a Tag object.
   */
  public Tag() {
  }

  /**
   * Constructs a Tag object.
   *
   * @param tag The tag name
   */
  public Tag(String tag) {
    super(tag);
  }


  /**
   * Creates a new tag whose tag value is the value of any
   * class that supports the {@code Label} interface.
   *
   * @param lab The label to be used as the basis of the new Tag
   */
  public Tag(Label lab) {
    super(lab);
  }


  public String tag() {
    return value();
  }


  public void setTag(String tag) {
    setValue(tag);
  }


  /**
   * A {@code TagFactory} acts as a factory for creating objects
   * of class {@code Tag}
   */
  private static class TagFactory implements LabelFactory {

    public TagFactory() {
    }


    /**
     * Create a new {@code Tag}, where the label is formed
     * from the {@code String} passed in.
     *
     * @param cat The cat that will go into the {@code Tag}
     */
    public Label newLabel(String cat) {
      return new Tag(cat);
    }


    /**
     * Create a new {@code Tag}, where the label is formed
     * from the {@code String} passed in.
     *
     * @param cat     The cat that will go into the {@code Tag}
     * @param options is ignored by a TagFactory
     */
    public Label newLabel(String cat, int options) {
      return new Tag(cat);
    }


    /**
     * Create a new {@code Tag}, where the label is formed
     * from the {@code String} passed in.
     *
     * @param cat The cat that will go into the {@code Tag}
     */
    public Label newLabelFromString(String cat) {
      return new Tag(cat);
    }


    /**
     * Create a new {@code Tag Label}, where the label is
     * formed from
     * the {@code Label} object passed in.  Depending on what fields
     * each label has, other things will be {@code null}.
     *
     * @param oldLabel The Label that the new label is being created from
     * @return a new label of a particular type
     */
    public Label newLabel(Label oldLabel) {
      return new Tag(oldLabel);
    }

  }


  // extra class guarantees correct lazy loading (Bloch p.194)
  private static class LabelFactoryHolder {

    private static final LabelFactory lf = new TagFactory();

  }


  /**
   * Return a factory for this kind of label
   * (i.e., {@code Tag}).
   * The factory returned is always the same one (a singleton).
   *
   * @return The label factory
   */
  @Override
  public LabelFactory labelFactory() {
    return LabelFactoryHolder.lf;
  }


  /**
   * Return a factory for this kind of label
   * (i.e., {@code Tag}).
   * The factory returned is always the same one (a singleton).
   *
   * @return The label factory
   */
  public static LabelFactory factory() {
    return LabelFactoryHolder.lf;
  }

}
