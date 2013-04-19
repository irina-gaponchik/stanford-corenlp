package edu.stanford.nlp.util.logging;

import javolution.text.TxtBuilder;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** Simply format and put a newline after each log message.
 *
 *  @author Heeyoung LeeChristopher Manning
 */
public class NewlineLogFormatter extends Formatter {

  @Override
  public String format(LogRecord rec) {
    TxtBuilder buf = new TxtBuilder(1000);
    buf.append(formatMessage(rec));
    buf.append('\n');
    return buf.toString();
  }

}
