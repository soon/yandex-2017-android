package com.awesoon.thirdtask.util;

import android.support.annotation.Nullable;

public final class StringUtils {
  private StringUtils() {
  }

  public static String makeEmptyIfNull(@Nullable CharSequence value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }

  public static boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }

  public static String trim(String str) {
    return str == null ? "" : str.trim();
  }

  public static boolean containsIgnoreCaseTrimmed(String original, String pattern) {
    if (original == null || pattern == null) {
      return false;
    }

    return original.toLowerCase().contains(pattern.toLowerCase().trim());
  }

  public static boolean startsWithTrimmed(String original, String pattern) {
    if (original == null || pattern == null) {
      return false;
    }

    return original.trim().startsWith(pattern.trim());
  }

  public static boolean areSameTrimmed(String s1, String s2) {
    return trim(s1).equals(trim(s2));
  }
}
