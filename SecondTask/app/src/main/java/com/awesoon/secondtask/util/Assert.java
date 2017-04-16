package com.awesoon.secondtask.util;

public final class Assert {
  private Assert() {
  }

  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new NullPointerException(message);
    }
  }

  public static void isTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
