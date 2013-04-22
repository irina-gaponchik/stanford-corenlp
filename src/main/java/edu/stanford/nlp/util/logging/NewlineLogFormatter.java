package edu.stanford.nlp.util.logging;

import edu.stanford.nlp.util.TextBuilder;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** Simply format and put a newline after each log message.
 *
 *  @author Heeyoung LeeChristopher Manning
 */
public class NewlineLogFormatter extends Formatter {

  @Override
  public String format(LogRecord rec) {
    TextBuilder buf = new TextBuilder(1000);
    buf.append(formatMessage(rec));
    buf.append('\n');
    return buf.toString();
  }

}
