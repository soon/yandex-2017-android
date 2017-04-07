package com.awesoon.firsttask.testUtils;

import com.awesoon.firsttask.util.Log;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LogMatcher {
  private Log log;
  private int index;
  private boolean isRecordingRepeatingAction;
  private List<Log.Message> repeatingActions = new ArrayList<>();
  private boolean isDisabled;

  public LogMatcher(Log log) {
    this.log = log;
    this.index = 0;
  }

  private boolean checkEntry(Log.Message message) {
    if (index >= log.size()) {
      return false;
    }
    Log.Message logEntry = log.get(index);
    return logEntry.equalsUpToTime(message);
  }

  public LogMatcher assertMatches(int level, String tag, String message) {
    if (isDisabled) {
      return this;
    }

    assertLogHasRemainingMessages();
    Log.Message logMessage = log.get(index);
    assertThat("Level does not match at log entry #" + index,
        logMessage.getLevel(), is(level));
    assertThat("Tag does not match at log entry #" + index,
        logMessage.getTag(), is(tag));
    assertThat("Message does not match at log entry #" + index,
        logMessage.getMessage(), is(message));
    index++;
    if (isRecordingRepeatingAction) {
      repeatingActions.add(new Log.Message(level, tag, message));
    }
    return this;
  }

  public LogMatcher startRepeatingAction() {
    if (isDisabled) {
      return this;
    }

    assertThat("Already recording repeating action", isRecordingRepeatingAction, is(false));
    isRecordingRepeatingAction = true;
    return this;
  }

  public LogMatcher checkRepeatingAction() {
    if (isDisabled) {
      return this;
    }

    assertThat(repeatingActions.size(), greaterThan(0));
    isRecordingRepeatingAction = false;
    Log.Message firstMessage = repeatingActions.get(0);
    boolean matches = checkEntry(firstMessage);

    while (matches) {
      for (int i = 0; i < repeatingActions.size(); ++i) {
        Log.Message message = repeatingActions.get(i);
        assertMatches(message.getLevel(), message.getTag(), message.getMessage());
      }
      matches = checkEntry(firstMessage);
    }

    repeatingActions.clear();
    return this;
  }

  public LogMatcher disableWhen(boolean condition) {
    if (condition) {
      isDisabled = true;
    }
    return this;
  }

  public LogMatcher enable() {
    isDisabled = false;
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
