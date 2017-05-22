package com.awesoon.thirdtask.util;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;


public class NumberUtils {
  private static final DecimalFormat SHORT_STRING_DECIMAL_FORMATTER = new DecimalFormat("#.#");

  @Nullable
  public static BigDecimal parseBigDecimal(@Nullable Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof BigDecimal) {
      return (BigDecimal) o;
    }
    if (o instanceof String) {
      return new BigDecimal((String) o);
    }

    throw new RuntimeException("Unable to parse big decimal");
  }

  @Nullable
  public static Long parseLong(@Nullable Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof Long) {
      return (Long) o;
    }
    if (o instanceof String) {
      return Long.parseLong((String) o);
    }

    throw new RuntimeException("Unable to parse long from " + o);
  }

  @Nullable
  public static BigDecimal tryParseBigDecimal(@Nullable Object o) {
    try {
      return parseBigDecimal(o);
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static Long tryParseLong(@Nullable Object o) {
    try {
      return parseLong(o);
    } catch (Exception e) {
      return null;
    }
  }

  public static int nextRandomInt(Random rnd, int start, int endExclusive) {
    Assert.isTrue(endExclusive > start, "endExclusive should be greater than start");
    return rnd.nextInt(endExclusive - start) + start;
  }

  public static String makeShortString(int number) {
    String[] suffixes = {"", "K", "M"};
    int suffixIndex = 0;
    double resultNumber = number;

    while (resultNumber >= 1000 && suffixIndex < suffixes.length - 1) {
      resultNumber /= 1000;
      suffixIndex++;
    }

    return SHORT_STRING_DECIMAL_FORMATTER.format(resultNumber) + suffixes[suffixIndex];
  }

  public static int getPercentage(int currentNumber, int maxNumber) {
    return (int) ((double) currentNumber / maxNumber * 100);
  }
}
