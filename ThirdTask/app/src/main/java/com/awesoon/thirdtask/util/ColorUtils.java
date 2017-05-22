package com.awesoon.thirdtask.util;

public final class ColorUtils {
  public static String toHexString(int color) {
    return String.format("#%06x", color);
  }

  public static int colorStringToInt(String hex) {
    if (StringUtils.isBlank(hex)) {
      return 0;
    }

    hex = hex.trim();
    int radix = 10;
    if (hex.charAt(0) == '#') {
      hex = hex.substring(1);
      radix = 16;
    }

    try {
      return (int) Long.parseLong(hex, radix);
    } catch (Exception e) {
      return 0;
    }
  }
}
