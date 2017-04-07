package com.awesoon.firsttask.util;

public final class Assert {
  private Assert() {
  }

  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new NullPointerException(message);
    }
  }
}
