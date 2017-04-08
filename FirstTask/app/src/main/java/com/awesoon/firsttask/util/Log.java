package com.awesoon.firsttask.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Log {
  private List<Message> messages = new ArrayList<>();
  private boolean addToGlobalLog = false;

  private static Log globalLog = new Log(false);

  public Log() {
    this(true);
  }

  private Log(boolean addToGlobalLog) {
    this.addToGlobalLog = addToGlobalLog;
  }

  public void addEntry(int level, String tag, String message) {
    Message entry = new Message(level, tag, message);
    addEntry(entry);
    if (addToGlobalLog) {
      globalLog.addEntry(entry);
    }
  }

  public void clear() {
    messages.clear();
  }

  private void addEntry(Message entry) {
    messages.add(entry);
  }

  public int size() {
    return messages.size();
  }

  public Message get(int index) {
    return messages.get(index);
  }

  /**
   * Collapses all log messages into a list of blocks.
   * Each block contains log entries with same level, tag, and message (see {@link Message#equalsUpToTime(Message)}
   *
   * @return A list of collapsed log entries.
   */
  public List<List<Log.Message>> collapseMessages() {
    List<List<Log.Message>> result = new ArrayList<>();
    if (size() == 0) {
      return result;
    }

    result.add(new ArrayList<Log.Message>());
    result.get(0).add(get(0));

    for (int i = 1; i < size(); i++) {
      List<Log.Message> lastBlock = result.get(result.size() - 1);
      Log.Message logEntry = get(i);

      if (lastBlock.get(0).equalsUpToTime(logEntry)) {
        lastBlock.add(logEntry);
      } else {
        ArrayList<Log.Message> newBlock = new ArrayList<>();
        newBlock.add(logEntry);
        result.add(newBlock);
      }
    }

    return result;
  }

  public static Log getGlobalLog() {
    return globalLog;
  }

  public static class Message {
    private int level;
    private String tag;
    private String message;
    private long timeMillis;

    public Message(int level, String tag, String message) {
      Assert.notNull(tag, "tag must not be null");
      Assert.notNull(tag, "message must not be null");

      this.level = level;
      this.tag = tag;
      this.message = message;
      this.timeMillis = System.currentTimeMillis();
    }

    public int getLevel() {
      return level;
    }

    public String getTag() {
      return tag;
    }

    public String getMessage() {
      return message;
    }

    public boolean equalsUpToTime(Message msg) {
      return Objects.equals(level, msg.level)
          && Objects.equals(tag, msg.tag)
          && Objects.equals(message, msg.message);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Message message1 = (Message) o;

      if (level != message1.level) {
        return false;
      }
      if (timeMillis != message1.timeMillis) {
        return false;
      }
      if (!tag.equals(message1.tag)) {
        return false;
      }
      return message.equals(message1.message);

    }

    @Override
    public int hashCode() {
      int result = level;
      result = 31 * result + tag.hashCode();
      result = 31 * result + message.hashCode();
      result = 31 * result + (int) (timeMillis ^ (timeMillis >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return "Message{"
          + "level=" + level
          + ", tag='" + tag + '\''
          + ", message='" + message + '\''
          + ", timeMillis=" + timeMillis
          + '}';
    }

    public long getTimeMillis() {
      return timeMillis;
    }
  }
}
