package com.awesoon.firsttask.util;

import java.util.ArrayList;
import java.util.List;

public class Log {
  private List<Message> messages = new ArrayList<>();

  public void i(String tag, String message) {
   messages.add(new Message(Severity.INFO, tag, message));
  }

  public int size() {
    return messages.size();
  }

  public Message get(int index) {
    return messages.get(index);
  }

  public static class Message {
    private Severity severity;
    private String tag;
    private String message;

    public Message(Severity severity, String tag, String message) {
      this.severity = severity;
      this.tag = tag;
      this.message = message;
    }

    public Severity getSeverity() {
      return severity;
    }

    public String getTag() {
      return tag;
    }

    public String getMessage() {
      return message;
    }
  }

  public enum Severity {
    INFO
  }
}
