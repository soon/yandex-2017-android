package com.awesoon.firsttask.testUtils;

import com.awesoon.firsttask.util.Log;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LogMatcher {
  private Log log;
  private int index;

  public LogMatcher(Log log) {
    this.log = log;
    this.index = 0;
  }

  public LogMatcher assertMatches(Log.Severity severity, String tag, String message) {
    assertLogHasRemainingMessages();
    Log.Message logMessage = log.get(index);
    assertThat("Severity does not match at log entry #" + index,
        logMessage.getSeverity(), is(severity));
    assertThat("Tag does not match at log entry #" + index,
        logMessage.getTag(), is(tag));
    assertThat("Message does not match at log entry #" + index,
        logMessage.getMessage(), is(message));
    index++;
    return this;
  }

  public LogMatcher assertAllMessagesChecked() {
    assertThat("Log has remaining messages", log.size(), equalTo(index));
    return this;
  }

  private void assertLogHasRemainingMessages() {
    assertThat("No more messages in the log", log.size(), greaterThan(index));
  }
}
