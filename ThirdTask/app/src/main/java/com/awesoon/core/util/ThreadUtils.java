package com.awesoon.core.util;

public final class ThreadUtils {
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (Exception ignored) {

    }
  }
}
