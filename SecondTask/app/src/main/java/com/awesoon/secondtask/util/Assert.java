package com.awesoon.secondtask.util;

public final class Assert {
  private Assert() {
  }

  /**
   * Ensures that the given value is not null. If the value is null, throws a NullPointerException.
   *
   * @param value   A value to check.
   * @param message An exception message.
   * @throws NullPointerException if the given value is null
   */
  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new NullPointerException(message);
    }
  }

  /**
   * Ensures that the given condition is true. If the condition is false, throws an AssertionError.
   *
   * @param condition A condition to check.
   * @param message   An exception message.
   * @throws AssertionError if the given condition is false.
   */
  public static void isTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
