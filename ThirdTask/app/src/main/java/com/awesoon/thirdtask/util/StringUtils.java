package com.awesoon.thirdtask.util;

public final class StringUtils {
  private StringUtils() {
  }

  public static String makeEmptyIfNull(CharSequence value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }
}
