package com.awesoon.thirdtask.util;

public final class BooleanUtils {
  public static boolean isTrue(Boolean value) {
    return value != null && value;
  }

  public static boolean isFalse(Boolean value) {
    return value != null && !value;
  }
}
