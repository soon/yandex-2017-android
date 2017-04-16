package com.awesoon.secondtask.util;

public final class MathUtil {
  public static float fitToBounds(float min, float current, float max) {
    return Math.max(min, Math.min(current, max));
  }
}
