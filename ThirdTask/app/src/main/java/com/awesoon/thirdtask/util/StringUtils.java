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
    return str == null || str .trim().isEmpty();
  }
}
