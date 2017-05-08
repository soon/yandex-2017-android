package com.awesoon.thirdtask.util;

import android.support.annotation.Nullable;

import java.math.BigDecimal;


public class NumberUtils {
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
  public static BigDecimal tryParseBigDecimal(@Nullable Object o) {
    try {
      return parseBigDecimal(o);
    } catch (Exception e) {
      return null;
    }
  }
}
